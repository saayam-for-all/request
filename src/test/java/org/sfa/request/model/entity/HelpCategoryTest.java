package org.sfa.request.model.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelpCategoryTest {

    @Test
    void testGettersAndSetters() {
        HelpCategory helpCategory = new HelpCategory();
        helpCategory.setCatId("1.2");
        helpCategory.setCatName("GROCERY_SHOPPING");
        helpCategory.setCatDesc("Grocery shopping and delivery");

        assertThat(helpCategory.getCatId()).isEqualTo("1.2");
        assertThat(helpCategory.getCatName()).isEqualTo("GROCERY_SHOPPING");
        assertThat(helpCategory.getCatDesc()).isEqualTo("Grocery shopping and delivery");
    }

    @Test
    void testAllArgsConstructor() {
        HelpCategory helpCategory = new HelpCategory("1.2", "GROCERY_SHOPPING",
                "Grocery shopping and delivery");

        assertThat(helpCategory.getCatId()).isEqualTo("1.2");
        assertThat(helpCategory.getCatName()).isEqualTo("GROCERY_SHOPPING");
        assertThat(helpCategory.getCatDesc()).isEqualTo("Grocery shopping and delivery");
    }

    @Test
    void testBuilder() {
        HelpCategory helpCategory = HelpCategory.builder()
                .catId("1.2")
                .catName("GROCERY_SHOPPING")
                .catDesc("Grocery shopping and delivery")
                .build();

        assertThat(helpCategory.getCatId()).isEqualTo("1.2");
        assertThat(helpCategory.getCatName()).isEqualTo("GROCERY_SHOPPING");
        assertThat(helpCategory.getCatDesc()).isEqualTo("Grocery shopping and delivery");
    }

    @Test
    void testEquals() {
        HelpCategory helpCategory1 = new HelpCategory("1.2", "GROCERY_SHOPPING", "desc");
        HelpCategory helpCategory2 = new HelpCategory("1.2", "GROCERY_SHOPPING", "desc");
        HelpCategory helpCategory3 = new HelpCategory("2.1", "MEDICAL", "different desc");

        assertThat(helpCategory1).isEqualTo(helpCategory2);
        assertThat(helpCategory1).isNotEqualTo(helpCategory3);
        assertThat(helpCategory1).isNotEqualTo(null);
    }

    @Test
    void testHashCode() {
        HelpCategory helpCategory1 = new HelpCategory("1.2", "GROCERY_SHOPPING", "desc");
        HelpCategory helpCategory2 = new HelpCategory("1.2", "GROCERY_SHOPPING", "desc");

        assertThat(helpCategory1.hashCode()).isEqualTo(helpCategory2.hashCode());
    }
}