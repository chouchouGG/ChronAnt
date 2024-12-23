package cn.uhoc.domain.register.impl;


import cn.uhoc.domain.register.IMultiStageAsyncTaskRegistry;
import cn.uhoc.domain.register.anno.MultiStageAsyncTask;
import cn.uhoc.type.enums.ExceptionStatus;
import cn.uhoc.type.exception.E;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MultiStageAsyncTaskRegister implements IMultiStageAsyncTaskRegistry {

    // 存储所有带有 @MultiStageAsyncTask 注解的类的 Class 对象
    private final Map<String, Class<?>> taskTypeMap = new ConcurrentHashMap<>();

    @Resource
    private ApplicationContext applicationContext;

    // 在任务类实例化后将其注册到 Map 中
    @Override
    public void init() {
        // 扫描 Spring 容器中的所有 Bean，找出带有 @MultiStageAsyncTask 注解的类
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(MultiStageAsyncTask.class);
        // 遍历所有带有 @MultiStageAsyncTask 注解的 Bean，将其 Class 对象注册到 Map 中
        for (Object bean : beansWithAnnotation.values()) {
            Class<?> clazz = bean.getClass();
            MultiStageAsyncTask annotation = clazz.getAnnotation(MultiStageAsyncTask.class);
            taskTypeMap.put(clazz.getSimpleName(), clazz);  // 注册类的 Class 对象
            log.info("Registered task: {}", clazz.getSimpleName());
        }
    }

    @Override
    public Class<?> getTaskClass(String taskTypeName) {
        Class<?> taskClazz = taskTypeMap.get(taskTypeName);
        if (taskClazz == null) {
            log.error("Class for task type not found, task type name: {}, task class map: {}", taskTypeName, JSON.toJSONString(taskTypeMap));
            throw new E(ExceptionStatus.NO_CLASS_FOR_TASK_TYPE);
        }
        return taskClazz;
    }

    @Override
    public Set<String> getTaskTypeSet() {
        return this.taskTypeMap.keySet();
    }


}
