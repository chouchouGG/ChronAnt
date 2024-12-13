package cn.uhoc;

import cn.uhoc.launcher.Launcher;
import cn.uhoc.launcher.Launch;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class Application implements CommandLineRunner {

    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }

    @Override
    public void run(String... args) throws Exception {
        // 启动 launcher
        Launch launcher = Launcher.getInstance();
        // 运行 launcher
        launcher.start();
    }
}
