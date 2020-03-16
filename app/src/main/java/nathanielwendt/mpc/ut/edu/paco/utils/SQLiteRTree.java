package nathanielwendt.mpc.ut.edu.paco.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.ut.mpc.utils.STPoint;
import com.ut.mpc.utils.STRegion;
import com.ut.mpc.utils.STStorage;

//import org.sqlite.database.ExtraUtils;
import org.sqlite.database.DatabaseUtils;
import org.sqlite.database.sqlite.SQLiteDatabase;
import org.sqlite.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import nathanielwendt.mpc.ut.edu.paco.Data.PlaceData;

import static nathanielwendt.mpc.ut.edu.paco.utils.DBConstants.DATABASE_NAME;
import static nathanielwendt.mpc.ut.edu.paco.utils.DBConstants.DATABASE_VERSION;


public class SQLiteRTree extends SQLiteOpenHelper implements STStorage {

    static {
        System.loadLibrary("sqliteX");
    }

    private Context myContext;
    private String table_identifier;

    public SQLiteRTree(Context context, String identifier) {
        super(context, context.getDatabasePath(DATABASE_NAME).getPath(), null, DATABASE_VERSION);
        this.myContext = context;
        this.table_identifier = identifier;

        DBInit dbInit = new DBInit(context);

        if(!dbInit.dbExists()){
            Log.d("LST", "DB did not exist, using dbInit to create now.");
            dbInit.init();
            this.onCreate(this.getWritableDatabase());
        }
    }

