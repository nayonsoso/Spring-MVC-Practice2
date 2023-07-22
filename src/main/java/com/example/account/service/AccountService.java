package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

// Account Repository를 활용해서 데이터를 저장하는 서비스
@Service
@RequiredArgsConstructor // 꼭 필요한 인자(private final)가 들어간 생성자를 만들어줌 (생성자 삽입)
public class AccountService {
    private final AccountRepository accountRepository;

    @Transactional
    public void createAccount(){
        // Account에 @Builder 어노테이션을 사용했기 때문에 이런 문법을 사용할 수 있음
        Account account = Account.builder()
                .accountNumber("40000")
                .accountStatus(AccountStatus.IN_USE)
                .build();
        accountRepository.save(account); // 이 save는 어디서 튀어나온거지?
    }

    @Transactional
    public Account getAccount(Long id){
        // findById가 Optional을 리턴하기 때문에 바로 get하는게 사실 권장되진 않음
        // null이 있을 수 있으므로
        return accountRepository.findById(id).get();
    }
}
