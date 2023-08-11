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

> 💡 **@Id**
- 테이블의 PK 와 객체의 필드를 매핑시켜주는 어노테이션
- @GeneratedValue 없이 @Id 만 사용하는 경우 기본키를 직접 할당해줘야 한다.
> 💡 **@GeneratedValue**
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


> 💡 **@Enumerated(EnumType.?)**

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

컨트롤러를 테스트하기 위해선 크게 두가지 방법이 있다.

> **💡 @SpringBootTest + @AutoConfigureMockMvc**
  - @SpringBootTest는 프로젝트 내부의 ***모든 빈을 등록*** 한다.
  - @AutoConfigureMockMvc은 Mock ***테스트에 필요한 의존성을 제공*** 해준다.
  - 단위 테스트같은 기능 검증이 아니라 **_전체 Flow가 제대로 동작하는지 테스트하기 위해_** 쓰인다.
  - 장점 : 어플리케이션의 설정, 모든 빈을 로드하기 때문에 운영 환경과 유사한 테스트가 가능하다. 
  - 단점 : 모든 빈을 등록하기 때문에 ***시간이 오래*** 걸리고, 테스트 단위가 크기 때문에 디버깅이 힘들다.

> **💡 @WebMvcTest + @MockBean**
  - MVC를 위한 테스트, 특히 Controller를 테스트 할 때 사용된다. (요즘 더 선호하는 방식)
  - 모든 빈이 아니라 ***WebApplication 관련 빈만 로드*** 하므로 빠르고 가벼운 테스트가 가능하다.
    - e.g. Controller, ControllerAdvice, Converter, Filter, HandlerInterceptor 등
    - Service 등 Controller에서 의존하는 _**하위 레이어의 기능은 @MockBean을 통해 원하는 동작을 설정할 수 있다.**_
    - 예를들어, 결제 API에서 결제가 실패하는 Service 객체를 직접 설계하여 테스트할 수 있다.

> 💡 **MockMvc**
- MockMvc는 서버에 애플리케이션을 올리지 않고도 (서블릿 컨테이너를 사용하지 않고) MVC 환경을 만들어 요청, 응답기능을 제공해주는 유틸리티 클래스다. 
- MockMvc를 이용해서 테스트하는 방법 : 
  - 1)) MockMvc를 생성한다. 이때 @Autowired 로 주입받아야 한다.
  - 2)) MockMvc에게 요청에 대한 정보를 입력한다. e.g. `mockMvc.perform(get("url"))`
    - post로 전송할 때 빌더 패턴으로 `.content()`으로 body에 값을 넘겨서 전송할 수도 있다.
  - 3)) 요청에 대한 응답값을 Expect를 이용하여 테스트한다. e.g. `andExpect(jsonPath("$.id").value("1"))`
    - Expect가 모두 통과하면 테스트 통과 
    - Expect가 1개라도 실패하면 테스트 실패

```java
@WebMvcTest(AccountController.class) // 테스트할 클래스를 지정
class AccountControllerTest {
    // 컨트롤러가 의존하는 하위 레이어 빈은 MockBean으로 주입
    @MockBean
    private AccountService accountService;

    @MockBean
    private RedisTestService redisTestService;
    
    // MockMvc는 @Autowired로 주입
    @Autowired
    private MockMvc mockMvc;

    @Test
    void successGetAccount() throws Exception {
        // given - accountService.getAccount() 메서드 실행 시 반환하는 값을 설정
        given(accountService.getAccount(anyLong()))
                .willReturn(Account.builder()
                        .accountNumber("3456")
                        .accountStatusStr(AccountStatus.IN_USE)
                        .build()); // 어떤 인자를 주더라도, 계좌 번호가 3456이고 사용중인 계좌를 리턴함

        // then - get 방식으로 요청을 하고, 그 값이 Expect했던 것이 맞는지 테스트
        mockMvc.perform(get("/account/1111"))
                .andDo(print())
                .andExpect(jsonPath("$.accountNumber").value("3456")) // $ 기호는 json 객체의 루트
                .andExpect(jsonPath("$.accountStatus").value("IN_USE"))
                .andExpect(status().isOk());
    }
}
```

---

### 📌 Service 테스트

> 💡 **@ExtendWith(MockitoExtension.class)**

- 서비스 레이어만 테스트를 하고 싶으므로, 유닛 테스트를 해줘야 한다. 
- 이때 `@ExtendWith(MockitoExtension.class)`를 테스트 클래스에 선언해줌으로써 유닛 테스트가 가능하다. 
- 이 어노테이션은 모키토 클래스를 이용해서 유닛 테스트를 하겠다는 어노테이션이다.

