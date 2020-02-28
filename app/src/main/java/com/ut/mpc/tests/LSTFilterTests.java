package com.ut.mpc.tests;

import java.util.ArrayList;
import java.util.List;

import com.ut.mpc.setup.Initializer;
import com.ut.mpc.utils.SpatialArray;
import nathanielwendt.mpc.ut.edu.paco.utils.SQLiteRTree;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.ut.mpc.setup.Constants;
import com.ut.mpc.setup.Constants.PoK;
import com.ut.mpc.utils.LSTFilter;
import com.ut.mpc.utils.STPoint;
import com.ut.mpc.utils.STRegion;

public class LSTFilterTests {
    private Initializer initializer;

	@Before
	public void setUp(){
        initializer = new Initializer.Builder().spatialType(Constants.SpatialType.Meters)
                                                .spaceWeight(1).temporalWeight(1).refPointDefault()
                                                .spaceRadius(1).temporalRadius(1).trimThresh(10).build();
	}

	@Test
	public void testPointPoKSweepTemporal(){
		LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
		filter.setSmartInsert(false);
		filter.insert(new STPoint(1,1,10));

		double result = filter.pointPoK(new STPoint(1,1,10f));
		assertEquals(1f, result, .0001);

		result = filter.pointPoK(new STPoint(1,1,9.5f));
		assertEquals(.5f, result, .0001);

		result = filter.pointPoK(new STPoint(1,1,10.5f));
		assertEquals(.5f, result, .0001);

        //Point is not included in the range, so result is 0
		result = filter.pointPoK(new STPoint(1,1,11f));
		assertEquals(.0f, result, .0001);

		//Point is not included in the range, so result is 0
		result = filter.pointPoK(new STPoint(1,1,11.1f));
		assertEquals(0f, result, .0001);
	}

//    @Test
//    public void testPointPoKSweepTemporal_new(){
//        LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
//        filter.setSmartInsert(false);
//        filter.insert(new STPoint(1,1,10), "testPointPoKSweepTemporal_new", " ");
//
//        double result = filter.pointPoK(new STPoint(1,1,10f));
//        assertEquals(1f, result, .0001);
//
//        result = filter.pointPoK(new STPoint(1,1,9.5f));
//        assertEquals(.5f, result, .0001);
//
//        result = filter.pointPoK(new STPoint(1,1,10.5f));
//        assertEquals(.5f, result, .0001);
//
//        //Point is not included in the range, so result is 0
//        result = filter.pointPoK(new STPoint(1,1,11f));
//        assertEquals(.0f, result, .0001);
//
//        //Point is not included in the range, so result is 0
//        result = filter.pointPoK(new STPoint(1,1,11.1f));
//        assertEquals(0f, result, .0001);
//    }

	@Test
	public void testPointPoKSweepSpatial(){
		LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
		filter.setSmartInsert(false);
		filter.insert(new STPoint(1,1,10), "testPointPoKSweepSpatial()_new", " ");
		//filter.insert(new STPoint(2,2,20));
		//filter.insert(new STPoint(3,3,30));
		//filter.insert(new STPoint(4,4,40));

		double result = filter.pointPoK(new STPoint(1f,1f,10f));
		assertEquals(1f, result, .0001);

		result = filter.pointPoK(new STPoint(0.5f,1f,10f));
		assertEquals(.5f, result, .0001);

		result = filter.pointPoK(new STPoint(0.5f,0.5f,10f));
		assertEquals(.292893f, result, .0001);

		result = filter.pointPoK(new STPoint(0f,0f,10f));
		assertEquals(0f, result, .0001);

		result = filter.pointPoK(new STPoint(-0.01f,-0.01f,10f));
		assertEquals(0f, result, .0001);
	}

//    @Test
//    public void testPointPoKSweepSpatial_new(){
//        LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
//        filter.setSmartInsert(false);
//        filter.insert(new STPoint(1,1,10));
//        //filter.insert(new STPoint(2,2,20));
//        //filter.insert(new STPoint(3,3,30));
//        //filter.insert(new STPoint(4,4,40));
//
//        double result = filter.pointPoK(new STPoint(1f,1f,10f));
//        assertEquals(1f, result, .0001);
//
//        result = filter.pointPoK(new STPoint(0.5f,1f,10f));
//        assertEquals(.5f, result, .0001);
//
//        result = filter.pointPoK(new STPoint(0.5f,0.5f,10f));
//        assertEquals(.292893f, result, .0001);
//
//        result = filter.pointPoK(new STPoint(0f,0f,10f));
//        assertEquals(0f, result, .0001);
//
//        result = filter.pointPoK(new STPoint(-0.01f,-0.01f,10f));
//        assertEquals(0f, result, .0001);
//    }

