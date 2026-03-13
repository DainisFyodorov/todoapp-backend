package lv.dainis.todoapp.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TaskPriority {
    LOW,
    MEDIUM,
    HIGH;

    @JsonCreator
    public static TaskPriority fromString(String value) {
        for(TaskPriority taskPriority : TaskPriority.values()) {
            if(taskPriority.name().equalsIgnoreCase(value)) {
                return taskPriority;
            }
        }

        return null;
    }
}
