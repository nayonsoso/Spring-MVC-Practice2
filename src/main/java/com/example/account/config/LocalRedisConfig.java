package com.example.account.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class LocalRedisConfig {
    @Value("${spring.redis.port}") // application.yml에 적은 값을 가져와서 redisPort에 넣어주겠다는 뜻
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct // 빈으로 만들어진 직후 실행하겠다는 뜻
    public void startRedis() {
        redisServer = new RedisServer(redisPort);
        redisServer.start();
    }

    @PreDestroy // 빈이 종료되기 직전에 실행하겠다는 뜻
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}