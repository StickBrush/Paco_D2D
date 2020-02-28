package com.ut.mpc.workload;

import com.ut.mpc.utils.STPoint;
import com.ut.mpc.utils.STStorage;

import java.io.FileReader;
import java.io.BufferedReader;


public class CabsWrapper {

    //@pre: requires at least one point in file, otherwise seg. fault
    //@pre: requires first entry to be most recent and last entry to be least recent
    public static void fillPointsFromFile(STStorage[] trees, String[] args) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader("../Crawdad/cabspottingdata/" + args[0]));
        //BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/../Crawdad/mobilitydata/KAIST/" + args[0]));
        String line;
        line = br.readLine();
        String[] split = new String[3];
        split = line.split(" ");
//        Init.REFERENCE_TIMESTAMP = timestamp; //should be 0.00 for mobility data
 //       Init.CURRENT_TIMESTAMP = timestamp; //continually update current to simulate real-time insertion of points
        STPoint point = new STPoint(Float.valueOf(split[0]),Float.valueOf(split[1]),Float.valueOf(split[2]));
        insertPoint(trees, point);

        while ((line = br.readLine()) != null) {
            split = line.split(" ");
//s          Init.CURRENT_TIMESTAMP = timestamp; //continually update current to simulate real-time insertion of points
            point = new STPoint(Float.valueOf(split[0]),Float.valueOf(split[1]),Float.valueOf(split[3]));
            insertPoint(trees, point);
        }
        br.close();
    }

    public static void insertPoint(STStorage[] trees, STPoint temp){
        for(int i = 0; i < trees.length; i++){
            trees[i].insert(temp);
        }
    }

}