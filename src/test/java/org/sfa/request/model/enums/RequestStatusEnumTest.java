package org.sfa.request.model.enums;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestStatusEnumTest {

    private static final Pattern SEEDED_STATUS = Pattern.compile("\\((\\d+),\\s*'([A-Z_]+)'");

    @Test
    void preservesStableIdsAndAppendsWorkflowStatuses() {
        assertEquals(0, RequestStatusEnum.UNSPECIFIED.getId());
        assertEquals(1, RequestStatusEnum.CREATED.getId());
        assertEquals(2, RequestStatusEnum.PENDING_VOLUNTEER_ASSIGNMENT.getId());
        assertEquals(3, RequestStatusEnum.IN_PROGRESS.getId());
        assertEquals(4, RequestStatusEnum.COMPLETED.getId());
        assertEquals(5, RequestStatusEnum.CANCELLED.getId());
        assertEquals(6, RequestStatusEnum.DELETED.getId());
        assertEquals(7, RequestStatusEnum.RATED_BY_REQUESTER.getId());
        assertEquals(8, RequestStatusEnum.RATED_BY_VOLUNTEER.getId());
        assertEquals(9, RequestStatusEnum.VOLUNTEER_NOT_FOUND.getId());
        assertEquals(10, RequestStatusEnum.REASSIGNMENT_REQUESTED.getId());
    }

    @Test
    void hasUniqueIdsAndNames() {
        RequestStatusEnum[] statuses = RequestStatusEnum.values();
        Set<Integer> ids = Arrays.stream(statuses)
                .map(RequestStatusEnum::getId)
                .collect(Collectors.toSet());
        Set<String> names = Arrays.stream(statuses)
                .map(Enum::name)
                .collect(Collectors.toSet());

        assertEquals(statuses.length, ids.size());
        assertEquals(statuses.length, names.size());
    }

    @Test
    void localSeedMatchesEnumExactly() throws IOException {
        String dataSql;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("data.sql")) {
            if (input == null) {
                throw new IOException("data.sql was not found on the test classpath");
            }
            dataSql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        int insertStart = dataSql.indexOf("INSERT INTO request_status");
        int insertEnd = dataSql.indexOf("ON CONFLICT (request_status_id)", insertStart);
        String statusSeed = dataSql.substring(insertStart, insertEnd);

        Map<Integer, String> seededStatuses = new LinkedHashMap<>();
        Set<String> seededNames = new java.util.HashSet<>();
        Matcher matcher = SEEDED_STATUS.matcher(statusSeed);
        while (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            String name = matcher.group(2);
            assertNull(seededStatuses.put(id, name), "Duplicate request-status seed ID: " + id);
            assertTrue(seededNames.add(name), "Duplicate request-status seed name: " + name);
        }

        Map<Integer, String> enumStatuses = Arrays.stream(RequestStatusEnum.values())
                .collect(Collectors.toMap(
                        RequestStatusEnum::getId,
                        Enum::name,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        assertEquals(enumStatuses, seededStatuses);
    }
}
