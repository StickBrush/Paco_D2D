package com.ut.mpc.setup;

import com.ut.mpc.utils.STPoint;

/**
 * Created by nathanielwendt on 1/30/17.
 */
public class Initializer {
    private int spaceWeight;
    private int temporalWeight;
    private float spaceRadius;
    private float temporalRadius;
    private int trimThresh;
    private STPoint refPoint;
    private Constants.SpatialType spatialType;

    public static class Builder {
        private int spaceWeight;
        private int temporalWeight;
        private float spaceRadius;
        private float temporalRadius;
        private int trimThresh;
        private STPoint refPoint;
        private Constants.SpatialType spatialType;

        public Initializer build(){
            //TODO: perform checks here for proper initialization
            return new Initializer(this);
        }

        public Builder spaceWeight(int val){
            spaceWeight = val;
            return this;
        }

        public Builder temporalWeight(int val){
            temporalWeight = val;
            return this;
        }

        public Builder spaceRadius(float val){
            spaceRadius = val;
            return this;
        }

        public Builder temporalRadius(float val){
            temporalRadius = val;
            return this;
        }

        public Builder trimThresh(int val){
            trimThresh = val;
            return this;
        }

        public Builder refPoint(STPoint point) {
            refPoint = point;
            return this;
        }

        public Builder refPointDefault(){
            refPoint = Constants.DEFAULT_REF_POINT;
            return this;
        }

        public Builder spatialType(Constants.SpatialType type){
            spatialType = type;
            return this;
        }
    }

    public static Initializer vehicularDefaults(){
        return new Builder().refPoint(Constants.DEFAULT_REF_POINT)
                            .spatialType(Constants.SpatialType.GPS)
                            .trimThresh(10)
                            .temporalRadius(60 * 60 * 6).spaceRadius(1) // 6 hours in seconds, 1 km
                            .temporalWeight(1).spaceWeight(1).build();
    }

    public static Initializer pedDefaults(){
        return new Builder().refPoint(Constants.DEFAULT_REF_POINT)
                .spatialType(Constants.SpatialType.Meters)
                .trimThresh(10)
                .temporalRadius(60 * 5).spaceRadius((float)0.05) // 5 minutes in seconds, 50 meters /////was 50
                .temporalWeight(1).spaceWeight(1).build();
    }

    private Initializer(Builder builder){
        this.spaceWeight = builder.spaceWeight;
        this.temporalWeight = builder.temporalWeight;
        this.spaceRadius = builder.spaceRadius;
        this.temporalRadius = builder.temporalRadius;
        this.trimThresh = builder.trimThresh;
        this.refPoint = builder.refPoint;
        this.spatialType = builder.spatialType;
    }

    public void initialize(){
        Constants.PoK.SPACE_WEIGHT = this.spaceWeight;
        Constants.PoK.TEMPORAL_WEIGHT = this.temporalWeight;
        Constants.PoK.SPACE_RADIUS = this.spaceRadius;
        Constants.PoK.TEMPORAL_RADIUS = this.temporalRadius;
        Constants.PoK.TRIM_THRESH = this.trimThresh;
        Constants.SPATIAL_TYPE = this.spatialType;
        //updates the x,y,t cube values with reference point if GPS spatialtype
        Constants.PoK.updateConfig(this.refPoint);
    }
}
