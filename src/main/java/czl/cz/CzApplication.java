package czl.cz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("czl.cz.dao")
@EnableTransactionManagement(proxyTargetClass = true)
public class CzApplication {

    public static void main(String[] args) {
        SpringApplication.run(CzApplication.class, args);
    }

}
