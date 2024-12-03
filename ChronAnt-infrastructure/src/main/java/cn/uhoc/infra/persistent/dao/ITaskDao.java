package cn.uhoc.infra.persistent.dao;

import cn.uhoc.domain.scheduler.model.entity.TaskEntity;
import cn.uhoc.infra.persistent.po.Task;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ITaskDao {

    void insertTask(String tableName, TaskEntity taskEntity);

    List<Task> getTaskList(String tableName, String taskType, int status, int limit);

    Task getTaskById(String taskId, String tableName);
}
