package com.rs.app.lowpolyimage;

import com.rs.math.geometry.shape.Point;
import com.rs.math.geometry.shape.Triangle;
import com.rs.math.geometry.util.AABB;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Processor {

    private final BufferedImage image;
    private final int           width;
    private final int           height;
    public Processor(BufferedImage image) {
        this.image = image;
        width = image.getWidth();
        height = image.getHeight();
    }

    public BufferedImage process(Collection<? extends Triangle> triangles) {
        for (Triangle triangle : triangles) {
            averageColor(triangle);
        }
        return image;
    }

    private void averageColor(Triangle triangle) {
        AABB aabb = new AABB();
        aabb.combine(triangle.a.x, triangle.a.y);
        aabb.combine(triangle.b.x, triangle.b.y);
        aabb.combine(triangle.c.x, triangle.c.y);
        int x1 = (int) Math.floor(aabb.getMinX());
        int y1 = (int) Math.floor(aabb.getMinY());
        int x2 = (int) Math.ceil(aabb.getMaxX());
        int y2 = (int) Math.ceil(aabb.getMaxY());

        List<Integer> xList = new ArrayList<>();
        List<Integer> yList = new ArrayList<>();
        List<Color> colors = new ArrayList<>();

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                boolean b = Util.pointInTriangle(new Point(x, y), triangle);
                if (b) {
                    int rgb = image.getRGB(x, y);
                    xList.add(x);
                    yList.add(y);
                    colors.add(new Color(rgb));
                }
            }
        }

        int c = averageColor(colors);

        for (int i = 0; i < colors.size(); i++) {
            Integer x = xList.get(i);
            Integer y = yList.get(i);
            image.setRGB(x, y, c);
        }
    }

    private int averageColor(List<Color> colors) {
        double r = 0;
        double g = 0;
        double b = 0;
        for (Color color : colors) {
            r += color.r() * color.r();
            g += color.g() * color.g();
            b += color.b() * color.b();
        }
        r /= colors.size();
        g /= colors.size();
        b /= colors.size();
        r = Math.sqrt(r);
        g = Math.sqrt(g);
        b = Math.sqrt(b);
        Color c = new Color((float) r, (float) g, (float) b, 1);
        return c.toARGB8888();
    }

}
