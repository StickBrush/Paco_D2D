package com.ut.mpc.nativewrapper;

import com.ut.mpc.setup.Constants;
import com.ut.mpc.setup.Initializer;
import com.ut.mpc.utils.LSTFilter;
import com.ut.mpc.utils.STPoint;
import com.ut.mpc.utils.STRegion;
import com.ut.mpc.utils.STStorage;
import com.ut.mpc.workload.CabsWrapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * Created by nathanielwendt on 2/19/15.
 */
public class DemoRunner {
    public static void main(String[] args){

        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:../Data/DB/mydata.db");
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        //Constants.setCabsDefaults();
        NativeWrapper main = new NativeWrapper(conn,"RTreeMain");
//
//        STStorage[] storages = new STStorage[]{main};
//        String[] file = new String[]{"new_atsfiv.txt"};
//        try {
//            CabsWrapper.fillPointsFromFile(storages, file);
//        } catch (Exception e){
//            e.printStackTrace();
//        }

        LSTFilter filter = new LSTFilter(main, Initializer.vehicularDefaults());
//        STRegion query = new STRegion(new STPoint(37.4f,-122.2f,0), new STPoint(37.8f,-122.6f,10000));
//        double val = filter.windowPoK(query);
//        System.out.println(val);

        List<STPoint> points = main.range(new STRegion(STPoint.minPoint(), STPoint.maxPoint()));
        System.out.println(points.size());

        points = main.getAllPoints();
        System.out.println(points.size());


        NativeWrapper midWrapper = new NativeWrapper(conn,"RTreeMid");
        NativeWrapper sparseWrapper = new NativeWrapper(conn,"RTreeSparse");
        LSTFilter mid = new LSTFilter(midWrapper, Initializer.vehicularDefaults());
        LSTFilter sparse = new LSTFilter(sparseWrapper, Initializer.vehicularDefaults());

        mid.setSmartInsert(true);
        Constants.SmartInsert.INS_THRESH = .8;
        for(STPoint point: points){
            mid.insert(point);
        }

        sparse.setSmartInsert(true);
        Constants.SmartInsert.INS_THRESH = .2;
        for(STPoint point: points){
            sparse.insert(point);
        }


        System.out.println(main.getSize());
        System.out.println(mid.getSize());
        System.out.println(sparse.getSize());

        try {
            conn.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}
