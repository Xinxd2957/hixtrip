package com.hixtrip.sample;

import org.mybatis.spring.annotation.MapperScan;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication(scanBasePackages = {"com.hixtrip"})
@MapperScan(basePackages = {"com.hixtrip.sample.infra.db.mapper"})
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    /**
     * 使用redisson
     * @return
     */
    @Bean
    public Redisson redisson (){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.0.0.1:8080").setDatabase(0);  //模拟
        return (Redisson)Redisson.create(config);
    }

}
