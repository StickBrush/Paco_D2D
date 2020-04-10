package com.ut.mpc.utils;

import android.util.Log;

import com.ut.mpc.setup.Constants;
import com.ut.mpc.setup.Initializer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import nathanielwendt.mpc.ut.edu.paco.Data.PlaceData;

import static com.ut.mpc.setup.Constants.PoK;
import static com.ut.mpc.setup.Constants.SPATIAL_TYPE;
import static com.ut.mpc.setup.Constants.SpatialType;

public class LSTFilter {
	private STStorage structure;
	private boolean smartInsert = false;//true
    private boolean kdCache = true;
	private float gridFactor;//

	/**
	 * Creates a new LSTFilter containing the given structure.
	 * @param structure - STStorage structure to include within the LSTFilter
	 */
	public LSTFilter(STStorage structure, Initializer initializer){
		this.structure = structure;
        initializer.initialize();
	}

	//
	public void setRefPoint(STPoint input) { Constants.PoK.updateConfig(input); }
	public void setSmartInsertThresh(double input) { Constants.SmartInsert.INS_THRESH = input; }
	//


    public void setKDCache(boolean val){
        this.kdCache = val;
    }

	/**
	 * Inserts a point into the structure.  Smart insertion is
	 * determined from the LSTFilter's smartInsert member variable
	 * @param item point to insert into the structure
	 */
	public void insert(STPoint item) {
		if (smartInsert)
			this.smartInsert(item);
		else
			this.stdInsert(item);
	}

	public void insert(STPoint item, String placeName, String uri) {
		if (smartInsert)
			this.smartInsert(item, placeName, uri);
		else
			this.stdInsert(item, placeName, uri);
	}//

    public void insert(STPoint item, STPoint gridGran){

    }

    /**
     *  Method to adapt to legacy queries with region instead of query window
     *  @param region - spatial and temporal bounds for which to query for the PoK
     */
    public double windowPoK(STRegion region){
        STPoint gridGran = new STPoint(PoK.X_CUBE, PoK.Y_CUBE, PoK.T_CUBE);
        return this.windowPoK(new QueryWindow(region, gridGran));
    }

