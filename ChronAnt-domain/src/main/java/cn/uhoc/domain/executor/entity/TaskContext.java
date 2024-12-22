package cn.uhoc.domain.executor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * task_context的解析类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskContext {

    private Object[] params;

    private Class<?>[] paramsClazz;

    private Object[] envs; // 留作扩展使用

}