package com.ut.mpc.paco;

import com.ut.mpc.utils.LSTFilter;
import com.ut.mpc.utils.QueryWindow;
import com.ut.mpc.utils.STPoint;
import com.ut.mpc.utils.STRegion;

import java.util.List;

/**
 * Created by nathanielwendt on 10/13/16.
 */

public class Paco {
    private LSTFilter lstFilter;
    private QueryBroker queryBroker;

    public Paco(){}

    public double windowPoK(QueryWindow window, String appId) throws PacoException {
        double cost = window.getCost();
        QueryBudget budget = queryBroker.account(appId);
        if(budget.canSpend(cost)){
            budget.spend(cost);
            return lstFilter.windowPoK(window);
        } else {
            throw new PacoException("Budget exceeded for appId: " + appId);
        }
    }

    public double currWindowPoK() throws PacoException {
        return 0.0;
    }

    public STPoint getLocation() throws PacoException {
        return null;
    }

    public List<STPoint> findPath(STRegion region){
        return null;
    }

    /*
    Following methods are for distributed processing with a Z-index
     */

    // Currently bypasses budgets, since we are dealing with semi-trusted party
    public List<String> keys(QueryWindow window){
        return null;
    }

    public List<String> groups(STRegion region){
        return null;
    }

    public List<String> groups(){
        return null;
    }

    public List<String> orderedKeys(List<String> includedKeys) {
        return null;
    }


    public int register(){
        //need a way to generate unique id per application (make sure they don't exceed budget)
        //User interaction to approve/disprove app and set access profile level
        return 0;

    }

}
