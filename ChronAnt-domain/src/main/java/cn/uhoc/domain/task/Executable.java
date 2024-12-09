package cn.uhoc.domain.task;

import cn.uhoc.domain.executor.model.entity.TaskContextEntity;
import cn.uhoc.domain.executor.model.entity.TaskSetStageEntity;
import cn.uhoc.domain.scheduler.model.vo.TaskStatus;
import cn.uhoc.type.common.ReflectionUtils;

import java.lang.reflect.Method;

public interface Executable<T> {

    TaskRet<T> handleProcess();

    TaskRet<T> handleFinish();

    TaskRet<T> handleError();

    TaskRet<T> contextLoad(String context);

    default TaskSetStageEntity setStage(Class<?> clazz, String methodName, Object[] params, Class<?>[] parameterTypes, Object... envs) {
        return build(clazz, methodName, params, parameterTypes, envs);
    }


    // 利用类信息创建任务
    default TaskSetStageEntity build(Class<?> clazz, String methodName, Object[] params, Class<?>[] parameterTypes, Object... envs) {
        ReflectionUtils.checkParamsNum(params, parameterTypes);
        Method method = ReflectionUtils.getMethod(clazz, methodName, parameterTypes);

        // get 方法名
        String taskStage = method.getName();

        // 上下文信息
        TaskContextEntity taskContextEntity = new TaskContextEntity(params, parameterTypes, envs);
        return TaskSetStageEntity.builder()
                .status(TaskStatus.PENDING.getStatus())
                .taskContext(taskContextEntity)
                .taskStage(taskStage)
                .build();
    }

    default boolean judgeParamsTypes(Method clazzMethod, Class<?>[] parameterTypes) {
        Class<?>[] types = clazzMethod.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (types[i] != parameterTypes[i]) {
                return false;
            }
        }
        return true;
    }
}
