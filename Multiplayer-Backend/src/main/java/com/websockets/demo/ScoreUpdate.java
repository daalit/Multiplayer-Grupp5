package com.websockets.demo;

import java.util.Map;

public class ScoreUpdate {
    private Map<String, Integer> scores;

    public ScoreUpdate() {}

    public ScoreUpdate(Map<String, Integer> scores) {
        this.scores = scores;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<String, Integer> scores) {
        this.scores = scores;
    }
}
