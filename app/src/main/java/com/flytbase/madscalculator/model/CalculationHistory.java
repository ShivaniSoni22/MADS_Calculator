package com.flytbase.madscalculator.model;

public class CalculationHistory {

    private String expression;
    private String answer;

    public CalculationHistory(){

    }

    public CalculationHistory(String expression, String answer) {
        this.expression = expression;
        this.answer = answer;
    }

    public String getExpression() {
        return expression;
    }

    public String getAnswer() {
        return answer;
    }
}