> 💡 **@Mock, @InjectMocks**

- 유닛 테스트는 다른 레이어에 독립적이여야 하므로 의존하는 객체를 mock 객체로 만들어 사용해야 한다. 
- 이때 `@Mock` 어노테이션을 사용해서 의존하려는 객체를 mock 객체로 만들 수 있다. 
- 이렇게 만든 mock 객체를 `@InjectMocks` 를 통해 주입받을 수 있다.

> 💡 **검증 방법 - verify(횟수 검증)**
- verify
  - 의존하고 있는 Mock이 해당되는 동작을 몇번 수행했는지 확인하는 검증
    - cf. Service Test 입장에서 의존하고 있는 Mock은 Repository
  - 형식 : `verify({의존하는 목객체}, times({수행한 횟수})).{수행한 메소드}({메소드 인자});`
  - 예시 : `verify(accountRepository, times(1)).save(any<Account>());`

> 💡 **검증 방법 - ArgumentCaptor(인자 검증)**
- ArgumentCaptor 
  - 메소드에 들어가는 인자를 받아올 수 있음
  - interaction을 기록하는 객체이기 때문에, ArgumentCaptor 선언 전에 실행된 함수의 인자값도 가져올 수 있음 (어떤 원리인지는 모르겠음)
  - 사용 방법 :
    - 1)) 무엇을 가로챌 captor인지 선언 : `ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);`
    - 2)) 인자를 받아올 메서드의 ( )안에 captor.capture()을 넣기 : `verify(accountRepository, times(1)).save(captor.capture());`
      - 위 예시처럼 verify 구문 내에서 사용 가능
    - 3)) 캡쳐한 값이 예상한 값과 같은지 검증 : `assertEquals("1234", captor.getValue().getAccountNumber());`

```java
@ExtendWith(MockitoExtension.class) // 모키토를 이용한 유닛테스트
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks // @Mock으로 만든 목객체를 주입할 객체
    private AccountService accountService;

    @Test
    @DisplayName("계좌 조회 성공")
    void getAccount() {
        // given
        given(accountRepository.findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountStatusStr(AccountStatus.UNREGISTERED)
                        .accountNumber("65789").build()));
        // when
        Account account = accountService.getAccount(45555L);

        // test
        // Long 자료형(조회되는 ID)를 저장할 captor
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        // accountService.getAccount를 하면서 accountRepository.findById() 가 한번 실행되었는가
        verify(accountRepository, times(1)).findById(captor.capture());
        // accountService.getAccount를 하면서 accountRepository.save() 가 실행되진 않았는가
        verify(accountRepository, times(0)).save(any());
        // findById를 호출할 때 사용한 인자 검증 : captor에 저장된 값을 다양한 assert로 검증
        assertEquals(45555L, captor.getValue());
        assertNotEquals(55555L, captor.getValue());
        assertTrue(45555L == captor.getValue());

        // then
        assertEquals("65789", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatusStr());
    }
}

```

---

### 📌 정리 : 상황별 테스트 방법

| Spring MVC Test | 단위 테스트(Mokito)   | 통합 테스트         |
|----------|--------------------------|--------------------------|
| @WebMvcTest  | @ExtendWith(MokitoExtension.class)    | @SpringBootTest   |

---

### Reference

@Value 설명 참고 : https://variety82p.tistory.com/entry/Springboot-%EA%B0%9C%EB%B0%9C%ED%99%98%EA%B2%BD%EC%97%90%EC%84%9C-%EB%85%B8%EC%B6%9C%EB%90%98%EB%A9%B4-%EC%95%88%EB%90%98%EB%8A%94-%EC%84%A4%EC%A0%95%EA%B0%92-%ED%99%98%EA%B2%BD%EB%B3%80%EC%88%98-%EA%B4%80%EB%A6%AC%ED%95%98%EA%B8%B0

yml 설명 참고 : https://variety82p.tistory.com/entry/Springboot-%EA%B0%9C%EB%B0%9C%ED%99%98%EA%B2%BD%EC%97%90%EC%84%9C-%EB%85%B8%EC%B6%9C%EB%90%98%EB%A9%B4-%EC%95%88%EB%90%98%EB%8A%94-%EC%84%A4%EC%A0%95%EA%B0%92-%ED%99%98%EA%B2%BD%EB%B3%80%EC%88%98-%EA%B4%80%EB%A6%AC%ED%95%98%EA%B8%B0#-gitigroe%--%EC%-E%--%EC%--%B-

@Builder 설명 참고 : https://resilient-923.tistory.com/418
