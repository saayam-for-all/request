package org.sfa.request.stepfunctions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.sfa.request.service.impl.VolunteerAssignmentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.sfa.request.stepfunctions.Part4WorkStore.Action.*;
import static org.sfa.request.stepfunctions.WorkflowFinalizationTask.Outcome.*;

@DataJpaTest(properties = "saayam.workflow.part4.status-writes-approved=true", showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({WorkflowFinalizationTask.class, VolunteerAssignmentServiceImpl.class,
        WorkflowFinalizationPostgresTest.ConfirmedDecisions.class})
@EnabledIfEnvironmentVariable(named = "ISSUE96_TEST_JDBC_URL",
        matches = "jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/request_issue_96")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Sql("/stepfunctions/part4-postgres-fixture.sql")
class WorkflowFinalizationPostgresTest {
    private static final String SCHEMA = "virginia_dev_saayam_rdbms.";

    @Autowired WorkflowFinalizationTask tasks;
    @Autowired JdbcTemplate jdbc;

    @DynamicPropertySource
    static void localDataSource(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () -> System.getenv("ISSUE96_TEST_JDBC_URL"));
        properties.add("spring.datasource.username", () -> "saayam");
        properties.add("spring.datasource.password", () -> "");
    }

    @Test
    void serializesCompetingWritesAndRollsBackAnUncommittedDecision() throws Exception {
        assertThrows(IllegalStateException.class, () -> tasks.apply(ADD_HELPERS, "req", "exec", "fail-mark"));
        assertEquals(List.of("lead"), assignedIds());

        assertEquals(List.of(HELPERS_ASSIGNED, HELPERS_ASSIGNED), concurrently(
                () -> tasks.apply(ADD_HELPERS, "req", "exec", "helpers"),
                () -> tasks.apply(ADD_HELPERS, "req", "exec", "helpers")));
        assertEquals(List.of("helper-1", "helper-2", "lead"), assignedIds());

        var outcomes = concurrently(() -> tasks.apply(COMPLETE, "req", "exec", "complete-a"),
                () -> tasks.apply(COMPLETE, "req", "exec", "complete-b"));
        assertEquals(1, outcomes.stream().filter(result -> result == RESOLVED).count());
        assertEquals(1, outcomes.stream().filter(result -> result == REJECTED).count());
        assertEquals(87, jdbc.queryForObject("select req_status_id from " + SCHEMA + "request", Integer.class));
        String winningDecision = outcomes.get(0) == RESOLVED ? "complete-a" : "complete-b";
        assertEquals(RESOLVED, tasks.apply(COMPLETE, "req", "exec", winningDecision));
        assertEquals(2, jdbc.queryForObject("select count(*) from " + SCHEMA + "part4_test_applied_decision", Integer.class));
    }

    private List<String> assignedIds() {
        return jdbc.queryForList("select volunteer_id from " + SCHEMA + "volunteers_assigned order by volunteer_id",
                String.class);
    }

    private static <T> List<T> concurrently(Callable<T> first, Callable<T> second) throws Exception {
        var workers = Executors.newFixedThreadPool(2);
        var ready = new CountDownLatch(2);
        try {
            var a = workers.submit(() -> startTogether(ready, first));
            var b = workers.submit(() -> startTogether(ready, second));
            return List.of(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS));
        } finally {
            workers.shutdownNow();
        }
    }

    private static <T> T startTogether(CountDownLatch ready, Callable<T> operation) throws Exception {
        ready.countDown();
        if (!ready.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Concurrent worker did not reach the starting point");
        }
        return operation.call();
    }

    @TestConfiguration
    static class ConfirmedDecisions {
        @Bean
        Part4WorkStore workStore(JdbcTemplate jdbc) {
            return new Part4WorkStore() {
                @Override
                public Optional<Decision> lockVerifiedDecision(String request, String execution, String decision) {
                    boolean applied = Boolean.TRUE.equals(jdbc.queryForObject(
                            "select exists (select 1 from " + SCHEMA + "part4_test_applied_decision where id = ?)",
                            Boolean.class, decision));
                    return switch (decision) {
                        case "fail-mark", "helpers" -> Optional.of(new Decision(ADD_HELPERS, "lead",
                                List.of("helper-1", "helper-2", "helper-1"), null, applied));
                        case "complete-a", "complete-b" -> Optional.of(new Decision(COMPLETE, "lead", null, null, applied));
                        default -> Optional.empty();
                    };
                }

                @Override
                public void markApplied(String request, String execution, String decision) {
                    if ("fail-mark".equals(decision)) {
                        throw new IllegalStateException("Simulated work ledger failure");
                    }
                    jdbc.update("insert into " + SCHEMA + "part4_test_applied_decision (id) values (?)", decision);
                }
            };
        }
    }
}
