package rasterize;

import java.awt.*;

public abstract class LineRasterizer {
    protected RasterBufferedImage raster;
    protected Color color;

    public LineRasterizer(RasterBufferedImage raster) {
        this.raster = raster;
        this.color = Color.WHITE;
    }

    public LineRasterizer(RasterBufferedImage raster, Color color) {
        this.raster = raster;
        this.color = color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public abstract void rasterize(int x1, int y1, int x2, int y2);

    // Добавить метод для градиента
    public abstract void rasterize(int x1, int y1, int x2, int y2, int color1, int color2);
}