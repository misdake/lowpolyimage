package com.rs.app.lowpolyimage;

public class Color {
    public static float[] rgb888To32f(int r, int g, int b) {
        return new float[]{r / 255.0f, g / 255.0f, b / 255.0f, 1};
    }
    public static float[] rgba8888To32f(int r, int g, int b, int a) {
        return new float[]{r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f};
    }
    public static float[] rgb32uTo32f(int c) {
        float[] r = rgb888To32f((c >> 16) & 0xFF, (c >> 8) & 0xFF, (c >> 0) & 0xFF);
        int a = (c >> 24) & 0xFF;
        r[3] = a / 255.0f;
        return r;
    }

    public float[] color;

    public Color() {
        color = new float[]{1, 1, 1, 1};
    }
    public Color(float[] c) {
        color = new float[]{c[0], c[1], c[2], c[3]};
    }
    public Color(int c) {
        color = rgb32uTo32f(c);
    }
    public Color(float r, float g, float b, float a) {
        color = new float[]{r, g, b, a};
    }

    public Color(int r, int g, int b) {
        color = new float[]{r / 255f, g / 255f, b / 255f, 1f};
    }

    public int toARGB8888() {
        int r = (int) (color[0] * 255);
        int g = (int) (color[1] * 255);
        int b = (int) (color[2] * 255);
        int a = (int) (color[3] * 255);
        return a << 24 | r << 16 | g << 8 | b;
    }
    public int[] toIntArray() {
        return new int[]{(int) (color[0] * 255), (int) (color[1] * 255), (int) (color[2] * 255), (int) (color[3] * 255)};
    }

    public float[] toFloatArray() {
        return color;
    }

    public float r() { return color[0]; }
    public float g() { return color[1]; }
    public float b() { return color[2]; }
    public float a() { return color[3]; }

    public int ri() { return (int) (color[0] * 255); }
    public int gi() { return (int) (color[1] * 255); }
    public int bi() { return (int) (color[2] * 255); }
    public int ai() { return (int) (color[3] * 255); }
}
