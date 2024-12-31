package cn.uhoc.infra.persistent.dao;

import cn.uhoc.infra.persistent.po.TaskCfg;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ITaskCfgDao {


    TaskCfg getTaskConfigByType(String taskType);

    List<TaskCfg> getTaskTypeCfgList();

    int save(TaskCfg taskCfg);

}
