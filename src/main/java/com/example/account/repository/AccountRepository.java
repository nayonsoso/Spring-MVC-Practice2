package com.example.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.account.domain.Account;
import org.springframework.stereotype.Repository;

// Account라는 테이블에 접속하기 위한 레퍼지토리
// 첫번째 타입은 레퍼지토리가 활용하게 될 엔티티(Account), 두번째 타입은 PK
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

}
