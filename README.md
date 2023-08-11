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

### 📌 @Value

> 💡 yml 또는 properties 에서 설정한 메타데이터를 가져와서 넣어준다.

yml, properties 파일은 애플리케이션에서 사용하는 설정값, 환경변수들을 관리하는 파일로

서버의 포트번호, 연결할 DB에 관한 정보, AWS의 연결 정보 등을 관리할 수 있다.

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

###  📌 yml파일에서 민감한 정보를 숨기는 방법

> 💡 Git에 DB연결 관련 정보같은 민감한 내용들이 그대로 올라가면 안되기 때문에 따로 처리를 해야한다.

- **1) application-?.yml 작성** 
  - 따로 관리할 내용이 담긴 파일을 생성한다. 
  - 이름은 반드시 `application-?.yml` 형식을 따라야 한다.
  - e.g. application-db.yml / application-s3.yml
- **2) application.yml에 포함 관계 설정**
  - spring.profiles.include로 포함할 yml 파일을 설정
  - 하나일 경우 `spring.profiles.include=secret`
  - 두개 이상일 경우 `spring:profiles.include: -db -s3`
- **3) .gitignore 작성**

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

### 📌 @Builder을 @NoArgsConstructor와 @AllArgsConstructor와 함께 써야하는 이유

> 💡 Builder 패턴에는 전체 생성자가 필요한데,
> `@NoArgsConstructor`를 쓰면 전체 생성자가 자동 생성되지 않으므로 
> `@AllArgsConstructor`와 함께 써야 함

우선 Builder 패턴에 대한 이해가 필요하다. Builder 패턴이 어떻게 구현되어 있는지 보면 전체 생성자가 필요하다는 것을 알 수 있다.

***빌더 패턴은 파라미터를 1개씩 갖는 각각의 생성자가 필요하기 때문에 모든 파라미터를 갖는 전체 생성자가 필요하다.***

이를 위해서 어떠한 생성자도 없으면 빌더 안에서 자동으로 전체 생성자를 만들어주는 기능을 제공한다.
***하지만 기본 생성자 또는 다른 생성자가 하나라도 존재한다면 이 기능은 작동하지 않는다.***
따라서 이때는 직접 전체 생성자를 만들어주어야 한다.
그러므로 @Builder 하나만 작성하면, 빌더를 위한 전체 생성자가 자동으로 만들어지므로 어떠한 오류도 발생하지 않는다.

하지만 무분별하게 생성되는 객체들을 한 번 더 체크하고, 의도하지 않은 객체를 만드는 것을 방지하기 위해
`@NoArgsConstructor(access=AccessLevel.PROTECTED)`를 사용하는 경우에는 오류가 발생한다.
NoArgsConstructor가 어떤 파라미터도 없는 기본 생성자를 만들어주기 때문이다.
앞서 말했던 Builder의 '전체 생성자 자동 생성 원칙'을 기억하는가?

**_어떤 생성자도 없을 때에만 전체 생성자를 만들어주는데,
NoArgsConstructor가 기본 생성자를 만들므로 전체 생성자가 생성되지 않는다._**
따라서 전체 생성자를 만들기 위해 @AllArgsConstructor를 함께 선언해줘야 한다.
이러한 원리를 배경으로 @Builder 어노테이션을 쓸 때는 @AllArgsConstructor와 @NoArgsConstructor를 써주는게 좋다.
(@Builder만 써도 되긴 하지만, 관행적으로 써주는게 좋다.)

---

### 📌 엔티티의 어노테이션 - @Id, @GeneratedValue

