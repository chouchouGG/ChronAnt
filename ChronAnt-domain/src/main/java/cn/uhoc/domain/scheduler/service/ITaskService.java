package cn.uhoc.domain.scheduler.service;


import cn.uhoc.trigger.api.dto.TaskCreateReqDTO;
import cn.uhoc.trigger.api.dto.TaskResDTO;
import cn.uhoc.type.model.R;

public interface ITaskService {

    /**
     * 创建任务
     * @return 返回创建的任务的task_id
     */
    String createTask(TaskCreateReqDTO taskCreateReqDTO);

//    /**
//     * 查询任务列表
//     * @param taskType 任务类型
//     * @param status 任务状态
//     * @param limit 拉取任务数量限制
//     * @return 任务列表
//     */
//    List<TaskEntity> getTaskList(String taskType, int status, int limit);
//
//    /**
//     * 更改任务信息
//     * @param TaskSetRequest
//     * @param
//     * @return
//     */
//    void setTask(TaskSetReqDTO TaskSetRequest);

    /**
     * 根据任务id查询任务
     * @param taskId 任务id
     * @return 任务
     */
    TaskResDTO getTask(String taskId);
}
