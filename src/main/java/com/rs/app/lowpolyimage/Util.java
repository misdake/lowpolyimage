package com.rs.app.lowpolyimage;

import com.rs.math.geometry.shape.Point;
import com.rs.math.geometry.shape.Triangle;

public class Util {

    private static double sign(Point p1, Point p2, Point p3) {
        return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y);
    }

    static boolean pointInTriangle(Point pt, Triangle t) {
        boolean b1 = sign(pt, t.a, t.b) < 0.0f;
        boolean b2 = sign(pt, t.b, t.c) < 0.0f;
        boolean b3 = sign(pt, t.c, t.a) < 0.0f;
        return ((b1 == b2) && (b2 == b3));
    }

}
