package com.ut.mpc.utils;

import com.ut.mpc.setup.Constants;

public class GPSLib {

    public static void main(String[] args){
        System.out.println(Math.toRadians(0));
    }

	private static float longitudeKM = .009f;
	private static float latitudeKM = .001f;

	/*
	 * Retrieves a space bound around a given point with given radius values
	 * @param point - point around which to form the bound
	 * @param boundValues - point representing expansion values in each dimension.
	 */
	public static STRegion getSpaceBoundQuick(STPoint point, STPoint boundValues, Constants.SpatialType type) throws LSTFilterException{
        STPoint newMin = new STPoint();
        STPoint newMax = new STPoint();
        if(point.hasX() && boundValues.hasX()){
            newMin.setX(point.getX() - boundValues.getX());
            newMax.setX(point.getX() + boundValues.getX());
        }

        if(point.hasY() && boundValues.hasY()){
            newMin.setY(point.getY() - boundValues.getY());
            newMax.setY(point.getY() + boundValues.getY());
        }

        if(point.hasT() && boundValues.hasT()){
            newMin.setT(point.getT() - boundValues.getT());
            newMax.setT(point.getT() + boundValues.getT());
        }

		return new STRegion(newMin, newMax);
	}

    public static double distanceX(STPoint p1, STPoint p2, Constants.SpatialType type) throws LSTFilterException {
        if(type == Constants.SpatialType.GPS) {
            double R = 6371;
            double long1 = Math.toRadians(p1.getX());
            double long2 = Math.toRadians(p2.getX());
            double d = Math.acos(1 * Math.cos(long2 - long1)) * R;
            return d;
        } else if(type == Constants.SpatialType.Meters) {
            return Math.abs(p2.getX() - p1.getX());
        } else {
            throw new LSTFilterException("Unrecognized coordinate type for GPSLib");
        }
    }

