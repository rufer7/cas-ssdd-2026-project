package ch.ssdd.eventhub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestSecurityConfig.class)
@ActiveProfiles("local")
class EventhubApplicationTests {

    @Test
    void contextLoads() {
    }
}
