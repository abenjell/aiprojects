package com.sazyconseil.mcpserver.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.sazyconseil.mcpserver.model.Task;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.File;
import tools.jackson.core.JacksonException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BacklogService {

    private final ObjectMapper objectMapper;
    private final File storageFile;
    private final List<Task> tasks = new ArrayList<>();
    private final AtomicInteger taskCounter = new AtomicInteger(0);

    public BacklogService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.storageFile = new File("backlog.json");
        loadTasks();
    }

    private synchronized void loadTasks() {
        if (!storageFile.exists()) {
            taskCounter.set(0);
            return;
        }
        try {
            List<Task> loadedTasks = objectMapper.readValue(storageFile, new TypeReference<List<Task>>() {});
            tasks.clear();
            tasks.addAll(loadedTasks);
            
            // Initialiser le compteur basé sur l'ID séquentiel le plus élevé existant (ex: TASK-12)
            int maxId = 0;
            for (Task task : tasks) {
                if (task.id() != null && task.id().startsWith("TASK-")) {
                    try {
                        int num = Integer.parseInt(task.id().substring(5));
                        if (num > maxId) {
                            maxId = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            taskCounter.set(maxId);
        } catch (JacksonException e) {
            System.err.println("Erreur lors de la lecture du fichier backlog.json : " + e.getMessage());
            taskCounter.set(0);
        }
    }

    private synchronized void saveTasks() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(storageFile, tasks);
        } catch (JacksonException e) {
            System.err.println("Erreur lors de l'écriture du fichier backlog.json : " + e.getMessage());
        }
    }

    @Tool(description = "Récupérer la liste complète de toutes les User Stories et tâches présentes dans le Backlog Scrum")
    public synchronized List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }

    @Tool(description = "Ajouter une nouvelle User Story ou tâche de backlog structurée")
    public synchronized Task addTask(
            @ToolParam(description = "Titre court et explicite de la tâche") String title,
            @ToolParam(description = "Description rédigée (ex: En tant que... Je veux... Afin de...)") String description,
            @ToolParam(description = "Priorité du ticket (LOW, MEDIUM, HIGH, CRITICAL)") String priority,
            @ToolParam(description = "Points d'effort Scrum (Story Points: 1, 2, 3, 5, 8, 13)") int storyPoints,
            @ToolParam(description = "Liste des critères d'acceptation au format Gherkin ou textuel") List<String> acceptanceCriteria
    ) {
        String id = "TASK-" + taskCounter.incrementAndGet();
        String finalPriority = priority != null ? priority.toUpperCase() : "MEDIUM";
        
        // Aplatir et assainir la liste pour éviter le double-wrapping (type erasure Jackson + LLM formatting bug)
        List<String> flatCriteria = new ArrayList<>();
        if (acceptanceCriteria != null) {
            for (Object item : acceptanceCriteria) {
                if (item instanceof Iterable<?> iterable) {
                    for (Object nestedItem : iterable) {
                        if (nestedItem != null) {
                            flatCriteria.add(nestedItem.toString());
                        }
                    }
                } else if (item != null) {
                    flatCriteria.add(item.toString());
                }
            }
        }
        
        Task newTask = new Task(
                id,
                title,
                description,
                finalPriority,
                "TODO",
                storyPoints,
                flatCriteria
        );
        
        tasks.add(newTask);
        saveTasks();
        return newTask;
    }

    @Tool(description = "Mettre à jour le statut d'un ticket existant dans le Backlog")
    public synchronized Task updateTaskStatus(
            @ToolParam(description = "L'ID unique du ticket (ex: TASK-1)") String taskId,
            @ToolParam(description = "Le nouveau statut du ticket (TODO, IN_PROGRESS, REVIEW, DONE)") String status
    ) {
        if (taskId == null || status == null) {
            throw new IllegalArgumentException("L'ID du ticket et le statut sont obligatoires");
        }
        
        String finalStatus = status.toUpperCase();
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            if (taskId.equalsIgnoreCase(t.id())) {
                Task updatedTask = new Task(
                        t.id(),
                        t.title(),
                        t.description(),
                        t.priority(),
                        finalStatus,
                        t.storyPoints(),
                        t.acceptanceCriteria()
                );
                tasks.set(i, updatedTask);
                saveTasks();
                return updatedTask;
            }
        }
        throw new IllegalArgumentException("Aucun ticket trouvé avec l'ID : " + taskId);
    }

    @Tool(description = "Effacer complètement le Backlog de toutes ses tâches (utile pour réinitialiser le projet)")
    public synchronized java.util.Map<String, String> clearBacklog() {
        tasks.clear();
        taskCounter.set(0);
        if (storageFile.exists()) {
            storageFile.delete();
        }
        return java.util.Map.of("message", "Le Backlog a été vidé avec succès.");
    }
}
