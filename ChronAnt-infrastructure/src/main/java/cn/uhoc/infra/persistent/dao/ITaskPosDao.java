package cn.uhoc.infra.persistent.dao;

import cn.uhoc.domain.manager.model.entity.TaskPosEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ITaskPosDao {


    TaskPosEntity getTaskPositionByType(String taskType);
}
