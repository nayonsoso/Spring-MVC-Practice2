package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;



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