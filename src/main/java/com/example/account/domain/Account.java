package com.example.account.domain;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder // Builder를 쓰려면 @AllArgsConstructor와 @NoArgsConstructor가 있어야 문제없이 상속받을 수 있음
@Entity
public class Account { // Entity : 자바 객체처럼 보이지만 실제로는 설정 파일
    @Id // Account라는 테이블의 PK로 설정한다는 의미
    @GeneratedValue // 이 어노테이션에 의해서 PK가 autoIncrement할 수 있음
    private Long id;

    private String accountNumber;

    @Enumerated(EnumType.STRING) // enum은 내부적으로 0,1,2 ... 로 저장되는데, 이러면 의미를 알 수 없으므로 String Type으로 설정
    private AccountStatus accountStatus;
}
