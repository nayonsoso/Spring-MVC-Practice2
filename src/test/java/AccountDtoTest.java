import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountDtoTest {
    @Test
    void accountDto() {
        AccountDto accountDto = new AccountDto();

        // getter와 setter 테스트
        accountDto.setAccountNumber("123");
        System.out.println(accountDto.getAccountNumber());

        // toString 테스트
        System.out.println(accountDto.toString());

        // Slf4j 테스트
        accountDto.log();
    }
}