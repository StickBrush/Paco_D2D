package com.ut.mpc.nativewrapper;

import com.ut.mpc.utils.STPoint;
import com.ut.mpc.utils.STRegion;
import com.ut.mpc.utils.STStorage;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

import nathanielwendt.mpc.ut.edu.paco.Data.PlaceData;

/**
 * Created by nathanielwendt on 2/19/15.
 */
public class NativeWrapper implements STStorage {
    private String tableName;
    private Connection conn;


    public NativeWrapper(Connection conn, String tableName){
        this.tableName = tableName;
        this.conn = conn;

        Statement stmt = null;
        int count = 0;
        try {
            stmt = this.conn.createStatement();
            String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + this.tableName +"';";
            ResultSet res = stmt.executeQuery(sql);
            boolean val = res.next();
            if(!val){
                System.out.println("creating table" + this.tableName);
                this.createTable();
            }
        } catch ( Exception e ){
            e.printStackTrace();
        }
    }

    private void createTable(){
        Statement stmt = null;
        try {
            stmt = this.conn.createStatement();
            String sql = "CREATE VIRTUAL TABLE " + this.tableName + " USING rtree(\n" +
                    "   id,              -- Integer primary key\n" +
                    "   minX, maxX,      -- Minimum and maximum X coordinate\n" +
                    "   minY, maxY,       -- Minimum and maximum Y coordinate\n" +
                    "   minT, maxT       -- Minimum and maximum T coordinate\n" +
                    ");";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    @Override
    public int getSize() {
        Statement stmt = null;
        int count = 0;
        try {
            stmt = this.conn.createStatement();
            String sql = "SELECT COUNT(*) as rowCount FROM " + this.tableName + ";";
            ResultSet res = stmt.executeQuery(sql);
            res.next();
            count = res.getInt("rowCount");
            res.close();
            stmt.close();
        } catch ( Exception e ){
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public void insert(STPoint point) {
        Statement stmt = null;
        try {
            String x = String.valueOf(point.getX());
            String y = String.valueOf(point.getY());
            String t = String.valueOf(point.getT());

            stmt = this.conn.createStatement();
            String sql = "INSERT INTO " + this.tableName + " (minX,maxX,minY,maxY,minT,maxT) VALUES ("
                                + x + ", " + x + ", " + y + "," + y + ", " + t + ", " + t + ");";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ){
            e.printStackTrace();
        }
    }

    public List<STPoint> getAllPoints(){
        String query = "SELECT * FROM " + this.tableName + ";";
        List<STPoint> points = null;
        try {
            Statement stmt = this.conn.createStatement();
            ResultSet res = stmt.executeQuery(query);
            points = resSetToList(res);
            res.close();
            stmt.close();
        } catch ( Exception e ){
            e.printStackTrace();
        }
        return points;
    }

    @Override
    public List<STPoint> range(STRegion range) {
        String query = "SELECT id,minX,maxX,minY,maxY,minT,maxT from " + this.tableName + " ";

        STPoint mins = range.getMins();
        STPoint maxs = range.getMaxs();

        String prefix = " WHERE ";
        if(mins.hasX() && maxs.hasX()){
            query += prefix + " minX >= " + String.valueOf(mins.getX()) +
                    " AND maxX <= " + String.valueOf(maxs.getX());
            prefix = " AND ";
        }

        if(mins.hasY() && maxs.hasY()){
            query += prefix + " minY >= " + String.valueOf(mins.getY()) +
                    " AND maxY <= " + String.valueOf(maxs.getY());
            prefix = " AND ";
        }

        if(mins.hasT() && maxs.hasT()){
            query += prefix + " minT >= " + String.valueOf(mins.getT()) +
                    " AND maxT <= " + String.valueOf(maxs.getT());
            prefix = " AND ";
        }

        List<STPoint> points = null;
        try {
            Statement stmt = this.conn.createStatement();
            ResultSet res = stmt.executeQuery(query);
            points = resSetToList(res);
            res.close();
            stmt.close();
        } catch ( Exception e ){
            e.printStackTrace();
        }
        return points;
    }

    private List<STPoint> resSetToList(ResultSet res) {
        List<STPoint> points = new ArrayList<STPoint>();
        try {
            while (res.next()) {
                STPoint point = new STPoint();
                point.setX(res.getFloat("minX"));
                point.setY(res.getFloat("minY"));
                point.setT(res.getFloat("minT"));
                points.add(point);
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return points;
    }

    @Override
    public List<STPoint> nearestNeighbor(STPoint needle, STPoint boundValues, int n) {
        return null;
    }

    @Override
    public List<STPoint> getSequence(STPoint start, STPoint end) {
        return null;
    }

    @Override
    public STRegion getBoundingBox() {
        String query = "SELECT minX FROM " + tableName + " ORDER BY minX ASC LIMIT 1;";
        float minX = getRowValueHelper(query, 1);
        query = "SELECT minY FROM " + this.tableName + " ORDER BY minY ASC Limit 1;";
        float minY = getRowValueHelper(query, 1);
        query = "SELECT minT FROM " + this.tableName + " ORDER BY minT ASC Limit 1;";
        float minT = getRowValueHelper(query, 1);
        query = "SELECT maxX FROM " + this.tableName + " ORDER BY maxX DESC Limit 1;";
        float maxX = getRowValueHelper(query, 1);
        query = "SELECT maxY FROM " + this.tableName + " ORDER BY maxY DESC Limit 1;";
        float maxY = getRowValueHelper(query, 1);
        query = "SELECT maxT FROM " + this.tableName + " ORDER BY maxT DESC Limit 1;";
        float maxT = getRowValueHelper(query, 1);

        return new STRegion(new STPoint(minX, minY, minT), new STPoint(maxX, maxY, maxT));
    }

    private float getRowValueHelper(String sql, int index){
        Statement stmt = null;
        float val = 0f;
        try {
            stmt = this.conn.createStatement();
            ResultSet res = stmt.executeQuery(sql);
            res.next();
            val = res.getFloat(index);
            res.close();
            stmt.close();
        } catch ( Exception e ){
            e.printStackTrace();
        }
        return val;
    }

    @Override
    public void clear() {

    }

    public List<PlaceData> getPlaces(){

        return null;
    }//
    public List<PlaceData> getPlacesByRange(STRegion range){

        return null;
    }//
    public void insert(STPoint point, String placeName, String uri){

    }//

    public void delete(String p){
//        structure.delete(p);
    }

}
