import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
// @NoArgsConstructor :: (access=AccessLevel.PROTECTED) 속성을 부여하면, 기본 생성자로 객체를 만들 수 없으므로
// 무조건 필요한 인자를 저장하는 완전한 상태의 객체를 생성하게 강제할 수 있다.
@RequiredArgsConstructor // 생성자를 이용해서 의존성 주입을 받을 때, 필요한 스프링빈을 자동으로 넣어주도록 한다.
@Slf4j
public class AccountDto {
    private String accountNumber;
    private String nickname;
    private LocalDateTime registeredAt;

    public void log(){
        log.error("error is occured");
    }
}