> 💡 @Id
- 테이블의 PK 와 객체의 필드를 매핑시켜주는 어노테이션
- @GeneratedValue 없이 @Id 만 사용하는 경우 기본키를 직접 할당해줘야 한다.
> 💡 @GeneratedValue
- DB가 **자동 생성**하는 값을 PK 로 만들어주는 어노테이션
- 속성으로는 strategy가 있는데, 이를 통해 자동 생성 전략을 지정해 줄 수 있다.
  - 속성의 종류 : IDENTITY, SEQUNCE, TABLE, AUTO
  - 이 중에 가장 많이 쓰이는 **strategy = GenerationType.IDENTITY** 옵션은 DB에 PK 생성을 위임하는 전략이다.
  - 주로 auto_increment되는 PK를 만들기 위해 사용된다.
    - 원리 : DDL로 PK를 auto_increment로 설정해놓고 Entity의 PK 생성을 DB에 위임하면, auto_increment를 따르는 PK가 저장됨

```java
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder // Builder를 쓰려면 @AllArgsConstructor와 @NoArgsConstructor가 있어야함
@Entity
public class Account { // Entity : 자바 객체처럼 보이지만 실제로는 설정 파일
    @Id // 테이블의 PK로 설정한다는 의미
    @GeneratedValue // PK가 autoIncrement됨
    private Long id;

    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatusStr;

    @Enumerated(EnumType.ORDINAL)
    private AccountStatus accountStatusInt;
}
```

---
### 📌 엔티티의 어노테이션 - @Enumerated


> 💡 @Enumerated(EnumType.?)

자바 enum 타입을 엔티티 클래스의 속성으로 사용할 수 있다. @Enumerated 애노테이션에는 두 가지 EnumType이 존재한다.

- EnumType.ORDINAL : enum 순서 값을 DB에 저장
- EnumType.STRING : enum 이름을 DB에 저장

예를 들어 enum이 다음과 같이 생겼다고 하자.

``` java
public enum AccountStatus {
IN_USE,
UNREGISTERED
}
```

**▶ EnumType.ORDINAL**

``` java
@Enumerated(EnumType.ORDINAL)
private AccountStatus accountStatusInt;
```

EnumType을 ORDINAL으로 지정하면 ENUM에서 선언된 `순서`에 해당하는 정수가 저장된다.
IN_USE는 1이, UNREGISTERED은 2가 저장된다.

**▶ EnumType.STRING**

``` java
@Enumerated(EnumType.STRING)
private AccountStatus accountStatusStr;
```

STRING으로 지정하면 "IN_USE", "UNREGISTERED" 문자열 자체가 저장된다.

**▶ 테이블 구조**

이렇게 만들어준 엔티티의 테이블 구조는 아래와 같다.

```sql
create table account (
   id bigint not null,
    account_number varchar(255),
    account_status_int integer,
    account_status_str varchar(255),
    primary key (id)
)
```
---

### 📌 Controller 테스트



---

### ❗Service 테스트❗







### Reference

@Value 설명 참고 : https://variety82p.tistory.com/entry/Springboot-%EA%B0%9C%EB%B0%9C%ED%99%98%EA%B2%BD%EC%97%90%EC%84%9C-%EB%85%B8%EC%B6%9C%EB%90%98%EB%A9%B4-%EC%95%88%EB%90%98%EB%8A%94-%EC%84%A4%EC%A0%95%EA%B0%92-%ED%99%98%EA%B2%BD%EB%B3%80%EC%88%98-%EA%B4%80%EB%A6%AC%ED%95%98%EA%B8%B0

yml 설명 참고 : https://variety82p.tistory.com/entry/Springboot-%EA%B0%9C%EB%B0%9C%ED%99%98%EA%B2%BD%EC%97%90%EC%84%9C-%EB%85%B8%EC%B6%9C%EB%90%98%EB%A9%B4-%EC%95%88%EB%90%98%EB%8A%94-%EC%84%A4%EC%A0%95%EA%B0%92-%ED%99%98%EA%B2%BD%EB%B3%80%EC%88%98-%EA%B4%80%EB%A6%AC%ED%95%98%EA%B8%B0#-gitigroe%--%EC%-E%--%EC%--%B-

@Builder 설명 참고 : https://resilient-923.tistory.com/418
