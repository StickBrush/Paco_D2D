package com.ut.mpc.utils;

import android.util.Log;

import java.math.BigDecimal;

/**
 * Creates a range object commonly used for queries
 * The range can be conceptualized as 2 STPoints, one for minimums and
 * one for maximums.  In 3 dimensions, the range can be visualized as
 * a cube with the mins point in one of the upper corners and the maxs
 * point in one of the lower corners
 */
public class STRegion {
	private STPoint mins;
	private STPoint maxs;
	
	public STRegion(){}
	
	public STRegion(STPoint p1, STPoint p2){
		if(mins == null){
			mins = new STPoint(p1);
		}
		if(maxs == null){
			maxs = new STPoint(p1);
		}
		mins.updateMin(p1);
		mins.updateMin(p2);
		maxs.updateMax(p1);
		maxs.updateMax(p2);
	}
	
	/**
	 * Adds a point to the range, if the point does not contribute to the range's
	 * minimum or maximum values, it will not affect the range
	 * @param point point to add to the range
	 */
	public void addPoint(STPoint point){
		if(this.mins == null || this.maxs == null){
			this.mins = new STPoint(point);
			this.maxs = new STPoint(point);
		} else {
			this.mins.updateMin(point);
			this.maxs.updateMax(point);	
		}
	}

    //Determines if a given point lies within the region bounds
    //Every dimension that both the point and region have will be evaluated
    public boolean withinRegion(STPoint point){
        if(point.hasX()){
            double x = point.getX();
            if(x > this.maxs.getX() || x < this.mins.getX()){
                return false;
            }
        }
        if(point.hasY()){
            double y = point.getY();
            if(y > this.maxs.getY() || y < this.mins.getY()){
                return false;
            }
        }
        if(point.hasT()){
            double t = point.getT();
            if(t > this.maxs.getT() || t < this.mins.getT()){
                return false;
            }
        }
        return true;
    }
	
	/**
	 * Retrieves the range's minimum values
	 * @return STPoint with minimum values for each dimension
	 */
	public STPoint getMins(){
		return this.mins;
	}
	
	/**
	 * Retrieves the range's maximum values
	 * @return STPoint with maximum values for each dimension
	 */
	public STPoint getMaxs(){
		return this.maxs;
	}
	
	public String toString(){
		return this.mins.toString() + this.maxs.toString();
	}

	public static STRegion fromString(String input){
		try{
		String[] temp = input.split(" ");
		STPoint minPoint = STPoint.fromString("X: "+temp[2]+ " Y: "+temp[4] + " T: "+temp[6]);
		STPoint maxPoint = STPoint.fromString("X: "+temp[8]+ " Y: "+temp[10] + " T: "+temp[12]);
		return new STRegion(minPoint, maxPoint);
		}
		catch (Exception e){
			Log.d("Exception", e.toString());
			return null;
		}
	}

    public double getVolume(){
        return this.maxs.getX() - this.mins.getX() *
                this.maxs.getY() - this.mins.getY() *
                this.maxs.getT() - this.mins.getT();
    }

    public double getNVolume(){
        float temp = 1.0f;
        return this.getNVolume(this.mins.hasX(), this.mins.hasY(), this.mins.hasT());
    }

    public double getNVolume(boolean x, boolean y, boolean t){
        float temp = 1.0f;
        if(x){
            temp *= this.maxs.getX() - this.mins.getX();
        }
        if(y){
            temp *= this.maxs.getY() - this.mins.getY();
        }
        if(t){
			temp *= this.maxs.getT() - this.mins.getT();
        }
        return temp;
    }

}
