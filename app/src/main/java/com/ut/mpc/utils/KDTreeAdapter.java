package com.ut.mpc.utils;

import com.ut.mpc.kdtree.KDTree;

import java.util.ArrayList;
import java.util.List;

import nathanielwendt.mpc.ut.edu.paco.Data.PlaceData;

public class KDTreeAdapter implements STStorage {
	public KDTree kdtree;
    public int dims;
    public int offsetDim;
	
	public KDTreeAdapter(int dims){
        this.dims = dims;
        kdtree = new KDTree(dims);
	}
	
	public void insert(STPoint point){
        float[] rawPoint;
        if(dims == 3 && offsetDim == 0){
            rawPoint = new float[]{point.getX(), point.getY(), point.getT()};
        } else if(dims == 2 && offsetDim == 0){
            rawPoint = new float[]{point.getX(), point.getY()};
        } else if(dims == 1 && offsetDim == 2){
            rawPoint = new float[]{point.getT()};
        } else {
            throw new RuntimeException("unsupported dims and offset dims settings");
        }
        kdtree.insert(rawPoint, point);
	}

    //serves when have dimen < 3 but don't want default ordering of x,y,t
    //for example, when want 1 dimensional but don't want to use x, set offset to 2 to get t
    public void setOffsetDim(int offset){
        this.offsetDim = offset;
    }

    public List<STPoint> range(STRegion region){
        STPoint mins = region.getMins();
        STPoint maxs = region.getMaxs();

        float[] minsRaw;
        float[] maxsRaw;
        if(dims == 3 && offsetDim == 0){
            minsRaw = new float[]{mins.getX(), mins.getY(), mins.getT()};
            maxsRaw = new float[]{maxs.getX(), maxs.getY(), maxs.getT()};
        } else if(dims == 2 && offsetDim == 0){
            minsRaw = new float[]{mins.getX(), mins.getY()};
            maxsRaw = new float[]{maxs.getX(), maxs.getY()};
        } else if(dims == 1 && offsetDim == 2){
            minsRaw = new float[]{mins.getT()};
            maxsRaw = new float[]{maxs.getT()};
        } else {
            throw new RuntimeException("unsupported dims and offset dims settings");
        }
        Object[] results = kdtree.range(minsRaw, maxsRaw);
        List<STPoint> points = new ArrayList<STPoint>();
        for(Object result : results){
            points.add((STPoint) result);
        }
        return points;
    }

    @Override
    public List<STPoint> nearestNeighbor(STPoint needle, STPoint boundValues, int n) {
        throw new RuntimeException("naive structure shouldn't need this method");
    }

    @Override
    public List<STPoint> getSequence(STPoint start, STPoint end) {
        throw new RuntimeException("naive structure shouldn't need this method");
    }

    @Override
    public STRegion getBoundingBox() {
        throw new RuntimeException("naive structure shouldn't need this method");
    }

    @Override
    public void clear() {
        throw new RuntimeException("naive structure shouldn't need this method");
    }

    public static KDTreeAdapter makeBalancedTree(int dims, int offsetDim, List<STPoint> points){
        KDTreeAdapter kdtree = new KDTreeAdapter(dims);
        kdtree.offsetDim = offsetDim;
        int treeSize = points.size();
        kdtree.makeTreeRecurse(points,0,treeSize - 1, 0);
        return kdtree;
    }

    //int dim 0-x, 1-y, 2-t
    private void makeTreeRecurse(List<STPoint> points, int start, int end, int dim){
        if(start <= end){
            Quicksort.sort(points, start, end, dim);

            //find median and insert into new tree
            int median = ((end - start) / 2) + start;
            this.insert(points.get(median));

            int nextDim = ((dim + 1) % this.dims) + this.offsetDim;
            //recurse on left part of array
            makeTreeRecurse(points, start, median - 1, nextDim);

            //recurse on right part of array
            makeTreeRecurse(points, median + 1, end, nextDim);
        }
    }

    public int getSize(){
        return kdtree.getSize();
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

