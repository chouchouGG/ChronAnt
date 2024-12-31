package cn.uhoc.test;

import cn.uhoc.domain.launcher.entity.ScheduleLog;
import cn.uhoc.domain.launcher.entity.TaskContext;
import cn.uhoc.domain.task.IExecutableAsyncTask;
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

    public static TaskCreateReq build(IExecutableAsyncTask IExecutableAsyncTask) {
        Class<? extends IExecutableAsyncTask> aClass = IExecutableAsyncTask.getClass();
        Method handleProcess = ReflectionUtils.getMethod(aClass, "handleProcess", new Class[0]);
        return TaskCreateBuilder.build(aClass, handleProcess.getName(), new Object[0], new Class[0]);
    }

    // 利用类信息创建任务
    public static TaskCreateReq build(Class<?> clazz, String methodName, Object[] params, Class<?>[] paramTypes, Object... envs) {
        if (!IExecutableAsyncTask.class.isAssignableFrom(clazz)) {
            throw new RuntimeException("The task must be implemented TaskDefinition!");
        }
        Method method;
        ReflectionUtils.checkParamsNum(params, paramTypes);
        method = ReflectionUtils.getMethod(clazz, methodName, paramTypes);
        // 获取类名
        String taskType = method.getDeclaringClass().getSimpleName();
        // get 方法名
        String taskStage = method.getName();
        // 调度日志
        ScheduleLog sl = new ScheduleLog();
        String scheduleLog = JSON.toJSONString(sl);
        // 上下文信息
        TaskContext taskContextEntity = new TaskContext(params, paramTypes);
        String taskContext = JSON.toJSONString(taskContextEntity);
        return new TaskCreateReq(UserConfig.USERID, taskType, taskStage, scheduleLog, taskContext);
    }

}
