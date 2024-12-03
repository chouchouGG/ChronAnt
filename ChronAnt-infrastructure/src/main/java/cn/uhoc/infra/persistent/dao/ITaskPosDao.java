package cn.uhoc.infra.persistent.dao;

import cn.uhoc.domain.scheduler.model.entity.TaskPosEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ITaskPosDao {


    TaskPosEntity getTaskPositionByType(String taskType);
}
