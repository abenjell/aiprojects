package com.sazyconseil.mcpserver.service;

import tools.jackson.databind.ObjectMapper;
import com.sazyconseil.mcpserver.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BacklogServiceTest {

    private BacklogService backlogService;
    private final File tempFile = new File("backlog.json");

    @BeforeEach
    void setUp() {
        // Nettoyer s'il y a un fichier résiduel
        if (tempFile.exists()) {
            tempFile.delete();
        }
        backlogService = new BacklogService(new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void shouldAddNewTaskToBacklog() {
        Task task = backlogService.addTask(
                "Créer l'authentification",
                "En tant que visiteur, je veux me connecter...",
                "HIGH",
                5,
                List.of("Le mot de passe doit être hashé", "Le token JWT expire après 1h")
        );

        assertThat(task).isNotNull();
        assertThat(task.id()).isEqualTo("TASK-1");
        assertThat(task.title()).isEqualTo("Créer l'authentification");
        assertThat(task.priority()).isEqualTo("HIGH");
        assertThat(task.status()).isEqualTo("TODO");
        assertThat(task.storyPoints()).isEqualTo(5);
        assertThat(task.acceptanceCriteria()).hasSize(2)
                .containsExactly("Le mot de passe doit être hashé", "Le token JWT expire après 1h");

        List<Task> tasks = backlogService.getTasks();
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0)).isEqualTo(task);
    }

    @Test
    void shouldUpdateTaskStatusSuccessfully() {
        Task task = backlogService.addTask(
                "Tâche de test",
                "Description",
                "LOW",
                1,
                List.of()
        );

        Task updated = backlogService.updateTaskStatus("TASK-1", "IN_PROGRESS");

        assertThat(updated.id()).isEqualTo("TASK-1");
        assertThat(updated.status()).isEqualTo("IN_PROGRESS");

        List<Task> tasks = backlogService.getTasks();
        assertThat(tasks.get(0).status()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTask() {
        assertThatThrownBy(() -> backlogService.updateTaskStatus("TASK-999", "DONE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Aucun ticket trouvé avec l'ID : TASK-999");
    }

    @Test
    void shouldClearBacklogSuccessfully() {
        backlogService.addTask("Tâche 1", "Desc 1", "MEDIUM", 3, List.of());
        backlogService.addTask("Tâche 2", "Desc 2", "LOW", 2, List.of());

        assertThat(backlogService.getTasks()).hasSize(2);

        java.util.Map<String, String> result = backlogService.clearBacklog();
        assertThat(result.get("message")).contains("Le Backlog a été vidé");
        assertThat(backlogService.getTasks()).isEmpty();
        assertThat(tempFile.exists()).isFalse();
    }
}