	@Test
	public void testPointPoKMultiple(){
		LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
		filter.setSmartInsert(false);
		filter.insert(new STPoint(1,1,1));
		filter.insert(new STPoint(1.1f,1.1f,1.1f));
		filter.insert(new STPoint(1.2f,1.2f,1.2f));
		filter.insert(new STPoint(1.3f,1.3f,1.3f));
		double result = filter.pointPoK(new STPoint(1f,1f,1f));
		//System.out.println(result);
	}

//    @Test
//    public void testPointPoKMultiple_new(){
//        LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
//        filter.setSmartInsert(false);
//        filter.insert(new STPoint(1,1,1), "test1", "");
//        filter.insert(new STPoint(1.1f,1.1f,1.1f), "test2", "");
//        filter.insert(new STPoint(1.2f,1.2f,1.2f), "test3", "");
//        filter.insert(new STPoint(1.3f,1.3f,1.3f), "test4", "");
//        double result = filter.pointPoK(new STPoint(1f,1f,1f));
//        //System.out.println(result);
//    }

	@Test
	public void testAggPoK(){
		LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
		List<Double> items = new ArrayList<Double>();
		double item1 = .000005;
		double item2 = .000005;
		items.add(item1);
		items.add(item2);
		for(float i=.8f; i < .99f; i+=.05f){
			items.add((double)i);
		}
		double[] result = new double[]{0,0};
		filter.getAggPoK(result, new ArrayList<Double>(), items);

		//System.out.println(item1 + item2 - (item1 * item2));
		//System.out.println(result[0]);
	}

	@Test
	public void testWindowPoK(){
		LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
		filter.setSmartInsert(false);
		filter.insert(new STPoint(0.5f,0.5f,0.5f));

		STPoint min = new STPoint(0f,0f,0f);
		STPoint max = new STPoint(1f,1f,1f);
		double result = filter.windowPoK(new STRegion(min, max));
		System.out.println(result);
        assertEquals(1.0f, result, .0001f);

        //8 cubes.  7 are empty, 1 is 100% so result is 1/8.
        max = new STPoint(2f,2f,2f);
        result = filter.windowPoK(new STRegion(min, max));
        assertEquals(0.125f, result, .0001f);

        //2 cubes.  1 is empty, 1 is 100% so result is 1/2.
        max = new STPoint(1f,1f,2f);
        result = filter.windowPoK(new STRegion(min, max));
        assertEquals(0.5f, result, .0001f);
	}

//    @Test
//    public void testWindowPoK_new(){
//        LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
//        filter.setSmartInsert(false);
//        filter.insert(new STPoint(0.5f,0.5f,0.5f), "testWindowPoK_new", " ");
//
//        STPoint min = new STPoint(0f,0f,0f);
//        STPoint max = new STPoint(1f,1f,1f);
//        double result = filter.windowPoK(new STRegion(min, max));
//        System.out.println(result);
//        assertEquals(1.0f, result, .0001f);
//
//        //8 cubes.  7 are empty, 1 is 100% so result is 1/8.
//        max = new STPoint(2f,2f,2f);
//        result = filter.windowPoK(new STRegion(min, max));
//        assertEquals(0.125f, result, .0001f);
//
//        //2 cubes.  1 is empty, 1 is 100% so result is 1/2.
//        max = new STPoint(1f,1f,2f);
//        result = filter.windowPoK(new STRegion(min, max));
//        assertEquals(0.5f, result, .0001f);
//    }

