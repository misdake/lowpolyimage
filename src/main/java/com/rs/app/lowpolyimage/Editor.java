package com.rs.app.lowpolyimage;

import com.rs.math.geometry.func.Triangulation;
import com.rs.math.geometry.shape.Point;
import com.rs.math.geometry.shape.Triangle;
import com.rs.tool.canvas2d.Camera;
import com.rs.tool.canvas2d.Canvas2d_swing;
import com.rs.tool.canvas2d.MouseListener;
import com.rs.tool.canvas2d.Renderer2d;
import com.rs.tool.canvas2d.model.Drawable;
import com.rs.tool.canvas2d.model.Image;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Editor extends JFrame {

    public static void main(String[] args) {
        Editor editor = new Editor(args[0]);
        editor.setVisible(true);
        editor.pack();
    }

    private final String         file;
    private final Image          image;
    private final Canvas2d_swing canvas;
    private final JComponent     panel;
    private final Camera         camera;

    public Editor(String file) {
        this.file = file;
        image = new Image(new File(file));

        canvas = new Canvas2d_swing(0xffffffff, true);
        panel = canvas.getComponent();
        add(panel);

        camera = new Camera(canvas);
        camera.addCameraControl(MouseListener.Button.MIDDLE);
        camera.centerOnCanvas(new com.rs.tool.canvas2d.model.Point(image.width / 2.0, image.height / 2.0));
        camera.zoom(0.5);
        canvas.setCamera(camera);

        initListener();

        canvas.getLayer(0).add(image.leftTop(0, 0));
        canvas.getLayer(1).add(renderer -> {
            for (ControlPoint point : points) {
                point.draw(renderer);
            }
            for (ControlTriangle triangle : triangles) {
                triangle.draw(renderer);
            }
        });

        panel.setPreferredSize(new Dimension(image.width / 2, image.height / 2));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void initListener() {
        //left to add, right to remove
        canvas.addMouseListener(MouseListener.Button.LEFT, new MouseListener() {
            @Override
            public void click(Event e, double x, double y) {
                addPoint(Math.round((float) x), Math.round((float) y));
            }
        });
        canvas.addMouseListener(MouseListener.Button.RIGHT, new MouseListener() {
            @Override
            public void click(Event e, double x, double y) {
                boolean b = removePoint(Math.round((float) x), Math.round((float) y));
                if (!b) removeTriangle(Math.round((float) x), Math.round((float) y));
            }
        });

        canvas.addKeyboardListener((status, key) -> {
            if (key == KeyEvent.VK_SPACE) {
                triangulation();
            }
            if (key == KeyEvent.VK_ENTER) {
                Processor processor = new Processor(image.image);
                BufferedImage outImage = processor.process(triangles);
                int i = file.lastIndexOf(".");
                String extension = file.substring(i + 1);
                String outFile = file.substring(0, i) + "_out." + extension;
                try {
                    ImageIO.write(outImage, extension, new File(outFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Set<ControlPoint>    points    = new HashSet<>();
    private Set<ControlTriangle> triangles = new HashSet<>();

    private void addPoint(int x, int y) {
        if (findPoint(x, y) == null) {
            points.add(new ControlPoint(x, y));
            canvas.refresh();
        }
    }
    private boolean removePoint(int x, int y) {
        ControlPoint p = findPoint(x, y);
        if (p == null) return false;

        points.remove(p);
        canvas.refresh();
        return true;
    }
    private boolean removeTriangle(int x, int y) {
        ControlTriangle p = findTriangle(x, y);
        if (p == null) return false;

        triangles.remove(p);
        canvas.refresh();
        return true;
    }
    private ControlPoint findPoint(int x, int y) {
        com.rs.tool.canvas2d.model.Point click = camera.canvasCoordToWindow(new com.rs.tool.canvas2d.model.Point(x, y));

        double min = Double.MAX_VALUE;
        ControlPoint p = null;
        for (ControlPoint point : points) {
            com.rs.tool.canvas2d.model.Point coord = canvas.canvasCoordToWindow(new com.rs.tool.canvas2d.model.Point(point.x, point.y));
            double d = Math.hypot(click.x - coord.x, click.y - coord.y);
            if (d < 10 && d < min) {
                min = d;
                p = point;
            }
        }
        return p;
    }
    private ControlTriangle findTriangle(int x, int y) {
        Point point = new Point(x, y);
        for (ControlTriangle triangle : triangles) {
            if (Util.pointInTriangle(point, triangle)) {
                return triangle;
            }
        }
        return null;
    }

    private static class ControlPoint extends Point implements Drawable {
        public ControlPoint(int x, int y) {
            super(x, y);
        }
        @Override
        public void draw(Renderer2d renderer) {
            int size = (int) Math.ceil(renderer.getRatio() * 2);
            if (size < 3) size = 3;
            renderer.color(0xffffffff);
            renderer.pointSize(size + 2);
            renderer.point(x + 0.5, y + 0.5);
            renderer.color(0xff000000);
            renderer.pointSize(size);
            renderer.point(x + 0.5, y + 0.5);
        }
    }

    private static class ControlTriangle extends Triangle implements Drawable {
        private double[] x = new double[3];
        private double[] y = new double[3];
        private ControlTriangle(Triangle t) {
            super(t.a, t.b, t.c);
            x[0] = t.a.x;
            x[1] = t.b.x;
            x[2] = t.c.x;
            y[0] = t.a.y;
            y[1] = t.b.y;
            y[2] = t.c.y;
        }
        @Override
        public void draw(Renderer2d renderer) {
            renderer.color(0xffff7f00);
            renderer.lineWidth(3);
            renderer.line(x[0], y[0], x[1], y[1]);
            renderer.line(x[1], y[1], x[2], y[2]);
            renderer.line(x[2], y[2], x[0], y[0]);
            renderer.color(0x40ff7f00);
            renderer.fillPolygon(x, y);
        }
    }

    private Set<Triangle> triangulation() {
        List<Point> pointList = new ArrayList<>(points);
        Triangulation.CdtResult cdt = Triangulation.cdt(pointList, new ArrayList<>());
        triangles.clear();
        for (Triangle triangle : cdt.triangles) {
            triangles.add(new ControlTriangle(triangle));
        }
        canvas.refresh();
        return cdt.triangles;
    }

}
