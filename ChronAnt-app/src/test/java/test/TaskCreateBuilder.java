package test;

import cn.uhoc.domain.executor.entity.ScheduleLog;
import cn.uhoc.domain.executor.entity.TaskContext;
import cn.uhoc.domain.task.Executable;
import cn.uhoc.trigger.api.dto.TaskCreateReq;
import cn.uhoc.type.common.ReflectionUtils;
import cn.uhoc.type.common.UserConfig;
import cn.uhoc.type.exception.ReflectionException;
import com.alibaba.fastjson.JSON;

import java.lang.reflect.Method;

/**
 * 用于构建任务传输对象
 */
public class TaskCreateBuilder {

    public static TaskCreateReq build(Executable executable) throws NoSuchMethodException {
        Class<? extends Executable> aClass = executable.getClass();
        Method handProcess = aClass.getMethod("handleProcess");
        return TaskCreateBuilder.build(aClass, handProcess.getName(), new Object[0], new Class[0]);
    }

    // 利用类信息创建任务
    public static TaskCreateReq build(Class<?> clazz, String methodName, Object[] params, Class<?>[] paramTypes, Object... envs) {
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
        ScheduleLog sl = new ScheduleLog();
        String scheduleLog = JSON.toJSONString(sl);

        // 上下文信息
        TaskContext taskContextEntity = new TaskContext(params, paramTypes, envs);
        String taskContext = JSON.toJSONString(taskContextEntity);
        return new TaskCreateReq(UserConfig.USERID, taskType, taskStage, scheduleLog, taskContext);
    }

}
