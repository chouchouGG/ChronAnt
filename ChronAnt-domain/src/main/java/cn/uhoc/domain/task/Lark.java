package cn.uhoc.domain.task;

import cn.uhoc.domain.launcher.entity.TaskContext;
import cn.uhoc.domain.launcher.entity.TaskStageResult;
import cn.uhoc.domain.register.MultiStageAsyncTask;
import cn.uhoc.domain.register.TaskStage;
import com.alibaba.fastjson.JSON;

// 测试任务
@MultiStageAsyncTask(taskType = "Lark")
public class Lark implements IExecutableAsyncTask {

    @TaskStage(nextStage = "taskPhase2")
    public TaskRet taskPhase1(String msg) {
        System.out.println("第一阶段开启打印: " + msg);
        return new TaskRet("第一阶段执行成功", "我是英雄！");
    }

    @TaskStage(nextStage = "taskPhase3")
    public TaskRet taskPhase2(String msg) {
        System.out.println("第二阶段开启打印: " + msg);
        return new TaskRet("第二阶段执行成功", 7911);
    }

    @TaskStage(nextStage = "handleFinish")
    public TaskRet taskPhase3(Integer num) {
        System.out.println("第三阶段开启打印: " + num * 100);
        return new TaskRet("第三阶段执行成功");
    }

    // 入口方法
    @TaskStage(nextStage = "taskPhase1")
    @Override
    public TaskRet handleProcess() {
        // 任务的初始参数
        return new TaskRet("嘻嘻");
    }

    @TaskStage
    @Override
    public TaskRet handleFinish() {
        System.out.println("任务后置处理，可以自定义做点任务执行成功后的后置处理，例如回收资源等");
        return new TaskRet("全部任务阶段执行完毕~");
    }

    @Override
    public TaskStageResult handleError() { // fixme 返回值还未修改
        System.out.println("任务实在是执行不了了，还是人工检查一下吧~");
        return new <String>TaskStageResult(null, "任务实在是执行不了了，还是人工检查一下吧~");
    }

    @Override
    public TaskStageResult handleFailure() {
        System.out.println("失败了就失败吧，失败是成功之母！");
        return new <String>TaskStageResult(null, "失败了就失败吧，失败是成功之母！");
    }

    @Override
    public TaskContext contextLoad(String context) {
        return JSON.parseObject(context, TaskContext.class);
    }

}
