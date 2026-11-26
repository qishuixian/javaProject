package learning.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 扫描当前包和子包中的组件，并根据依赖启用自动配置。
@SpringBootApplication
public class Day33Application {
    public static void main(String[] args) {
        SpringApplication.run(Day33Application.class, args);
    }
}
