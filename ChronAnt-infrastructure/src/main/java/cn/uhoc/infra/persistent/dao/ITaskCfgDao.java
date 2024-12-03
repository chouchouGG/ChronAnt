package cn.uhoc.infra.persistent.dao;

import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ITaskCfgDao {


    TaskCfgEntity getTaskConfigByType(String taskType);
}
