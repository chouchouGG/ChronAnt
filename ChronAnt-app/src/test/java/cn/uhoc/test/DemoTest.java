package cn.uhoc.test;

import cn.uhoc.domain.task.Lark;
import cn.uhoc.trigger.api.dto.TaskCreateReq;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-29 16:03
 **/
@Slf4j
@SpringBootTest
public class DemoTest {

    @Test
    public void testJSON() {
        TaskCreateReq req = TaskCreateBuilder.build(new Lark());
        log.info("Json字符串为：{}", JSON.toJSONString(req));
    }

}
