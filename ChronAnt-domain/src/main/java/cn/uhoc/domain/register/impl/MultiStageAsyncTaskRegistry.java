package cn.uhoc.domain.register.impl;


import cn.uhoc.domain.launcher.entity.TaskContext;
import cn.uhoc.domain.launcher.entity.TaskStageMeta;
import cn.uhoc.domain.launcher.entity.TaskStageResult;
import cn.uhoc.domain.register.IMultiStageAsyncTaskRegistry;
import cn.uhoc.domain.register.MultiStageAsyncTask;
import cn.uhoc.domain.register.TaskStage;
import cn.uhoc.domain.task.TaskRet;
import cn.uhoc.type.common.ReflectionUtils;
import cn.uhoc.type.enums.ExceptionStatus;
import cn.uhoc.type.exception.E;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Component
public class MultiStageAsyncTaskRegistry implements IMultiStageAsyncTaskRegistry {

    // 注册管理所有带有 @MultiStageAsyncTask 注解的类的 Class 对象
    private static final Map<String, Class<?>> taskTypeMap = new HashMap<>();
    // 任务类型到阶段方法的映射
    private static final Map<String, Map<String, Method>> taskStageMethodMap = new HashMap<>();
    // 任务阶段关系，note 目前只是单一流转（线性任务）并不涉及复杂流转（并行任务）
    private static final Map<String, Map<String, List<String>>> taskStageFlowMap = new HashMap<>();
    // 每个任务类型对应一个代理类实例
    private static final Map<String, TaskProxyHandler> TaskProxyMap = new HashMap<>();

    public static Map<String, TaskProxyHandler> getTaskProxyMap() {
        return Collections.unmodifiableMap(TaskProxyMap); // 返回不可修改的 Map
    }

    @Resource
    private ApplicationContext applicationContext;

    // 在任务类实例化后将其注册到 Map 中
    @Override
    public void init() {
        // 扫描 Spring 容器中的所有 Bean，找出带有 @MultiStageAsyncTask 注解的类
        Map<String, Object> taskTypeBeanMap = applicationContext.getBeansWithAnnotation(MultiStageAsyncTask.class);

        // 遍历所有带有 @MultiStageAsyncTask 注解的 Bean，将其 Class 对象注册到 Map 中
        for (Object taskTypeBean : taskTypeBeanMap.values()) {
            Class<?> taskTypeClazz = taskTypeBean.getClass();
            MultiStageAsyncTask annotation = taskTypeClazz.getAnnotation(MultiStageAsyncTask.class);
            String taskType = StringUtils.defaultIfBlank(annotation.taskType(), taskTypeClazz.getSimpleName());
            taskTypeMap.put(taskType, taskTypeClazz);
            log.info("注册任务类型 : {}", taskType);
            // 解析阶段方法
            Map<String, Method> stageMethodMap = new HashMap<>();
            Map<String, List<String>> stageFlowMap = new HashMap<>();
            for (Method method : taskTypeClazz.getDeclaredMethods()) {
                // 标注了 @TaskStage 注解的方法是任务的阶段方法
                if (method.isAnnotationPresent(TaskStage.class)) {
                    String stageName = method.getName();
                    stageMethodMap.put(stageName, method);
                    String nextStageName = method.getAnnotation(TaskStage.class).nextStage();
                    stageFlowMap.put(stageName, Collections.singletonList(nextStageName));
                }
            }
            // 存储到阶段方法映射中
            taskStageMethodMap.put(taskType, stageMethodMap);
            taskStageFlowMap.put(taskType, stageFlowMap);
        }
        // 初始化任务代理对象
        initTaskProxyMap();
    }

    private void initTaskProxyMap() {
        for (String taskType : taskTypeMap.keySet()) {
            Map<String, Method> stageMethodMap = taskStageMethodMap.get(taskType);
            if (stageMethodMap == null || stageMethodMap.isEmpty()) {
                log.error("任务类型 '{}' 没有注册任何阶段方法，请检查自定义任务配置，并使用 @TaskStage 来标记阶段方法", taskType);
                continue;
            }
            TaskProxyHandler proxy = new TaskProxyHandler(taskType);
            TaskProxyMap.put(taskType, proxy);
        }
    }

    @Override
    public Class<?> getTaskClass(String taskTypeName) {
        Class<?> taskClazz = taskTypeMap.get(taskTypeName);
        if (taskClazz == null) {
            log.error("未找到任务类型的Class对象, taskType: {}", taskTypeName);
            throw new E(ExceptionStatus.NO_CLASS_FOR_TASK_TYPE);
        }
        return taskClazz;
    }

    @Override
    public Set<String> getTaskTypeSet() {
        return taskTypeMap.keySet();
    }

    // ================= 内部代理类 ==================

    /**
     * 动态代理类，负责任务阶段方法的调用
     */
    public static class TaskProxyHandler {

        private final Class<?> taskClass;
        private final Map<String, Method> stageMethodMap;
        private final Map<String, List<String>> stageFlowMap;

        public TaskProxyHandler(String taskType) {
            this.taskClass = taskTypeMap.get(taskType);
            this.stageMethodMap = taskStageMethodMap.get(taskType);
            this.stageFlowMap = taskStageFlowMap.get(taskType);
        }

        public TaskStageResult invoke(String stageMethodName, Object... args) {
            Method method = stageMethodMap.get(stageMethodName);
            return invoke(method, args);
        }

        public TaskStageResult invoke(Method method, Object... args) {
            log.info("开始执行任务阶段: {}", method.getName());
            // 调用目标方法
            Object result = ReflectionUtils.invokeMethod(taskClass, method, args);
            if (method.isAnnotationPresent(TaskStage.class)){
                if (!(result instanceof TaskRet)) {
                    throw new IllegalArgumentException("自定义任务阶段方法的返回结果必须为TaskRet，请重新检查");
                } else {
                    return buildTaskStageMeta((TaskRet) result, method.getName());
                }
            } else {
                if (!(result instanceof TaskStageResult)) {
                    throw new IllegalArgumentException("自定义任务handle方法的返回结果必须为TaskStageResult，请重新检查");
                } else {
                    return (TaskStageResult) result;
                }
            }
        }

        /**
         * 构建任务阶段结果对象
         *
         * @param taskRet       当前阶段任务的返回数据
         * @param currStageName 当前的阶段方法
         */
        private TaskStageResult buildTaskStageMeta(TaskRet taskRet, String currStageName) {
            Object[] params = taskRet.getParams();
            String nextStageName = stageFlowMap.get(currStageName).get(0); // note 目前只是线性流转
            if (nextStageName.isEmpty()) {
                return new TaskStageResult(null, taskRet.getStageResult());
            }
            Class<?>[] paramsType = stageMethodMap.get(nextStageName).getParameterTypes();
            // 校验下一阶段的参数和参数类型是否匹配
            ReflectionUtils.checkParamsNum(params, paramsType);
            // 构建结果
            TaskStageMeta taskStageMeta = TaskStageMeta.builder()
                    .taskContext(new TaskContext(params, paramsType))
                    .nextTaskStage(nextStageName)
                    .build();
            return new TaskStageResult(taskStageMeta, taskRet.getStageResult());
        }
    }
}
