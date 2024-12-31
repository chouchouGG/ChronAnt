package cn.uhoc.domain.manager.repository;

import cn.uhoc.domain.manager.model.entity.TaskCfgEntity;
import cn.uhoc.domain.manager.model.entity.TaskEntity;
import cn.uhoc.domain.manager.model.entity.TaskPosEntity;

import java.util.List;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-01 19:15
 **/
public interface ITaskRepository {

    TaskPosEntity getTaskPosByType(String taskType);

    TaskCfgEntity getTaskConfigByType(String taskType);

    void insertTask(String tableName, TaskEntity taskEntity);

    TaskEntity getTaskById(String taskId, String tableName);

    List<TaskEntity> getTaskByUseridAndStatus(String userId, List<Integer> statusList, String tableName);

    List<TaskEntity> getTaskList(String tableName, String taskType, List<Integer> statusList, int limit);

    void updateStatusBatch(String tableName, List<String> taskIds, int status);

    void updateTask(TaskEntity taskEntity, List<Integer> list, String tableName);

    List<TaskCfgEntity> getTaskTypeCfgList();

    void save(TaskCfgEntity taskCfgEntity);

    List<TaskPosEntity> getTaskPosList();

}