	/**
	 * Determines the PoK for a given STRegion
	 * @param window - query window with spatial and temporal bounds for query as well as granularity of the computation
	 * @return PoK value summarizing the coverage of the desired region
	 */
	public double windowPoK(QueryWindow window) {
        float xGridGran = window.getGridGran().getX();
        float yGridGran = window.getGridGran().getY();
        float tGridGran = window.getGridGran().getT();

        float xCenterOffset = xGridGran / 2;
        float yCenterOffset = yGridGran / 2;
        float tCenterOffset = tGridGran / 2;

        //boundPoints must have 3 dimensions, mins and maxs doesn't necessarily need to
        STPoint minFetch = new STPoint(window.getRegion().getMins());
        STPoint maxFetch = new STPoint(window.getRegion().getMaxs());

        //extend the region to include all possible points which could contribute
        STPoint.add(minFetch, new STPoint(-xCenterOffset, -yCenterOffset, -tCenterOffset));
        STPoint.add(maxFetch, new STPoint(xCenterOffset, yCenterOffset, tCenterOffset));

        //List<STPoint> boundPoints = structure.range(window.getRegion());
        List<STPoint> boundPoints = structure.range(new STRegion(minFetch, maxFetch));
		Log.d("checkBoundPoints", boundPoints.toString());

        if(boundPoints.size() == 0){
            return 0.0;
        }

        // Effective bounds are the bounds of the points actually found in the range query above
        // Effective bounds must exist in all three dimensions
        STPoint effMinBounds = new STPoint();
        STPoint effMaxBounds = new STPoint();
        for(STPoint point : boundPoints){
            effMinBounds.updateMin(point);
            effMaxBounds.updateMax(point);
        }
        Log.d("effMinBounds", effMinBounds.toString());
        Log.d("effMaxBounds", effMaxBounds.toString());

        /*
        Min and Max Bounds are the bounds upon which we evaluate the sub-cubes.
        We set them initially to the effective bounds of the points
         */
        STPoint minQueryBounds = window.getRegion().getMins();
        STPoint maxQueryBounds = window.getRegion().getMaxs();

        STPoint minBounds = new STPoint(effMinBounds);
        STPoint maxBounds = new STPoint(effMaxBounds);

        float effXSpan = effMaxBounds.getX() - effMinBounds.getX();
        float effYSpan = effMaxBounds.getY() - effMinBounds.getY();
        float effTSpan = effMaxBounds.getT() - effMinBounds.getT();

        if(effXSpan < xGridGran){
            float midX = (effXSpan / 2) + effMaxBounds.getX();
            minBounds.setX(midX - xCenterOffset);
			maxBounds.setX((midX - xCenterOffset) + 2*xCenterOffset);//
            //maxBounds.setX(midX + xCenterOffset);
        } else {
            minBounds.setX(STPoint.maxValidDim(effMinBounds.getX(), minQueryBounds.getX()));
            maxBounds.setX(STPoint.minValidDim(effMaxBounds.getX(), maxQueryBounds.getX()));
        }
        if(effYSpan < yGridGran){
            float midY = (effYSpan / 2) + effMaxBounds.getY();
            minBounds.setY(midY - yCenterOffset);
			maxBounds.setY((midY - yCenterOffset) + 2*yCenterOffset);//
            //maxBounds.setY((midY - yCenterOffset) + 2*yCenterOffset);
        } else {
            minBounds.setY(STPoint.maxValidDim(effMinBounds.getY(), minQueryBounds.getY()));
            maxBounds.setY(STPoint.minValidDim(effMaxBounds.getY(), maxQueryBounds.getY()));
        }
        if(effTSpan < tGridGran){
            float midT = (effTSpan / 2) + effMaxBounds.getT();
            minBounds.setT(midT - tCenterOffset);
			maxBounds.setT((midT - tCenterOffset) + 2*tCenterOffset);//
            //maxBounds.setT(midT + tCenterOffset);
        } else {
            minBounds.setT(STPoint.maxValidDim(effMinBounds.getT(), minQueryBounds.getT()));
            maxBounds.setT(STPoint.minValidDim(effMaxBounds.getT(), maxQueryBounds.getT()));
        }

        if(SPATIAL_TYPE == SpatialType.GPS){
            STPoint.add(minBounds, Constants.NEG_FLOAT_BUDGE);
            STPoint.add(maxBounds, Constants.POS_FLOAT_BUDGE);
        }

        //building cache index might not have all 3 dimensions, must create accordingly
        double totalGridCount = 0.0;

        STStorage cacheStore;
        double windowVolume = window.getRegion().getNVolume()/3600;
        double evalVolume = 0.0f;
        STRegion evalRegion = new STRegion(minBounds, maxBounds);
        if(this.kdCache){
            if(window.hasSpaceBounds() && window.hasTimeBounds()){
                cacheStore = KDTreeAdapter.makeBalancedTree(3,0,boundPoints);
                	evalVolume = evalRegion.getNVolume(true, true, true);
            } else if(window.hasSpaceBounds() && !window.hasTimeBounds()){
                cacheStore = KDTreeAdapter.makeBalancedTree(2,0,boundPoints);
                evalVolume = evalRegion.getNVolume(true, true, false);
            } else if(!window.hasSpaceBounds() && window.hasTimeBounds()){
                cacheStore = KDTreeAdapter.makeBalancedTree(1,2,boundPoints);
                evalVolume = evalRegion.getNVolume(false, false, true);
            } else {
                return 0.0;
            }
        } else {
            SpatialArray cacheStoreArr = new SpatialArray();
            cacheStoreArr.setPoints(boundPoints);
            cacheStore = cacheStoreArr;
        }

        STPoint boundValues = new STPoint(xGridGran, yGridGran, tGridGran);
        //STPoint boundValues = new STPoint(PoK.X_RADIUS, PoK.Y_RADIUS, PoK.T_RADIUS);

        // Refactored to not include the "blank" cube optimization
        double totalWeight = 0.0;
        double regionWeight = 0.0;

        int effCubeCount = 0;
        for(float x = minBounds.getX(); x < maxBounds.getX(); x = x + xGridGran){//devide ....
            for(float y = minBounds.getY(); y < maxBounds.getY(); y = y + yGridGran){
                for(float t = minBounds.getT(); t < maxBounds.getT(); t = t + tGridGran){
					STPoint centerOfRegion = new STPoint(x + xCenterOffset,
														 y + yCenterOffset,
							                             t + tCenterOffset);
                    try {
                        STRegion miniRegion = GPSLib.getSpaceBoundQuick(centerOfRegion, boundValues, SPATIAL_TYPE);
                        List<STPoint> activePoints = cacheStore.range(miniRegion);//all the points in miniRegion
						System.out.println(activePoints);
                        //List<STPoint> activePoints = structure.range(miniRegion);
                        regionWeight = this.getPointsPoK(centerOfRegion, activePoints);
                        effCubeCount++;
                        totalWeight += regionWeight;
                    } catch (LSTFilterException e){
                        e.printStackTrace();
                    }
				}
			}
		}

        double volDiff = windowVolume - evalVolume;
        double cubeVolume = (xGridGran * yGridGran * tGridGran);
        double blankCubes = Math.floor(volDiff / cubeVolume);

		if(volDiff < 0){
            //throw new RuntimeException("volume difference should never be less than 0");
			blankCubes = 0.0;//
        }

        return totalWeight / (effCubeCount + blankCubes);
	}

