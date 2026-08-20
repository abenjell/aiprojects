package com.sazyconseil.mcpserver.model;

import java.util.List;

/**
 * Représente un ticket Scrum ou une User Story dans le Backlog.
 */
public record Task(
    String id,
    String title,
    String description,
    String priority, // LOW, MEDIUM, HIGH, CRITICAL
    String status,   // TODO, IN_PROGRESS, REVIEW, DONE
    Integer storyPoints,
    List<String> acceptanceCriteria
) {}
