package nathanielwendt.mpc.ut.edu.paco;

//import android.app.Application;
//import android.content.Context;
//import android.test.ApplicationTestCase;
//
//import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
///**
// * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
// */
//public class ApplicationTest extends ApplicationTestCase<Application> {
//    public ApplicationTest() {
//        super(Application.class);
//    }
//}


////New Version////
import android.content.Context;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ut.mpc.setup.Constants;
import com.ut.mpc.setup.Initializer;
import com.ut.mpc.utils.LSTFilter;
import com.ut.mpc.utils.STPoint;
import com.ut.mpc.utils.STRegion;
import com.ut.mpc.utils.SpatialArray;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import nathanielwendt.mpc.ut.edu.paco.utils.SQLiteRTree;

@RunWith(AndroidJUnit4ClassRunner.class)


public class ApplicationTest {
//    @Test
//    public void useAppContext() throws Exception {
//
//    //Context of the app under test.
//    Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//    assertEquals("nathanielwendt.mpc.ut.edu.paco", appContext.getPackageName());
//    }

    private Initializer initializer;

    @Before
    public void setUp(){
        initializer = new Initializer.Builder().spatialType(Constants.SpatialType.Meters)
                .spaceWeight(1).temporalWeight(1).refPointDefault()
                .spaceRadius(1).temporalRadius(1).trimThresh(10).build();
    }

    @Test
    public void testPointPoKSweepTemporal_new(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LSTFilter filter = new LSTFilter(new SQLiteRTree(appContext, "RTreeMain"), initializer);
        filter.setSmartInsert(false);
        filter.clear();
        filter.insert(new STPoint(1,1,10), "testPointPoKSweepTemporal_new", "NULL");

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

    @Test
    public void testPointPoKSweepSpatial_new(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LSTFilter filter = new LSTFilter(new SQLiteRTree(appContext, "RTreeMain"), initializer);
        filter.setSmartInsert(false);
        filter.clear();
        filter.insert(new STPoint(1,1,10), "testPointPoKSweepTemporal_new", "NULL");
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

    @Test
    public void testPointPoKMultiple_new(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LSTFilter filter = new LSTFilter(new SQLiteRTree(appContext, "RTreeMain"), initializer);
        filter.setSmartInsert(false);
        filter.clear();
        filter.insert(new STPoint(1,1,1), "test1", "NULL");
        filter.insert(new STPoint(1.1f,1.1f,1.1f), "test2", "NULL");
        filter.insert(new STPoint(1.2f,1.2f,1.2f), "test3", "NULL");
        filter.insert(new STPoint(1.3f,1.3f,1.3f), "test4", "NULL");
        double result = filter.pointPoK(new STPoint(1f,1f,1f));
        //System.out.println(result);
    }

    @Test
    public void testWindowPoK_new(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LSTFilter filter = new LSTFilter(new SQLiteRTree(appContext, "RTreeMain"), initializer);
        filter.setSmartInsert(false);
        filter.clear();
        filter.insert(new STPoint(0.5f,0.5f,0.5f), "testWindowPoK_new", "NULL");

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

    @Test
    public void testWindowPoKExcludeTemporal_new(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LSTFilter filter = new LSTFilter(new SQLiteRTree(appContext, "RTreeMain"), initializer);
        filter.setSmartInsert(false);
        filter.clear();
        filter.insert(new STPoint(0.5f,0.5f,0.5f), "testWindowPoKExcludeTemporal_new", "NULL");

        STPoint min = new STPoint(0f,0f);
        STPoint max = new STPoint(1f,1f);
        double result = filter.windowPoK(new STRegion(min, max));
        assertEquals(1.0f, result, .0001f);
    }

    @Test
    public void testWindowPoKExcludeSpatial_new(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LSTFilter filter = new LSTFilter(new SQLiteRTree(appContext, "RTreeMain"), initializer);
        filter.setSmartInsert(false);
        filter.clear();
        filter.insert(new STPoint(0.5f,0.5f,0.5f), "testWindowPoKExcludeSpatial_new", "NULL");

        STPoint min = new STPoint();
        min.setT(0f);
        STPoint max = new STPoint();
        max.setT(1f);
        double result = filter.windowPoK(new STRegion(min, max));
        assertEquals(1.0f, result, .0001f);
    }

    @Test
    public void testWindowPoKNoPoints_new(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LSTFilter filter = new LSTFilter(new SQLiteRTree(appContext, "RTreeMain"), initializer);
        filter.setSmartInsert(false);
        filter.clear();
        filter.insert(new STPoint(0.5f,0.5f,0.5f), "testWindowPoKNoPoints_new", "NULL");

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
        filter.clear();//
    }
}