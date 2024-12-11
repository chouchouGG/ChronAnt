package test;

import cn.uhoc.domain.executor.model.entity.ScheduleLogEntity;
import cn.uhoc.domain.executor.model.entity.TaskContextEntity;
import cn.uhoc.domain.task.Executable;
import cn.uhoc.trigger.api.dto.TaskCreateReqDTO;
import cn.uhoc.type.common.ReflectionUtils;
import cn.uhoc.type.common.UserConfig;
import cn.uhoc.type.exception.ReflectionException;
import com.alibaba.fastjson.JSON;

import java.lang.reflect.Method;

/**
 * 用于构建任务传输对象
 */
public class TaskCreateBuilder {

    public static TaskCreateReqDTO build(Executable executable) throws NoSuchMethodException {
        Class<? extends Executable> aClass = executable.getClass();
        Method handProcess = aClass.getMethod("handleProcess");
        return TaskCreateBuilder.build(aClass, handProcess.getName(), new Object[0], new Class[0]);
    }

    // 利用类信息创建任务
    public static TaskCreateReqDTO build(Class<?> clazz, String methodName, Object[] params, Class<?>[] paramTypes, Object... envs) {
        if (!Executable.class.isAssignableFrom(clazz)) {
            throw new RuntimeException("The task must be implemented TaskDefinition!");
        }
        Method method;
        try {
            ReflectionUtils.checkParamsNum(params, paramTypes);
            method = ReflectionUtils.getMethod(clazz, methodName, paramTypes);
        } catch (ReflectionException e) {
            throw new RuntimeException(e);
        }

        // 获取类名
        String taskType = method.getDeclaringClass().getSimpleName();
        // get 方法名
        String taskStage = method.getName();
        // 调度日志
        ScheduleLogEntity sl = new ScheduleLogEntity();
        String scheduleLog = JSON.toJSONString(sl);

        // 上下文信息
        TaskContextEntity taskContextEntity = new TaskContextEntity(params, paramTypes, envs);
        String taskContext = JSON.toJSONString(taskContextEntity);
        return new TaskCreateReqDTO(UserConfig.USERID, taskType, taskStage, scheduleLog, taskContext);
    }

}
