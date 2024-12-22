package cn.uhoc.domain.manager.service;


import cn.uhoc.domain.manager.model.entity.TaskEntity;
import cn.uhoc.trigger.api.dto.TaskCreateReq;
import cn.uhoc.trigger.api.dto.TaskSetReq;

import java.util.List;

public interface ITaskService {

    /**
     * 创建任务
     * @return 返回创建的任务的task_id
     */
    String createTask(TaskCreateReq taskCreateReq);

    /**
     * 查询任务列表
     * @param taskType 任务类型
     * @param status 任务状态
     * @param limit 拉取任务数量限制
     * @return 任务列表
     */
    List<TaskEntity> getTaskList(String taskType, int status, int limit);


    /**
     * 根据任务id查询任务
     * @param taskId 任务id
     * @return 任务
     */
    TaskEntity getTask(String taskId);

    /**
     * 占据任务
     * @param taskType 任务类型
     * @param status 任务状态
     * @param limit 任务限制
     */
    List<TaskEntity> holdTask(String taskType, int status, int limit);


    /**
     * 更改任务信息
     * @param taskSetReq 任务设置DTO对象
     */
    void setTask(TaskSetReq taskSetReq);

    /**
     * 获取指定用户的任务列表
     * @param userId 用户id
     * @param statusList 状态列表
     * @return
     */
    List<TaskEntity> getTaskByUserIdAndStatus(String userId, int statusList);

}
