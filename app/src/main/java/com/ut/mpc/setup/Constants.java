package com.ut.mpc.setup;

import android.util.Log;

import com.ut.mpc.utils.GPSLib;
import com.ut.mpc.utils.STPoint;

public class Constants {

    //NOTE: The LST Filter and PACO in general will likely experience very odd behavior at the
    //edges of coordinate systems.  I.e. longitude going from 0 degrees to negative (assumptions about add/sub broken)

	public enum SpatialType{GPS,Meters};
	public static SpatialType SPATIAL_TYPE = SpatialType.GPS;
    public static STPoint DEFAULT_REF_POINT = new STPoint(-122.39393f,37.75169f,1212617674);

    public static final float FLOAT_DEC_BUDGE = .00001f; //about 10m
    public static final STPoint POS_FLOAT_BUDGE = new STPoint(FLOAT_DEC_BUDGE, FLOAT_DEC_BUDGE, 0);
    public static final STPoint NEG_FLOAT_BUDGE = new STPoint(-FLOAT_DEC_BUDGE, -FLOAT_DEC_BUDGE, 0);

    public static class PoK {
		public static float SPACE_WEIGHT = 1;
        public static float TEMPORAL_WEIGHT = 1;
        public static float SPACE_RADIUS;
        public static float TEMPORAL_RADIUS;
        public static int TRIM_THRESH = 10;

        //TODO: add SPACE-TEMPORAL Trim so nearby doesn't need to trim if they are insignificant points
        public static void updateConfig(STPoint refPoint){
            if(SPATIAL_TYPE == SpatialType.GPS){
                X_CUBE = GPSLib.longOffsetFromDistance(refPoint, PoK.SPACE_RADIUS);
                Y_CUBE = GPSLib.latOffsetFromDistance(refPoint, PoK.SPACE_RADIUS);
            } else {
                X_CUBE = PoK.SPACE_RADIUS;
                Y_CUBE = PoK.SPACE_RADIUS;
            }
            T_CUBE = PoK.TEMPORAL_RADIUS;
        }
        public static float X_CUBE, Y_CUBE, T_CUBE;
	}
	
	public static class SmartInsert{
		public static double INS_THRESH = .8;
	}
	
	public static boolean DEBUG_LEVEL1 = true;
	public static boolean DEBUG_LEVEL2 = true;
	public static boolean DEBUG_LEVEL3 = false;
	
	//level 1 for unit testing
	//level 2 for other testing
	//level 3 for performance benchmarking
	public static  void DebugPrint(String str, int level){
		if(level == 1 && DEBUG_LEVEL1){
			System.out.println(str);
		} else if(level == 2 && DEBUG_LEVEL2){
			System.out.println(str);
		} else if(level == 3 && DEBUG_LEVEL3){
			System.out.println(str);
		}
	}

//    public static void setCabDefaults(){
//        PoK.TEMPORAL_RADIUS = 60 * 60 * 6; //6 hours in seconds
//        PoK.SPACE_RADIUS = 1; //km
//        SPATIAL_TYPE = SpatialType.GPS;
//        PoK.updateConfig(DEFAULT_REF_POINT);
//    }
//
//    public static void setMobilityDefaults(){
//        PoK.TEMPORAL_RADIUS = 60 * 5; // 5 minutes in seconds
//        PoK.SPACE_RADIUS = 50; //m
//        SPATIAL_TYPE = SpatialType.Meters;
//        //DEFAULT_REF_POINT is unnecessary because gps type is off
//        PoK.updateConfig(DEFAULT_REF_POINT);
//    }

//	/*
//	 * Defaults configured for the Crawded Mobi Data Set
//	 * Distance is in meters
//	 */
//	public static  void setMobilityDefaults(){
//		SPATIAL_TYPE = SpatialType.Meters;
//		PoK.SPACE_WEIGHT = 100;
//		PoK.SPACE_RADIUS = 30;
//        PoK.TEMPORAL_RADIUS = 500;
//		PoK.GRID_DEFAULT = false;
//		PoK.X_GRID_GRAN = (PoK.GRID_DEFAULT) ? PoK.SPACE_RADIUS / 10 : 5;
//		PoK.Y_GRID_GRAN = (PoK.GRID_DEFAULT) ? PoK.SPACE_RADIUS / 10 : 5;
//		//PoK.NORMALIZE_PLOT = false;
//		PoK.TEMPORAL_DECAY = 8;
//		//PoK.CURRENT_TIMESTAMP = 0;
//		//PoK.REFERENCE_TIMESTAMP = 0;
//		SmartInsert.INS_THRESH = 80;
//		DEBUG_LEVEL1 = true;
//		DEBUG_LEVEL2 = true;
//		DEBUG_LEVEL3 = false;
//	}
//
//	/*
//	 * Defaults configured for the Cabspotting MObility Data Set
//	 * 	Distance is in kilometers
//	 */
//	public static  void setCabsDefaults(){
//		SPATIAL_TYPE = SpatialType.GPS;
//
//        PoK.SPACE_WEIGHT = 1f;
//        PoK.TEMPORAL_WEIGHT = 1f;
//        PoK.TOTAL_WEIGHT = 2f;
//        PoK.SPACE_RADIUS = .001f;
//        PoK.TEMPORAL_RADIUS = 1000 * 60 * 5f;
//        PoK.SPACE_DECAY = PoK.SPACE_WEIGHT / (PoK.SPACE_RADIUS);
//        PoK.TEMPORAL_DECAY = PoK.TEMPORAL_WEIGHT / (PoK.TEMPORAL_RADIUS);
//        //PoK.SPACE_RADIUS = .0035f;
//        //PoK.TEMPORAL_RADIUS = 1000 * 60 * 20;
//
//		PoK.GRID_DEFAULT = false;
//		PoK.X_GRID_GRAN = (PoK.GRID_DEFAULT) ? PoK.SPACE_RADIUS / 10 : .001f;
//		PoK.Y_GRID_GRAN = (PoK.GRID_DEFAULT) ? PoK.SPACE_RADIUS / 10 : .001f;
//	}
	
}
