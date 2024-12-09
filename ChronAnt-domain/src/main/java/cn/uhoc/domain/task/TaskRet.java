package cn.uhoc.domain.task;

import cn.uhoc.domain.executor.model.entity.TaskSetStageEntity;
import lombok.Data;

@Data
public class TaskRet<T> {

    T result;

    TaskSetStageEntity asyncTaskSetStage;

    public TaskRet(T result) {
        this(result, null);
    }

    public TaskRet(T result, TaskSetStageEntity asyncTaskSetStage) {
        this.result = result;
        this.asyncTaskSetStage = asyncTaskSetStage;
    }

}
