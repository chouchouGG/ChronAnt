package cn.uhoc.domain.executor.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 更新任务某一阶段的信息封装，执行结束后会有一些任务相关信息需要保存
 */
@Data
@Builder
@AllArgsConstructor
public class TaskStageMeta {

    // ‘任务的阶段名’ 等价于 ‘任务阶段的方法名’。（当前任务阶段执行完成后，会设置下一任务阶段的方法名）
    private String taskStage;

//    // 任务状态
//    private TaskStatus taskStatus;

    // 阶段任务上下文。（对于上一阶段任务来说是下文，对于下一阶段任务而言是上文）
    private TaskContext taskContext;

}