    @Override
    public int getSize() {
        SQLiteDatabase db = this.getReadableDatabase();
        //int size = ((Long) ExtraUtils.longForQuery(db, "SELECT COUNT(*) FROM " + this.table_identifier, null)).intValue();//
        int size = ((Long) DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + this.table_identifier, null)).intValue();
        db.close();
        return size;
    }

    @Override
    public void insert(STPoint point) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("minX", point.getX());
        values.put("maxX", point.getX());
        values.put("minY", point.getY());
        values.put("maxY", point.getY());
        values.put("minT", point.getT());
        values.put("maxT", point.getT());
        //values.put("placeName", "Default");//
        //values.put("uri", "NONE");//
        db.insert(this.table_identifier, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        db.close();
    }

    @Override
    public void insert(STPoint point, String placeName, String uri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if("".equals(placeName)){placeName = "Default";}
        values.put("minX", point.getX());
        values.put("maxX", point.getX());
        values.put("minY", point.getY());
        values.put("maxY", point.getY());
        values.put("minT", point.getT());
        values.put("maxT", point.getT());
        values.put("placeName", placeName);
        values.put("uri", uri);
        db.insert(this.table_identifier, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        db.close();
    }

    @Override
    public List<STPoint> range(STRegion range) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT id,minX,maxX,minY,maxY,minT,maxT from " + this.table_identifier + " ";

        STPoint mins = range.getMins();
        STPoint maxs = range.getMaxs();

        String prefix = " WHERE ";
//        List<Float> bindings = new ArrayList<Float>();
//        if(mins.hasX() && maxs.hasX()){
//            query += prefix + " minX >= ? AND maxX <= ?";
//            bindings.add(mins.getX());
//            bindings.add(maxs.getX());
//            prefix = " AND ";
//        }
//
//        if(mins.hasY() && maxs.hasY()){
//            query += prefix + " minY >= ? AND maxY <= ?";
//            bindings.add(mins.getY());
//            bindings.add(maxs.getY());
//            prefix = " AND ";
//        }
//
//        if(mins.hasT() && maxs.hasT()){
//            query += prefix + " minT >= ? AND maxT <= ?";
//            bindings.add(mins.getT());
//            bindings.add(maxs.getT());
//            prefix = " AND ";
//        }
//
//        SQLiteStatement statement = db.compileStatement(query);
//        int index = 1;
//        for(Float val: bindings){
//            statement.bindDouble(index, (double) val);
//            index++;
//        }


        if(mins.hasX() && maxs.hasX()){
            query += prefix + " minX >= " + String.format("%f", mins.getX()) +
                    " AND maxX <= " + String.format("%f", maxs.getX());
            prefix = " AND ";
        }

        if(mins.hasY() && maxs.hasY()){
            query += prefix + " minY >= " + String.format("%f", mins.getY()) +
                    " AND maxY <= " + String.format("%f", maxs.getY());
            prefix = " AND ";
        }

//        if(mins.hasT() && maxs.hasT()){
//            query += prefix + " minT >= " + String.format("%f", mins.getT()) +
//                    " AND maxT <= " + String.format("%f", maxs.getT());
//            prefix = " AND ";
//        }

        Cursor cur = db.rawQuery(query, null);

        List<STPoint> points = cursorToList(cur);
        db.close();
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
        //TODO: get bounding box through R-Tree shadow tables

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT minX FROM " + this.table_identifier + " ORDER BY minX ASC LIMIT 1;";
        float minX = getRowValueHelper(db, query, 0);
        query = "SELECT minY FROM " + this.table_identifier + " ORDER BY minY ASC Limit 1;";
        float minY = getRowValueHelper(db, query, 0);
        query = "SELECT minT FROM " + this.table_identifier + " ORDER BY minT ASC Limit 1;";
        float minT = getRowValueHelper(db, query, 0);
        query = "SELECT maxX FROM " + this.table_identifier + " ORDER BY maxX DESC Limit 1;";
        float maxX = getRowValueHelper(db, query, 0);
        query = "SELECT maxY FROM " + this.table_identifier + " ORDER BY maxY DESC Limit 1;";
        float maxY = getRowValueHelper(db, query, 0);
        query = "SELECT maxT FROM " + this.table_identifier + " ORDER BY maxT DESC Limit 1;";
        float maxT = getRowValueHelper(db, query, 0);

        return new STRegion(new STPoint(minX, minY, minT), new STPoint(maxX, maxY, maxT));
    }

    private float getRowValueHelper(SQLiteDatabase db, String query, int index){
        Cursor cur = db.rawQuery(query, null);
        cur.moveToFirst();
        float val = cur.getFloat(index);
        cur.close();
        return val;
    }

    private List<STPoint> cursorToList(Cursor cur){
        List<STPoint> list = new ArrayList<STPoint>();

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            int id = cur.getInt(0);
            float minX = cur.getFloat(1);
            float maxX = cur.getFloat(2);
            float minY = cur.getFloat(3);
            float maxY = cur.getFloat(4);
            float minT = cur.getFloat(5);
            float maxT = cur.getFloat(6);

            //Log.d("LST","minx: " + minX + " , maxx: " + maxX);
            //Log.d("LST","miny: " + minY + " , maxy: " + maxY);
            //Log.d("LST","mint: " + minT + " , maxt: " + maxT);

            list.add(new STPoint(minX, minY, minT));
            cur.moveToNext();
        }
        cur.close();

        return list;
    }


    protected boolean doesTableExist(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public void forceCreateTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        if(!doesTableExist(db, this.table_identifier)){
            onCreate(db);
        }
    }

    public void clear(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(this.table_identifier, null, null);
    }

    public void delete(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(this.table_identifier, "placeName="+name, null);//int
//        db.delete(this.table_identifier, "placeName = ?",new String[] {name});//String
    }////

    @Override
    public void onCreate(SQLiteDatabase database) {
        String create_rtree = "CREATE VIRTUAL TABLE " + this.table_identifier + " USING rtree(\n" +
                "   id,              -- Integer primary key\n" +
                "   minX, maxX,      -- Minimum and maximum X coordinate\n" +
                "   minY, maxY,       -- Minimum and maximum Y coordinate\n" +
                "   minT, maxT,       -- Minimum and maximum T coordinate\n" +
                "   +placeName TEXT,       -- placeName of the object\n" +
                "   +uri TEXT,             -- uri of the object\n" +
                ");";
        Log.d("LST", "Created table: " + this.table_identifier);
        database.execSQL(create_rtree);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SQLiteRTree.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + this.table_identifier);
        onCreate(db);
    }

    @Override
    public List<PlaceData> getPlaces(){

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT id,minX,maxX,minY,maxY,minT,maxT,placeName,uri from " + this.table_identifier + " ";
        Cursor cur = db.rawQuery(query, null);

        List<PlaceData> places = pointToList(cur);

        db.close();

        return places;
    }//

    private List<PlaceData> pointToList(Cursor cur){
        List<PlaceData> list = new ArrayList<PlaceData>();

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            int id = cur.getInt(0);
            float minX = cur.getFloat(1);
            float maxX = cur.getFloat(2);
            float minY = cur.getFloat(3);
            float maxY = cur.getFloat(4);
            float minT = cur.getFloat(5);
            float maxT = cur.getFloat(6);
            String placeName = cur.getString(7);
            String uri = cur.getString(8);

            STPoint point = new STPoint(minX, minY, minT);
            STRegion bounds = new STRegion(point, point);
            PlaceData nextPlace = new PlaceData(placeName, uri, bounds);

            list.add(nextPlace);
            cur.moveToNext();
        }
        cur.close();
        Log.d("pointToList", list.toString());
        return list;
    }//

    @Override
    public List<PlaceData> getPlacesByRange(STRegion range) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT id,minX,maxX,minY,maxY,minT,maxT,placeName,uri from " + this.table_identifier + " ";

        STPoint mins = range.getMins();
        STPoint maxs = range.getMaxs();

        String prefix = " WHERE ";

        if(mins.hasX() && maxs.hasX()){
            query += prefix + " minX >= " + String.format("%f", mins.getX()) +
                    " AND maxX <= " + String.format("%f", maxs.getX());
            prefix = " AND ";
        }

        if(mins.hasY() && maxs.hasY()){
            query += prefix + " minY >= " + String.format("%f", mins.getY()) +
                    " AND maxY <= " + String.format("%f", maxs.getY());
            prefix = " AND ";
        }

//        if(mins.hasT() && maxs.hasT()){
//            query += prefix + " minT >= " + String.format("%f", mins.getT()) +
//                    " AND maxT <= " + String.format("%f", maxs.getT());
//            prefix = " AND ";
//        }

        Cursor cur = db.rawQuery(query, null);

        List<PlaceData> places = pointToList(cur);

        db.close();

        return places;
    }

//    private List<PlaceData> RegionPointToList(Cursor cur){
//        List<PlaceData> list = new ArrayList<PlaceData>();
//
//        cur.moveToFirst();
//        while (!cur.isAfterLast()) {
//            int id = cur.getInt(0);
//            float minX = cur.getFloat(1);
//            float maxX = cur.getFloat(2);
//            float minY = cur.getFloat(3);
//            float maxY = cur.getFloat(4);
//            float minT = cur.getFloat(5);
//            float maxT = cur.getFloat(6);
//            String placeName = cur.getString(7);
//            String uri = cur.getString(8);
//
//            STPoint point = new STPoint(minX, minY, minT);
//            STRegion bounds = new STRegion(point, point);
//            PlaceData nextPlace = new PlaceData(placeName, uri, bounds);
//
//            list.add(nextPlace);
//            cur.moveToNext();
//        }
//        cur.close();
//        Log.d("pointToList", list.toString());
//        return list;
//    }//
}