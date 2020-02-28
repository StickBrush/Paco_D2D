package com.ut.mpc.paco;

//import javax.management.Query;

/**
 * Created by nathanielwendt on 10/13/16.
 */
public class QueryBudget {
    private double budget;
    private double balance;

    public QueryBudget(double budget){
        budget = budget;
    }

    public void spend(double value){
        if(balance + value > budget) {
            throw new RuntimeException("Query Budget exceeded");
        }
        balance += value;
    }

    public boolean canSpend(double value){
        return balance + value <= budget;
    }
}
