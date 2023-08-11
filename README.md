# Spring-MVC-Practice2

---

### 🔍 목차

- @Value
- yml 파일에서 민감한 정보를 숨기는 방법
- @Builder을 @AllArgsConstructor와 @NoArgsConstructor와 함께 써야하는 이유
- 엔티티의 어노테이션 (@Enumerated, @GeneratedValue)
- Redis의 SpinLock
- ❗Controller 테스트❗
- ❗Service 테스트❗

---

### @Value

> yml 또는 properties 에서 설정한 메타데이터를 가져와서 넣어준다.

yml, properties 파일은 애플리케이션에서 사용하는 설정값, 환경변수들을 관리하는 파일로 서버의 포트번호, 연결할 DB에 관한 정보, AWS의 연결 정보 등을 관리할 수 있다.

@Value 어노테이션은 이 파일에 있는 값을 코드에 넣어주는 역할을 한다.


```yaml
# resource/application.yml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
```

```java
// RedisRepositoryConfig.java
@Configuration
public class RedisRepositoryConfig {
    @Value("${spring.redis.host}") // 127.0.0.1
    private String redisHost;

    @Value("${spring.redis.port}") // 6379
    private int redisPort;

    @Bean
    public RedissonClient redissonClient() { // 여기서 리턴되는 레디슨 클라이언트를 빈으로 등록
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort);
        return Redisson.create(config);
    }
}
```

---

### yml파일에서 민감한 정보를 숨기는 방법

> Git에 DB연결 관련 정보같은 민감한 내용들이 그대로 올라가면 안되기 때문에 따로 처리를 해야한다.

- **application-?.yml 작성** 
  - 따로 관리할 내용이 담긴 파일을 생성한다. 
  - 이름은 반드시 `application-?.yml` 형식을 따라야 한다.
  - e.g. application-db.yml / application-s3.yml
- **application.yml에 포함 관계 설정**
  - spring.profiles.include로 포함할 yml 파일을 설정
  - 하나일 경우 `spring.profiles.include=secret`
  - 두개 이상일 경우 `spring:profiles.include: -db -s3`
- **.gitigroe 작성**

```yaml
# resource/application.tyml
spring:
  profiles.include: 
    -db 
    -s3
```
```gitignore
### gitignore

### yml
application-db.yml
application-s3.yml
```

---
### Reference

@Value 설명 참고 : https://variety82p.tistory.com/entry/Springboot-%EA%B0%9C%EB%B0%9C%ED%99%98%EA%B2%BD%EC%97%90%EC%84%9C-%EB%85%B8%EC%B6%9C%EB%90%98%EB%A9%B4-%EC%95%88%EB%90%98%EB%8A%94-%EC%84%A4%EC%A0%95%EA%B0%92-%ED%99%98%EA%B2%BD%EB%B3%80%EC%88%98-%EA%B4%80%EB%A6%AC%ED%95%98%EA%B8%B0

yml 설명 참고 : https://variety82p.tistory.com/entry/Springboot-%EA%B0%9C%EB%B0%9C%ED%99%98%EA%B2%BD%EC%97%90%EC%84%9C-%EB%85%B8%EC%B6%9C%EB%90%98%EB%A9%B4-%EC%95%88%EB%90%98%EB%8A%94-%EC%84%A4%EC%A0%95%EA%B0%92-%ED%99%98%EA%B2%BD%EB%B3%80%EC%88%98-%EA%B4%80%EB%A6%AC%ED%95%98%EA%B8%B0#-gitigroe%--%EC%-E%--%EC%--%B-
