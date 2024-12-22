package cn.uhoc;

import cn.uhoc.config.ThreadPoolConfigProp;
import cn.uhoc.domain.executor.service.ExecutorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ThreadPoolConfigProp.class})  // 显式注册配置类
public class Application implements CommandLineRunner {

    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }

    @Override
    public void run(String... args) {
        ExecutorService.getInstance().start();
    }
}
