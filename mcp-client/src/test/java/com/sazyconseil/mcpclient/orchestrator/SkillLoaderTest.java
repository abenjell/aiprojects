package com.sazyconseil.mcpclient.orchestrator;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class SkillLoaderTest {

    private final SkillLoader skillLoader = new SkillLoader(new DefaultResourceLoader());

    @Test
    void shouldLoadExistingSkill() {
        // Given
        String skillName = "sql-optimizer";

        // When
        String content = skillLoader.loadSkill(skillName);

        // Then
        assertThat(content)
            .isNotEmpty()
            .contains("SQL Optimizer Guard")
            .contains("SELECT *");
    }

    @Test
    void shouldReturnEmptyStringForNonExistingSkill() {
        // Given
        String skillName = "non-existing-skill-xyz";

        // When
        String content = skillLoader.loadSkill(skillName);

        // Then
        assertThat(content).isEmpty();
    }
}