    @Test
    public void testWindowPoKExcludeTemporal(){
        LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
        filter.setSmartInsert(false);
        filter.insert(new STPoint(0.5f,0.5f,0.5f));

        STPoint min = new STPoint(0f,0f);
        STPoint max = new STPoint(1f,1f);
        double result = filter.windowPoK(new STRegion(min, max));
        assertEquals(1.0f, result, .0001f);
    }

//    @Test
//    public void testWindowPoKExcludeTemporal_new(){
//        LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
//        filter.setSmartInsert(false);
//        filter.insert(new STPoint(0.5f,0.5f,0.5f), "testWindowPoKExcludeTemporal_new", " ");
//
//        STPoint min = new STPoint(0f,0f);
//        STPoint max = new STPoint(1f,1f);
//        double result = filter.windowPoK(new STRegion(min, max));
//        assertEquals(1.0f, result, .0001f);
//    }

    @Test
    public void testWindowPoKExcludeSpatial(){
        LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
        filter.setSmartInsert(false);
        filter.insert(new STPoint(0.5f,0.5f,0.5f));

        STPoint min = new STPoint();
        min.setT(0f);
        STPoint max = new STPoint();
        max.setT(1f);
        double result = filter.windowPoK(new STRegion(min, max));
        assertEquals(1.0f, result, .0001f);
    }

//    @Test
//    public void testWindowPoKExcludeSpatial_new(){
//        LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
//        filter.setSmartInsert(false);
//        filter.insert(new STPoint(0.5f,0.5f,0.5f), "testWindowPoKExcludeSpatial_new", " ");
//
//        STPoint min = new STPoint();
//        min.setT(0f);
//        STPoint max = new STPoint();
//        max.setT(1f);
//        double result = filter.windowPoK(new STRegion(min, max));
//        assertEquals(1.0f, result, .0001f);
//    }

    @Test
    public void testWindowPoKNoPoints(){
        LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
        filter.setSmartInsert(false);
        filter.insert(new STPoint(0.5f,0.5f,0.5f));

        STPoint min = new STPoint();
        min.setT(2f);
        STPoint max = new STPoint();
        max.setT(3f);
        double result = filter.windowPoK(new STRegion(min, max));
        assertEquals(0f, result, .0001f);

        min = new STPoint(2,2,2);
        max = new STPoint(3,3,3);
        result = filter.windowPoK(new STRegion(min, max));
        assertEquals(0f, result, .0001f);
    }

//    @Test
//    public void testWindowPoKNoPoints_new(){
//        LSTFilter filter = new LSTFilter(new SpatialArray(), initializer);
//        filter.setSmartInsert(false);
//        filter.insert(new STPoint(0.5f,0.5f,0.5f), "testWindowPoKNoPoints_new", " ");
//
//        STPoint min = new STPoint();
//        min.setT(2f);
//        STPoint max = new STPoint();
//        max.setT(3f);
//        double result = filter.windowPoK(new STRegion(min, max));
//        assertEquals(0f, result, .0001f);
//
//        min = new STPoint(2,2,2);
//        max = new STPoint(3,3,3);
//        result = filter.windowPoK(new STRegion(min, max));
//        assertEquals(0f, result, .0001f);
//    }



//    @Test
//    public void testNN(){
//        SpatialArray arr = new SpatialArray();
//        arr.insert(new STPoint(0,0,0));
//        arr.insert(new STPoint(1,1,1));
//        arr.insert(new STPoint(2,2,2));
//        arr.insert(new STPoint(3,3,3));
//
//        List<STPoint> nn = arr.nearestNeighbor(new STPoint(3,3,4), 1);
//        assertEquals(new STPoint(3,3,3), nn.get(0));
//
//        nn = arr.nearestNeighbor(new STPoint(2.6f,2.6f,2.6f), 1);
//        assertEquals(new STPoint(3,3,3), nn.get(0));
//    }

//    @Test
//    public void testNNSparse(){
//        SpatialArray arr = new SpatialArray();
//        arr.insert(new STPoint(100,100,100));
//        arr.insert(new STPoint(101,101,101));
//        arr.insert(new STPoint(102,102,103));
//        arr.insert(new STPoint(104,104,104));
//
//        List<STPoint> nn = arr.nearestNeighbor(new STPoint(0,0,0), 1);
//        assertEquals(new STPoint(100,100,100), nn.get(0));
//
//        nn = arr.nearestNeighbor(new STPoint(2000,2000,2000), 1);
//        assertEquals(new STPoint(104,104,104), nn.get(0));
//    }
}
