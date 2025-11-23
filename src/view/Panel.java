package view;

import rasterize.RasterBufferedImage;

import javax.swing.*;
import java.awt.*;

public class Panel extends JPanel {

    private final RasterBufferedImage raster;

    public Panel(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        raster = new RasterBufferedImage(width, height);

        // Nastavení pro lepší výkon
        setDoubleBuffered(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Rychlejší vykreslování
        if (raster.getImage() != null) {
            g.drawImage(raster.getImage(), 0, 0, null);
        }
    }

    public RasterBufferedImage getRaster() {
        return raster;
    }
}