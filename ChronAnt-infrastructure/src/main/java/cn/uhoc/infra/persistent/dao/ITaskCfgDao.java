package cn.uhoc.infra.persistent.dao;

import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import cn.uhoc.infra.persistent.po.TaskCfg;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ITaskCfgDao {


    TaskCfgEntity getTaskConfigByType(String taskType);

    List<TaskCfg> getTaskTypeCfgList();

    int save(TaskCfg taskCfg);

}
