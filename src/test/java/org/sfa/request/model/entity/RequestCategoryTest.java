package org.sfa.request.model.entity;

import org.junit.jupiter.api.Test;
import org.sfa.request.model.enums.RequestCategoryEnum;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestCategoryTest {
    @Test
    void testGettersAndSetters() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestCategory requestCategory = new RequestCategory();
        requestCategory.setRequestCategoryId(1);
        requestCategory.setCategory(RequestCategoryEnum.UNSPECIFIED);
        requestCategory.setDescription("General category description");
        requestCategory.setLastUpdatedAt(lastUpdatedAtTime);
        requestCategory.setRequests(requestSet);

        assertThat(requestCategory.getRequestCategoryId()).isEqualTo(1);
        assertThat(requestCategory.getCategory()).isEqualTo(RequestCategoryEnum.UNSPECIFIED);
        assertThat(requestCategory.getDescription()).isEqualTo("General category description");
        assertThat(requestCategory.getLastUpdatedAt()).isEqualTo(lastUpdatedAtTime);
        assertThat(requestCategory.getRequests()).isEqualTo(requestSet);
        assertThat(requestCategory.getRequests()).isNotNull();
        assertThat(requestCategory.getRequests()).isEmpty();

        String expectedToString = "RequestCategory(requestCategoryId=1, category=UNSPECIFIED, " +
                "description=General category description, lastUpdatedAt=" + lastUpdatedAtTime + ", requests=[])";
        assertThat(requestCategory.toString()).isEqualTo(expectedToString);

        System.out.println(requestCategory.toString().equals(expectedToString));
    }

    @Test
    void testGettersAndSetters2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestCategory requestCategory = new RequestCategory(1, RequestCategoryEnum.UNSPECIFIED,
                "General category description", lastUpdatedAtTime, requestSet);

        assertThat(requestCategory.getRequestCategoryId()).isEqualTo(1);
        assertThat(requestCategory.getCategory()).isEqualTo(RequestCategoryEnum.UNSPECIFIED);
        assertThat(requestCategory.getDescription()).isEqualTo("General category description");
        assertThat(requestCategory.getLastUpdatedAt()).isEqualTo(lastUpdatedAtTime);
        assertThat(requestCategory.getRequests()).isEqualTo(requestSet);
        assertThat(requestCategory.getRequests()).isNotNull();
        assertThat(requestCategory.getRequests()).isEmpty();

        String expectedToString = "RequestCategory(requestCategoryId=1, category=UNSPECIFIED, " +
                "description=General category description, lastUpdatedAt=" + lastUpdatedAtTime + ", requests=[])";
        assertThat(requestCategory.toString()).isEqualTo(expectedToString);

        System.out.println(requestCategory.toString().equals(expectedToString));
    }

    @Test
    void testEquals() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestCategory requestCategory1 = new RequestCategory();
        requestCategory1.setRequestCategoryId(1);
        requestCategory1.setCategory(RequestCategoryEnum.UNSPECIFIED);
        requestCategory1.setDescription("General category description");
        requestCategory1.setLastUpdatedAt(lastUpdatedAtTime);
        requestCategory1.setRequests(requestSet);

        RequestCategory requestCategory2 = new RequestCategory();
        requestCategory2.setRequestCategoryId(1);
        requestCategory2.setCategory(RequestCategoryEnum.UNSPECIFIED);
        requestCategory2.setDescription("General category description");
        requestCategory2.setLastUpdatedAt(lastUpdatedAtTime);
        requestCategory2.setRequests(requestSet);

        RequestCategory requestCategory3 = new RequestCategory();
        requestCategory3.setRequestCategoryId(3);
        requestCategory3.setCategory(RequestCategoryEnum.TECHNICAL_SUPPORT);
        requestCategory3.setDescription("General category description 3");
        requestCategory3.setLastUpdatedAt(lastUpdatedAtTime);
        requestCategory3.setRequests(requestSet);

        assertThat(requestCategory1).isEqualTo(requestCategory2);
        assertThat(requestCategory2).isEqualTo(requestCategory1);

        assertThat(requestCategory1).isNotEqualTo(requestCategory3);
        assertThat(requestCategory2).isNotEqualTo(requestCategory3);
        assertThat(requestCategory1).isNotEqualTo(null);

    }

    @Test
    void testEquals2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestCategory requestCategory1 = new RequestCategory();
        requestCategory1.setRequestCategoryId(1);
        requestCategory1.setCategory(null);
        requestCategory1.setDescription(null);
        requestCategory1.setLastUpdatedAt(null);
        requestCategory1.setRequests(null);

        RequestCategory requestCategory2 = new RequestCategory();
        requestCategory2.setRequestCategoryId(1);
        requestCategory2.setCategory(null);
        requestCategory2.setDescription(null);
        requestCategory2.setLastUpdatedAt(null);
        requestCategory2.setRequests(null);

        RequestCategory requestCategory3 = new RequestCategory();
        requestCategory3.setRequestCategoryId(2);

        assertThat(requestCategory1).isEqualTo(requestCategory2);
        assertThat(requestCategory1).isNotEqualTo(requestCategory3);
    }

    @Test
    void testEqualsHashCode() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestCategory requestCategory = new RequestCategory();
        requestCategory.setRequestCategoryId(1);
        requestCategory.setCategory(RequestCategoryEnum.UNSPECIFIED);
        requestCategory.setDescription("General category description");
        requestCategory.setLastUpdatedAt(lastUpdatedAtTime);
        requestCategory.setRequests(requestSet);

        assertThat(requestCategory).isEqualTo(requestCategory);

        int expectedHashCodeResult = requestCategory.hashCode();
        assertThat(expectedHashCodeResult).isEqualTo(requestCategory.hashCode());
    }

    @Test
    void testEqualsHashCode2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestCategory requestCategory1 = new RequestCategory();
        requestCategory1.setRequestCategoryId(1);
        requestCategory1.setCategory(RequestCategoryEnum.UNSPECIFIED);
        requestCategory1.setDescription("General category description");
        requestCategory1.setLastUpdatedAt(lastUpdatedAtTime);
        requestCategory1.setRequests(requestSet);

        RequestCategory requestCategory2 = new RequestCategory();
        requestCategory2.setRequestCategoryId(1);
        requestCategory2.setCategory(RequestCategoryEnum.UNSPECIFIED);
        requestCategory2.setDescription("General category description");
        requestCategory2.setLastUpdatedAt(lastUpdatedAtTime);
        requestCategory2.setRequests(requestSet);

        int expectedHashCodeResult = requestCategory1.hashCode();
        assertThat(expectedHashCodeResult).isEqualTo(requestCategory2.hashCode());
    }
}
