package ru.neoflex.kubrak.deal.service;

import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.neoflex.kubrak.deal.dto.LoanOfferDto;
import ru.neoflex.kubrak.deal.dto.dtoMapper.LoanOfferMapper;
import ru.neoflex.kubrak.deal.model.entity.Statement;
import ru.neoflex.kubrak.deal.model.enums.ApplicationStatus;
import ru.neoflex.kubrak.deal.repository.ClientRepository;
import ru.neoflex.kubrak.deal.repository.CreditRepository;
import ru.neoflex.kubrak.deal.repository.StatementRepository;
import ru.neoflex.kubrak.deal.testcontainer.AbstractPostgresBase;
import ru.neoflex.kubrak.deal.util.EntityFactory;

import javax.sql.DataSource;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class StatementServiceConcurrencyTest extends AbstractPostgresBase {

    @Autowired
    LoanOfferMapper loanOfferMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StatementService statementService;

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private CreditRepository creditRepository;

    @BeforeAll
    static void init() throws Exception {
        sleep(2000);
    }

    @BeforeEach
    void setUp() throws Exception {

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:/db/changelog/changelog-master.xml");
        liquibase.setShouldRun(true);
        liquibase.afterPropertiesSet();
    }


    @Test
    void setStatementLoanOffer_shouldBlockConcurrentAccess() throws InterruptedException{

        Statement statement = EntityFactory.createTestStatement(UUID.randomUUID());
        clientRepository.saveAndFlush(statement.getClient());
        creditRepository.saveAndFlush(statement.getCredit());
        statementRepository.save(statement);

        LoanOfferDto offer1 = EntityFactory.createExpectedOffers().get(0);
        offer1.setStatementId(statement.getStatementId());

        LoanOfferDto offer2 = EntityFactory.createExpectedOffers().get(1);
        offer2.setStatementId(statement.getStatementId());

        CountDownLatch firstThreadAcquiredLock = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);
        AtomicBoolean secondThreadWasBlocked = new AtomicBoolean(false);
        AtomicReference<Exception> secondThreadException = new AtomicReference<>();

        Thread firstThread = new Thread(() -> transactionTemplate.executeWithoutResult(transactionStatus -> {
            try {
                statementService.setStatementLoanOffer(offer1);
                firstThreadAcquiredLock.countDown();
                releaseLock.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        Thread secondThread = new Thread(() -> {
            try {
                if (!firstThreadAcquiredLock.await(2, TimeUnit.SECONDS)) {
                    fail("First thread didn't acquire lock in time");
                }
                long startTime = System.currentTimeMillis();
                statementService.setStatementLoanOffer(offer2);
                long duration = System.currentTimeMillis() - startTime;
                if (duration > 1000) {
                    secondThreadWasBlocked.set(true);
                }
            } catch (Exception e) {
                secondThreadException.set(e);
            } finally {
                releaseLock.countDown();
            }
        });

        firstThread.start();
        Thread.sleep(500);
        secondThread.start();

        firstThread.join();
        secondThread.join();

        if (secondThreadException.get() != null) {
            throw new AssertionError("Second thread failed", secondThreadException.get());
        }

        assertTrue(secondThreadWasBlocked.get(),
                "Second thread should have been blocked by pessimistic lock.");

        Statement updatedStatement = statementRepository.findById(statement.getStatementId()).orElseThrow();
        assertEquals(ApplicationStatus.APPROVED, updatedStatement.getStatus());
    }
}