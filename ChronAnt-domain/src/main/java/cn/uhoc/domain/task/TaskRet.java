package cn.uhoc.domain.task;

import lombok.Data;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-30 16:42
 **/
@Data
public class TaskRet<T> {
    private T stageResult;
    private Object[] params;

    public TaskRet(T stageResult, Object... params) {
        this.stageResult = stageResult;
        this.params = params;
    }
}
