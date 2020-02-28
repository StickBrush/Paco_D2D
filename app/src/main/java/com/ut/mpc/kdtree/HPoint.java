/**
 * %SVN.HEADER%
 * 
 * based on work by Simon Levy
 * http://www.cs.wlu.edu/~levy/software/kd/
 */
package com.ut.mpc.kdtree;

// Hyper-Point class supporting KDTree class

class HPoint {

    protected float[] coord;

    protected HPoint(int n) {
        coord = new float[n];
    }

    protected HPoint(float[] x) {

        coord = new float[x.length];
        for (int i = 0; i < x.length; ++i)
            coord[i] = x[i];
    }

    protected Object clone() {

        return new HPoint(coord);
    }

    protected boolean equals(HPoint p) {
        // seems faster than java.util.Arrays.equals(), which is not
        // currently supported by Matlab anyway
        for (int i = 0; i < coord.length; ++i)
            if (coord[i] != p.coord[i])
                return false;

        return true;
    }

    protected static float sqrdist(HPoint x, HPoint y) {

        float dist = 0;

        for (int i = 0; i < x.coord.length; ++i) {
            float diff = (x.coord[i] - y.coord[i]);
            dist += (diff * diff);
        }

        return dist;

    }

    protected static float eucdist(HPoint x, HPoint y) {

        return (float) Math.sqrt(sqrdist(x, y));
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < coord.length; ++i) {
            s = s + coord[i] + " ";
        }
        return s;
    }

}
