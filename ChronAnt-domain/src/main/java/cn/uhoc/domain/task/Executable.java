package cn.uhoc.domain.task;

import cn.uhoc.domain.executor.model.entity.TaskContext;
import cn.uhoc.domain.executor.model.entity.TaskStageMeta;
import cn.uhoc.domain.executor.model.entity.TaskStageResult;
import cn.uhoc.type.common.ReflectionUtils;
import cn.uhoc.type.exception.ReflectionException;

import java.lang.reflect.Method;

public interface Executable<T> {

    /**
     * 执行任务的入口
     */
    TaskStageResult<T> handleProcess();

    /**
     * 任务执行完成时的处理逻辑
     */
    TaskStageResult<T> handleFinish();

    /**
     * 任务执行出错时的处理逻辑
     */
    TaskStageResult<T> handleError();

    /**
     * 根据给定的上下文字符串加载任务上下文（可自定义加载逻辑）
     * @param context 上下文字符串
     * @return 加载后的任务上下文对象
     */
    TaskContext contextLoad(String context);

    // 利用类信息创建任务
    default TaskStageMeta buildTaskStageMeta(Class<?> clazz, String methodName, Object[] params, Class<?>[] parameterTypes, Object... envs) {
        Method method;
        try {
            ReflectionUtils.checkParamsNum(params, parameterTypes);
            method = ReflectionUtils.getMethod(clazz, methodName, parameterTypes);
        } catch (ReflectionException e) {
            throw new RuntimeException(e);
        }
        // ‘方法名’ 等价于 ‘任务阶段名’
        String taskStage = method.getName();
        // 构建 ‘任务上下文’
        TaskContext taskContext = new TaskContext(params, parameterTypes, envs);
        return TaskStageMeta.builder()
                // 任务状态不应该放在这里设置
//                .taskStatus(TaskStatus.PENDING)
                .taskContext(taskContext)
                .taskStage(taskStage)
                .build();
    }

}
