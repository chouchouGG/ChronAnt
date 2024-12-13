package cn.uhoc.trigger.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-01 18:58
 **/
@Data
@Builder
public class TaskCreateRes {
    private String taskId;
}
