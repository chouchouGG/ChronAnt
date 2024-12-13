package cn.uhoc.domain.executor.model.entity;

import lombok.Data;

/**
 * 多阶段异步任务中每个阶段完成后的执行结果的封装类
 */
@Data
public class TaskStageResult<T> {

    TaskStageMeta metadata;

    /**
     * 阶段任务执行完的结果字段，由用户自定义，留给用户自定义扩展使用
     */
    T result;

    public TaskStageResult(TaskStageMeta metadata, T result) {
        this.metadata = metadata;
        this.result = result;
    }
}
