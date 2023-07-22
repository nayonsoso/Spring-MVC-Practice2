package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest { // 한번에 모든 테스트를 실행할 수도 있음
    @Mock
    private AccountRepository accountRepository;
    // AccountRepository 인스턴스를 가짜로 만들어서 accountRepository에 넣어줌

    @InjectMocks
    private AccountService accountService;
    // 가짜로 만든 accountRepository를 Service에 주입해줌

    @Test
    @DisplayName("계좌 조회 성공")
    void testXXX() {
        //given
        //given을 설계하는 방법 : 의존하는 클래스를 가짜로 만들어둔 것이므로 그 구성을 채워줄 필요가 있음
        //findByID를 설계 : Long이 주어졌을 때, 상태와 넘버가 이러한 Account를 리턴한다.
        given(accountRepository.findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("65789").build()));
        //when
        Account account = accountService.getAccount(45555L);

        //then
        assertEquals("65789", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
    }


    @BeforeEach
        // 각 테스트 전에 실행됨
    void init() {
        accountService.createAccount();
    }

    @Test
    void testGetAccount() {
        // id는 autoIncrement이므로 id==1인 계좌가 생성됨
        Account account = accountService.getAccount(1L);

        assertEquals("40000", account.getAccountNumber());
        assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
    }

    @Test
    void testGetAccount2() {
        // id는 autoIncrement이므로 id==2인 계좌가 생성됨
        Account account = accountService.getAccount(2L);

        assertEquals("40000", account.getAccountNumber());
        assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
    }
}



/*
// @SpringBootTest는 Spring-boot-starter-test에 포함된 기능
// 스프링 부트 기반 테스트를 돌릴 수 있게 해줌
// 즉, 실제로 스프링 부트가 돌아가듯이 모든 빈을 등록하여 스프링 컨테이너를 띄워줌 -> 자동 주입 가능
@SpringBootTest
class AccountServiceTest { // 한번에 모든 테스트를 실행할 수도 있음
    @Autowired
    private AccountService accountService;

    @BeforeEach // 각 테스트 전에 실행됨
    void init(){
        accountService.createAccount();
    }

    @Test
    void testGetAccount(){
        // id는 autoIncrement이므로 id==1인 계좌가 생성됨
        Account account = accountService.getAccount(1L);

        assertEquals("40000", account.getAccountNumber());
        assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
    }

    @Test
    void testGetAccount2(){
        // id는 autoIncrement이므로 id==2인 계좌가 생성됨
        Account account = accountService.getAccount(2L);

        assertEquals("40000", account.getAccountNumber());
        assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
    }
}
 */