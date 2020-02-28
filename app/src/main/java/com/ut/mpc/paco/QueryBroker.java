package com.ut.mpc.paco;

import com.ut.mpc.utils.STRegion;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nathanielwendt on 10/13/16.
 */
public class QueryBroker {
    Map<String, QueryBudget> budgets = new HashMap<String, QueryBudget>();

    public QueryBroker(){

    }

    public QueryBudget account(String appId){
        QueryBudget budget = budgets.get(appId);
        if(budget == null){
            budget = new QueryBudget(BudgetConstants.UNKNOWN_PARTY);
            budgets.put(appId, budget);
        }
        return budget;
    }

}