	private double getGridCount(STPoint ref1, STPoint ref2, STPoint granularities){
		double xComp = (Math.abs(ref1.getX() - ref2.getX())) / granularities.getX();
		double xIter = Math.floor(xComp) + Math.ceil(xComp - Math.floor(xComp));
		double yComp = (Math.abs(ref1.getY() - ref2.getY())) / granularities.getY();
		double yIter = Math.floor(yComp) + Math.ceil(yComp - Math.floor(yComp));
		double tComp = (Math.abs(ref1.getT() - ref2.getT())) / granularities.getT();
		double tIter = Math.floor(tComp) + Math.ceil(tComp - Math.floor(tComp));
		return xIter * yIter * tIter;
	}

    public double pointPoK(STPoint point){
        return this.pointPoK(point, STPoint.unitGran());
    }

	/**
	 * Retrieves the PoK for a given point.
	 * Uses the default space bound values to form a region around the point
	 * @param point - point to query around
	 * @return PoK value
	 */
	public double pointPoK(STPoint point, STPoint boundValues){
		try {
			STRegion miniRegion = GPSLib.getSpaceBoundQuick(point, boundValues, SPATIAL_TYPE);
			List<STPoint> activePoints = structure.range(miniRegion);
			return this.getPointsPoK(point, activePoints);
		} catch (LSTFilterException e){
			e.printStackTrace();
			return Float.NaN;
		}
	}

	/**
	 * Finds the path between two points.  If the points don't match any in the structure
	 * the nearest neighbors are chosen.
	 * @param start - beginning point of path
	 * @param end - terminating point of path
	 * @return list of the sequentially (temporal) ordered points
	 */
	public List<STPoint> findPath(STPoint start, STPoint end, int dim) {//
		return structure.getSequence(start, end, dim);
	}

    /**
     * Convenience method for clearing the structure
     */
    public void clear(){
        structure.clear();
    }

    /**
     * Sets the structure's smart insert flag
     * @param isSmart - value to set the smart insert flag to
     */
    public void setSmartInsert(boolean isSmart){ this.smartInsert = isSmart; }

    /**
     * Gets the size of the structure
     * @return size of the structure, number of data points
     */
    public int getSize(){
        return structure.getSize();
    }


	/*
	 *  *********************** Private Methods ********************************
	 *
	 */


	/**
	 * Finds the PoK for the given points about the center point
	 * The PoK is found using the center point as the reference point for finding distances
	 * @param center - Reference point from which to perform the calculation
	 * @param points - points used to determine impact on PoK w.r.t. center point
	 */
	private double getPointsPoK(STPoint center, List<STPoint> points){
		double spatialDist, temporalDist, spatialCont, temporalCont, tileWeight, contribution;

		List<Double> nearby = new ArrayList<Double>();
		tileWeight = 0;
        for(STPoint currPoint : points){
			try {
                spatialDist = GPSLib.spatialDistanceBetweenFast(center, currPoint, SPATIAL_TYPE);
                temporalDist = GPSLib.distanceT(center, currPoint);
			} catch (LSTFilterException e){
				e.printStackTrace();
				return Double.NaN;
			}

            // Shouldn't need the Math.min anymore since a point shouldn't be counted unless it's
            // within the effective RADIUS, however floating point error might make it worth keeping this
			spatialCont =  (-1 * Math.min(spatialDist, PoK.SPACE_RADIUS));
			temporalCont = (-1 * Math.min(temporalDist, PoK.TEMPORAL_RADIUS));
			contribution = ((spatialCont + PoK.SPACE_WEIGHT) * (temporalCont + PoK.TEMPORAL_WEIGHT)) / (PoK.SPACE_WEIGHT * PoK.TEMPORAL_WEIGHT);
			Log.d("checkPoK_centerOfRegion_contribution", Double.toString(contribution));
			//contribution = ((spatialCont + PoK.SPACE_RADIUS)*PoK.SPACE_WEIGHT * (temporalCont + PoK.TEMPORAL_RADIUS)*PoK.TEMPORAL_WEIGHT) / (PoK.SPACE_RADIUS * PoK.TEMPORAL_RADIUS);
			nearby.add(contribution);
		}

		if(points.size() > 0 && nearby.size() > 0){ //make sure not to add a point with an empty activePoints tree
			double[] aggResults = new double[2];
			aggResults[0] = 0;
			aggResults[1] = 0;
			List<Double> empty = new ArrayList<Double>();
			int numTrimmed = this.trimNearby(nearby);
//			this.getAggPoK(aggResults,empty, nearby.subList(numTrimmed, nearby.size() - 1));//
			this.getAggPoK(aggResults,empty,nearby);
			//recurseIterations += aggResults[1];
			if(aggResults[0] > 0)
				tileWeight = aggResults[0];
			else
				tileWeight = 0.0;
		}
		return tileWeight;
	}

