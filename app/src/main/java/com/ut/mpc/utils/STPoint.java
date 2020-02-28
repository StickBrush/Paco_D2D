package com.ut.mpc.utils;

import com.ut.mpc.setup.Constants;
import com.ut.mpc.setup.Constants.PoK;

public class STPoint {
	private static float DEFAULT_VAL = Float.NaN;
	private float x = DEFAULT_VAL;
	private float y = DEFAULT_VAL;
	private float t = DEFAULT_VAL;
	
	public STPoint(){}

	public STPoint(STPoint other){
		this.x = other.x;
		this.y = other.y;
		this.t = other.t;
	}
	
	//x is typically longitude
	//y is typically latitude
	public STPoint(float x, float y, float t){
		this.x = x;
		this.y = y;
		this.t = t;
	}
	
	public STPoint(float x, float y){
		this(x,y,DEFAULT_VAL);
	}
	
	public STPoint(float t){
		this(DEFAULT_VAL, DEFAULT_VAL, t);
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}
	
	public boolean hasX(){
        return !Float.isNaN(this.x);
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
	
	public boolean hasY(){
        return !Float.isNaN(this.y);
	}

	public float getT() {
		return t;
	}

	public void setT(float t) {
		this.t = t;
	}
	
	public boolean hasT(){ return !Float.isNaN(this.t); }
	
	public void updateMin(STPoint next){
        if(!this.hasX()){
            this.x = next.getX();
        } else if(next.hasX() && (next.getX() < this.getX())){
            this.x = next.getX();
        }

        if(!this.hasY()){
            this.y = next.getY();
        } else if(next.hasY() && (next.getY() < this.getY())){
            this.y = next.getY();
        }

        if(!this.hasT()) {
            this.t = next.getT();
        } else if(next.hasT() && (next.getT() < this.getT())){
            this.t = next.getT();
        }
	}
	
	public void updateMax(STPoint next){
        if(!this.hasX()){
            this.x = next.getX();
        } else if(next.hasX() && (next.getX() > this.getX())){
            this.x = next.getX();
        }

        if(!this.hasY()){
            this.y = next.getY();
        } else if(next.hasY() && (next.getY() > this.getY())){
            this.y = next.getY();
        }

        if(!this.hasT()) {
            this.t = next.getT();
        } else if(next.hasT() && (next.getT() > this.getT())){
            this.t = next.getT();
        }
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == this){ 
			return true;
		}
		if(obj == null || obj.getClass() != this.getClass()){ 
			return false; 
		}
		STPoint other = (STPoint) obj;
		
		return (Float.compare(this.getX(), other.getX()) == 0) &&
				(Float.compare(this.getY(), other.getY()) == 0) &&
				(Float.compare(this.getT(), other.getT()) == 0);
	}
	
	public String toString(){
		return " X: " + String.valueOf(this.x) +
			   " Y: " + String.valueOf(this.y) +
			   " T: " + String.valueOf(this.t);
	}

    public static STPoint fromString(String input){
        int xIndex = input.indexOf("X:");
        int yIndex = input.indexOf("Y:");
        int tIndex = input.indexOf("T:");
        STPoint point = new STPoint();
        String xVal = "";
        String yVal = "";
        String tVal = "";
        for(int i = 0; i < input.length(); i++){
            if(i > xIndex + 2 && i < yIndex){
                xVal += input.charAt(i);
            } else if(i > yIndex + 2 && i < tIndex){
                yVal += input.charAt(i);
            } else if(i > tIndex + 2){
                tVal += input.charAt(i);
            }
        }
        return new STPoint(Float.parseFloat(xVal), Float.parseFloat(yVal), Float.parseFloat(tVal));
    }
	
	//linear decay function with any value for slope
	public double getTimeRelevance(float reference, float decay){
		double referenceDub = (double) reference;
		double thisDub = (double) this.getT();
		double decayDub = (double) decay;
		double offset = Math.abs(referenceDub - thisDub);
		//return ((-1 * decayDub * offset) + thisDub) / thisDub;
		return ((-PoK.TEMPORAL_WEIGHT / PoK.TEMPORAL_RADIUS) * offset +
				PoK.TEMPORAL_WEIGHT) / PoK.TEMPORAL_WEIGHT;
	}

    //for each dimension that is valid in both p1 and p2, p1 now reflects the sum of p1 and p2 along that dimension
    public static void add(STPoint p1, STPoint p2){
        if(p1.hasX() && p2.hasX()){
            p1.setX(p1.getX() + p2.getX());
        }
        if(p1.hasY() && p2.hasY()){
            p1.setY(p1.getY() + p2.getY());
        }
        if(p1.hasT() && p2.hasT()){
            p1.setT(p1.getT() + p2.getT());
        }
    }

    public static float maxValidDim(float a, float b){
        if(Float.isNaN(a)){
            return b;
        } else if(Float.isNaN(b)){
            return a;
        } else {
            return Math.max(a,b);
        }
    }

    public static float minValidDim(float a, float b){
        if(Float.isNaN(a)){
            return b;
        } else if(Float.isNaN(b)){
            return a;
        } else {
            return Math.min(a,b);
        }
    }

    public static STPoint minPoint(){
        return new STPoint(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
    }

    public static STPoint maxPoint(){
        return new STPoint(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public static STPoint unitGran() { return new STPoint(PoK.X_CUBE / 2, PoK.Y_CUBE / 2, PoK.T_CUBE / 2); }
	
}