    public static double distanceY(STPoint p1, STPoint p2, Constants.SpatialType type) throws LSTFilterException {
        if(type == Constants.SpatialType.GPS) {
            double R = 6371;
            double lat1 = Math.toRadians(p1.getY());
            double lat2 = Math.toRadians(p2.getY());
            double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) +
                    Math.cos(lat1) * Math.cos(lat2)) * R;
            return d;
        } else if(type == Constants.SpatialType.Meters) {
            return Math.abs(p2.getY() - p1.getY());
        } else {
            throw new LSTFilterException("Unrecognized coordinate type for GPSLib");
        }
    }

    public static double distanceT(STPoint p1, STPoint p2){
        return Math.abs(p1.getT() - p2.getT());
    }
	
	public static double spatialDistanceBetween(STPoint p1, STPoint p2, Constants.SpatialType type) throws LSTFilterException{
		if(type == Constants.SpatialType.GPS){
			double R = 6371;
			double lat1 = Math.toRadians(p1.getY());
			double lat2 = Math.toRadians(p2.getY());
			double long1 = Math.toRadians(p1.getX());
			double long2 = Math.toRadians(p2.getX());
			double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) +
							     Math.cos(lat1) * Math.cos(lat2) *
							     Math.cos(long2 - long1)) * R;
			return d;
		} else if(type == Constants.SpatialType.Meters) {
			return Math.sqrt(Math.pow(p1.getX() - p2.getX(),2) + Math.pow(p1.getY() - p2.getY(),2));
			//return Math.pow(p1.getX() - p2.getX(),2) + Math.pow(p1.getY() - p2.getY(),2);
		} else {
			throw new LSTFilterException("Unrecognized coordinate type for GPSLib");
		}
	}

    //Has a tolerance of about 10m when used as distance calculation 1km
    //Drastically decreases in accuracy with increasing distance calculations
    public static double spatialDistanceBetweenFast(STPoint p1, STPoint p2, Constants.SpatialType type) throws LSTFilterException {
        if(type == Constants.SpatialType.GPS){
            double R = 6371;
            double lat1 = Math.toRadians(p1.getY());
            double lat2 = Math.toRadians(p2.getY());
            double long1 = Math.toRadians(p1.getX());
            double long2 = Math.toRadians(p2.getX());

            double x = (long2 - long1) * Math.cos((lat1 - lat2)/2);
            double y = (lat2 - lat1);
            double d = Math.sqrt(x*x + y*y) * R;
            return d;
        } else if(type == Constants.SpatialType.Meters) {
            return Math.sqrt(Math.pow(p1.getX() - p2.getX(),2) + Math.pow(p1.getY() - p2.getY(),2));
            //return Math.pow(p1.getX() - p2.getX(),2) + Math.pow(p1.getY() - p2.getY(),2);
        } else {
            throw new LSTFilterException("Unrecognized coordinate type for GPSLib");
        }
    }

    public static double temporalDistanceBetween(STPoint p1, STPoint p2){
        return Math.abs(p1.getT() - p2.getT());
    }

    public static double spatialTemporalDistanceBetween(STPoint p1, STPoint p2, Constants.SpatialType type) throws LSTFilterException {
        if(type == Constants.SpatialType.GPS){
            double R = 6371;
            double lat1 = Math.toRadians(p1.getY());
            double lat2 = Math.toRadians(p2.getY());
            double long1 = Math.toRadians(p1.getX());
            double long2 = Math.toRadians(p2.getX());
            double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) +
                    Math.cos(lat1) * Math.cos(lat2) *
                            Math.cos(long2 - long1)) * R;
            return Math.sqrt(Math.pow(d,2) + Math.pow(p1.getT() - p2.getT(), 2));
        } else if(type == Constants.SpatialType.Meters) {
            return Math.sqrt(Math.pow(p1.getX() - p2.getX(),2)
                    + Math.pow(p1.getY() - p2.getY(),2)
                    + Math.pow(p1.getT() - p2.getT(),2));
        } else {
            throw new LSTFilterException("Unrecognized coordinate type for GPSLib");
        }
    }

    //This method is STPoint dimension safe for all dimensions - it supports inclusion-exclusion of any dimension for both p1 and p2
    public static double distanceBetween(STPoint p1, STPoint p2, Constants.SpatialType type) throws LSTFilterException {
        if(!p1.hasT() || !p2.hasT()){
            return spatialDistanceBetween(p1, p2, type);
        } else if((!p1.hasX() || !p2.hasX())){
            p1.setX(-Float.MAX_VALUE);
            p2.setX(-Float.MAX_VALUE);
            return spatialTemporalDistanceBetween(p1, p2, type);
        } else if(!p1.hasY() || !p2.hasY()){
            p1.setY(-Float.MAX_VALUE);
            p2.setY(-Float.MAX_VALUE);
            return spatialTemporalDistanceBetween(p1, p2, type);
        } else {
            return spatialTemporalDistanceBetween(p1, p2, type);
        }
    }


    public static float latOffsetFromDistance(STPoint reference, double dist) {
        double distRad = dist / 6371;
        double bearing = 0;
        double lat1 = Math.toRadians(reference.getY());
        double long1 = Math.toRadians(reference.getX());

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distRad) + Math.cos(lat1) * Math.sin(distRad));
        double long2 = long1 + Math.atan2(0, Math.cos(distRad) - Math.sin(lat1) * Math.sin(lat2));

        return (float) Math.toDegrees(Math.abs(lat2 - lat1));

//        var φ2 = Math.asin( Math.sin(φ1)*Math.cos(d/R) +
//                Math.cos(φ1)*Math.sin(d/R)*Math.cos(brng) );
//        var λ2 = λ1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(φ1),
//                Math.cos(d/R)-Math.sin(φ1)*Math.sin(φ2));

    }

    //
    public static float longOffsetFromDistance(STPoint reference, double dist){
        double distRad = dist / 6371;
        double bearing = 90;
        double lat1 = Math.toRadians(reference.getY());
        double long1 = Math.toRadians(reference.getX());

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distRad));
        double long2 = long1 + Math.atan2(Math.sin(distRad) * Math.cos(lat1), Math.cos(distRad) - Math.sin(lat1) * Math.sin(lat2));

        return (float) Math.toDegrees(Math.abs(long2 - long1));
    }
}
