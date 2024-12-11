package cn.uhoc.domain.register;

import java.util.Set;

/**
 * @program: ChronAnt
 * @description: 负责加载和注册多阶段的异步任务
 * @author: chouchouGG
 * @create: 2024-12-09 01:25
 **/
public interface IMultiStageAsyncTaskRegistry {

    // 在任务类实例化后将其注册到 Map 中
    void init();

    Class<?> getTaskClass(String taskTypeName);

    Set<String> getTaskTypeSet();
}
