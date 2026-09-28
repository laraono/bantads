package com.bantads.orchestration.model;

public class StepDefinition {
    private final String queue;
    private final String actionType;

    public StepDefinition(String queue, String actionType) {
        this.queue = queue;
        this.actionType = actionType;
    }

    public String getQueue() { return queue; }
    public String getActionType() { return actionType; }
}