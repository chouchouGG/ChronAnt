package cn.uhoc.infra.convertor;

import cn.uhoc.domain.scheduler.model.entity.TaskEntity;
import cn.uhoc.infra.persistent.po.Task;
import org.mapstruct.Mapper;


/**
 * <p>任务的Entity实体和PO对象转换</p>
 * <a href="https://blog.csdn.net/weixin_47324958/article/details/128172458">MapStruct使用简易教程，点击链接访问</a>
 */
@Mapper
public interface ITaskConvertor {

    /**
     * PO 转 Entity
     */
    TaskEntity toEntity(Task task);

    /**
     * Entity 转 PO
     */
    Task toPO(TaskEntity taskEntity);
}
