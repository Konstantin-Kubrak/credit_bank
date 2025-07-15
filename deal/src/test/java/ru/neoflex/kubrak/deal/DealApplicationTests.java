package ru.neoflex.kubrak.deal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.neoflex.kubrak.deal.testcontainer.AbstractPostgresBase;

@SpringBootTest
@Testcontainers
class DealApplicationTests extends AbstractPostgresBase {

    @Test
    void contextLoads() {
    }
}
