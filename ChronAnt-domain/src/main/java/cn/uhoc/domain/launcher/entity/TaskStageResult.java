package cn.uhoc.domain.launcher.entity;

/**
 * 多阶段异步任务中每个阶段完成后的执行结果的封装类
 */
public class TaskStageResult<T> {

    private TaskStageMeta metadata;

    /**
     * 阶段任务执行完的结果字段，由用户自定义，留给用户自定义扩展使用
     */
    private T result;

    public TaskStageResult() {}

    public TaskStageResult(TaskStageMeta metadata, T result) {
        this.metadata = metadata;
        this.result = result;
    }

    public TaskStageMeta getMetadata() {
        return metadata;
    }

    public T getResult() {
        return result;
    }

    public void setMetadata(TaskStageMeta metadata) {
        this.metadata = metadata;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
