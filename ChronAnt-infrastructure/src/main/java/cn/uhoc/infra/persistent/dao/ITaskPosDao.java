package cn.uhoc.infra.persistent.dao;

import cn.uhoc.infra.persistent.po.TaskPos;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ITaskPosDao {


    TaskPos getTaskPositionByType(String taskType);

    List<TaskPos> getTaskPosList();
}