	/**
	 * Trims the list of nearby points until it satisfies the TRIM_THRESH
	 * Values are first sorted before trimming so most influential points will be kept
	 * Sort after building the list as suggested in: http://stackoverflow.com/questions/168891/
	 * @param nearby - list of points to be trimmed
	 * @return number of trimmed points
	 */
	private int trimNearby(List<Double> nearby){
		int initialSize = nearby.size();
		Quicksort qs = new Quicksort();
		qs.sort(nearby, 0, nearby.size() - 1);
		while(nearby.size() > PoK.TRIM_THRESH){
			nearby.remove(0);
		}
		return initialSize - nearby.size();
	}

//	public List<STPoint> nearestNeighbor(STPoint a, STPoint b, int x, int dim){
//		return structure.nearestNeighbor(a, b, x, dim);
//	}

	/**
	 * Uses the inclusion-exclusion principle to determine the aggregate probability of points
	 * Each possible combination of points is generated and summed or subtracted according to the incl-excl principle
	 * @param sum - store the result in sum[0], iteration count in sum[1]
	 * @param active - active list of points, pass in an empty List
	 * @param rest - remaining list of points, pass in the list of points to be computed
	 */
	public void getAggPoK(double[] sum, List<Double> active, List<Double> rest){
		sum[1]++;
		if(rest.size() == 0){
			double sign;
			if((active.size() + 1) % 2 == 1) //this acts as the (-1)^(k-1) term for alternating subtraction and addition
				sign = -1;
			else
				sign = 1;
			//perform the probability equivalent of AND of the "set", which is multiplication of the probabilities
			double andValue = 0.0;
			for(int i = 0; i < active.size(); i++){
				if(i == 0)
					andValue = active.get(i);
				else
					andValue *= active.get(i);
			}
			sum[0] += andValue * sign;
		} else {
			//shallow copy of lists
			List<Double> next1 = new ArrayList<Double>(active);
			List<Double> next2 = new ArrayList<Double>(rest);
			List<Double> next3 = new ArrayList<Double>(active);
			next1.add(rest.get(0));
			next2.remove(0);

			//recursively call subsets
			this.getAggPoK(sum,next1,next2);
			this.getAggPoK(sum,next3,next2);
		}
	}

	private void smartInsert(STPoint point) {
		double pok = this.pointPoK(point);
		if(pok <= Constants.SmartInsert.INS_THRESH){
			this.stdInsert(point);
		}
	}

	private void smartInsert(STPoint point, String placeName, String uri) {
		double pok = this.pointPoK(point);
		if(pok <= Constants.SmartInsert.INS_THRESH){
			this.stdInsert(point, placeName, uri);
		}
	}//

	private void stdInsert(STPoint point) {
		structure.insert(point);
	}


	/////////////
	private void stdInsert(STPoint point, String placeName, String uri) {
		structure.insert(point, placeName, uri);
	}

	public List<PlaceData> getPlaces(){
		List<PlaceData> list = structure.getPlaces();
		return list;
	}

	public List<PlaceData> getPlacesByRange(STRegion range){
		List<PlaceData> list = structure.getPlacesByRange(new STRegion(range.getMins(), range.getMaxs()));
		return list;
	}

	public List<STPoint> rangeByT(STRegion range){
		List<STPoint> list = structure.rangeByT(new STRegion(range.getMins(), range.getMaxs()));
		return list;
	}

	public void delete(String name){
		structure.delete(name);
	}//

	public List<STPoint> range(STRegion range){
		List<STPoint> list = structure.range(new STRegion(range.getMins(), range.getMaxs()));
		return list;
	}

	public void setGridFactor(double GridFactor){
		float gridTemp = (float) GridFactor;
		this.gridFactor = gridTemp;
	}
	/////////////
}
