package com.ut.mpc.tests;

import com.ut.mpc.utils.STPoint;
import com.ut.mpc.utils.STRegion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class STPointTests {

	@Test
	public void testUpdateMinMax() {
        STPoint min = new STPoint();
        STPoint max = new STPoint();

        min.updateMin(new STPoint(0,1,2));
        max.updateMax(new STPoint(0,1,2));

        assertEquals(0, min.getX(), .0001f);
        assertEquals(1, min.getY(), .0001f);
        assertEquals(2, min.getT(), .0001f);

        assertEquals(0, max.getX(), .0001f);
        assertEquals(1, max.getY(), .0001f);
        assertEquals(2, max.getT(), .0001f);
    }

    @Test
    public void testUpdateMinMaxNotAllDim() {
        STPoint min = new STPoint();
        STPoint max = new STPoint();

        min.updateMin(new STPoint(0,1));
        max.updateMax(new STPoint(0,1));

        assertEquals(0, min.getX(), .0001f);
        assertEquals(1, min.getY(), .0001f);
        assertEquals(false, min.hasT());

        assertEquals(0, max.getX(), .0001f);
        assertEquals(1, max.getY(), .0001f);
        assertEquals(false, max.hasT());
    }

    @Test
    public void testToSTring(){
        STPoint p1 = new STPoint(32.993929949f, 43.49394992f, 194930000);
        System.out.println(p1);

        float f = 39.929219329294f;
        double d = 39.929219329294;
        System.out.println(f);
        System.out.println(d);
        System.out.println((double)f);

        System.out.println(String.format("%.10f", f));
        System.out.println(String.format("%.10f", d));

    }

}
