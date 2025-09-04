package com.websockets.demo;

public class PlayerAssignment {
    private String sessionId;
    private String color;

    public PlayerAssignment() {}

    public PlayerAssignment(String sessionId, String color) {
        this.sessionId = sessionId;
        this.color = color;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
