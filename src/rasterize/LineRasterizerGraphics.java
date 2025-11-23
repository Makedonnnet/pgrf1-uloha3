package rasterize;

import java.awt.*;

public class LineRasterizerGraphics extends LineRasterizer {

    public LineRasterizerGraphics(RasterBufferedImage raster) {
        super(raster);
    }

    @Override
    public void rasterize(int x1, int y1, int x2, int y2) {
        Graphics g = raster.getImage().getGraphics();
        g.setColor(color);
        g.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void rasterize(int x1, int y1, int x2, int y2, int color1, int color2) {
        // Jednoduchá implementace gradientu pro Graphics
        Graphics2D g2d = (Graphics2D) raster.getImage().getGraphics();
        GradientPaint gradient = new GradientPaint(
                x1, y1, new Color(color1),
                x2, y2, new Color(color2)
        );
        g2d.setPaint(gradient);
        g2d.drawLine(x1, y1, x2, y2);
    }
}