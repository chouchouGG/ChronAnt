package cn.uhoc.domain.scheduler.repository;

import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskPosEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-01 19:15
 **/
@Repository
public interface ITaskRepository {


    TaskPosEntity getTaskPositionByType(String taskType);

    TaskCfgEntity getTaskConfigByType(String taskType);

    void insertTask(String tableName, TaskEntity taskEntity);

//    List<TaskEntity> getTaskList(String tableName, String taskType, int status, int limit);

    TaskEntity getTaskById(String taskId, String tableName);

    List<TaskEntity> getTaskByUseridAndStatus(String userId, List<Integer> statusList, String tableName);

    List<TaskEntity> getTaskList(String tableName, String taskType, int status, int limit);

    void updateStatusBatch(String tableName, List<String> taskIds, int status);

    void updateTask(TaskEntity taskEntity, List<Integer> list, String tableName);
}
