package cn.uhoc.infra.persistent.dao;

import cn.uhoc.domain.manager.model.entity.TaskEntity;
import cn.uhoc.infra.persistent.po.Task;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ITaskDao {

    void insertTask(String tableName, TaskEntity taskEntity);

    List<Task> getTaskList(String tableName, String taskType, List<Integer> statusList, int limit);

    Task getTaskById(String taskId, String tableName);

    void updateStatusBatch(String tableName, List<String> taskIds, int status);

    void updateTask(TaskEntity taskEntity, List<Integer> statusList, String tableName);

    List<Task> getTaskByUseridAndStatus(String userId, List<Integer> statusList, String tableName);
}
