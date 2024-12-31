package cn.uhoc.domain.task;

import cn.uhoc.domain.launcher.entity.TaskContext;
import cn.uhoc.domain.launcher.entity.TaskStageMeta;
import cn.uhoc.domain.launcher.entity.TaskStageResult;
import cn.uhoc.type.common.ReflectionUtils;

import java.lang.reflect.Method;

public interface IExecutableAsyncTask<T> {

    /**
     * 执行任务的入口
     */
    TaskRet<T> handleProcess();

    /**
     * 任务执行完成时的处理逻辑
     */
    default TaskRet<T> handleFinish() {
        System.out.println("任务后置处理，可以自定义做点任务执行成功后的后置处理，例如回收资源等");
        return new TaskRet("任务后置处理，可以自定义做点任务执行成功后的后置处理，例如回收资源等");
    }

    /**
     * 任务执行出错时的处理逻辑
     */
    TaskStageResult<T> handleError();

    /**
     * 任务执行失败时，采取的兜底补偿措施
     */
    TaskStageResult<T> handleFailure();

    /**
     * 根据给定的上下文字符串加载任务上下文（可自定义加载逻辑），用户可以根据自己定义的协议格式对上下文进行解析
     *
     * @param context 上下文字符串
     * @return 加载后的任务上下文对象
     */
    TaskContext contextLoad(String context);

    /**
     * 利用类信息创建任务
     *
     * @param clazz          进行反射的Class对象
     * @param methodName     反射的方法名称，也是下一阶段任务的阶段名
     * @param params         下一阶段任务的参数
     * @param parameterTypes 下一阶段任务的参数类型
     * @return 返回下一阶段任务的元数据
     */
    default TaskStageMeta buildTaskStageMeta(Class<?> clazz, String methodName, Object[] params, Class<?>[] parameterTypes) {
        Method method;
        ReflectionUtils.checkParamsNum(params, parameterTypes);
        method = ReflectionUtils.getMethod(clazz, methodName, parameterTypes);
        // ‘方法名’ 等价于 ‘任务阶段名’
        String taskStage = method.getName();
        // 构建 ‘任务上下文’
        TaskContext taskContext = new TaskContext(params, parameterTypes);
        return TaskStageMeta.builder()
                .taskContext(taskContext)
                .nextTaskStage(taskStage)
                .build();
    }

}
