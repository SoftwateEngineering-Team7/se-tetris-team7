package org.extension;

import static org.junit.Assert.*;

import org.junit.Test;

public class PointTest {

    @Test
    public void testPointToString()
    {
        Point point = new Point(1, 2);
        String expected = "(1, 2)";
        assertEquals(expected, point.toString());
    }

    @Test
    public void testPointEquals()
    {
        var point1 = new Point(1, 2);
        var point2 = new Point(1, 2);
        var point3 = new Point(2, 3);

        assertEquals(point1, point2);
        assertNotEquals(point1, point3);
    }

    @Test
    public void testPointAdd()
    {
        var point = new Point(1, 2);
        var result = point.add(2, 3);
        var expected = new Point(3, 5);

        assertEquals(expected, result);

        result = point.add(new Point(2, 3));
        assertEquals(expected, result);
    } 

    @Test
    public void testPointSubtract()
    {
        var point = new Point(5, 7);
        var result = point.subtract(2, 3);
        var expected = new Point(3, 4);

        assertEquals(expected, result);

        result = point.subtract(new Point(2, 3));
        assertEquals(expected, result);
    }
}
