package rasterize;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RasterBufferedImage implements Raster {

    private BufferedImage image;

    public RasterBufferedImage(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Hned při vytvoření vyplníme pozadí černou barvou
        clear();
    }

    @Override
    public void setPixel(int x, int y, int color) {
        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            image.setRGB(x, y, color);
        }
    }

    @Override
    public int getPixel(int x, int y) {
        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            return image.getRGB(x, y);
        }
        return 0; // Mimo hranice vracíme 0 (což je černá)
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    /**
     * Vymaže celý rastr na definovanou barvu pozadí (černou).
     * Nahrazuje g.clearRect(), aby byla zaručena barva pozadí 0x000000.
     */
    @Override
    public void clear() {
        Graphics g = image.getGraphics();
        g.setColor(Color.BLACK); // Explicitně nastavit barvu na černou
        g.fillRect(0, 0, image.getWidth(), image.getHeight()); // Vyplnit celý rastr
        g.dispose(); // Uvolnit prostředky
    }

    public BufferedImage getImage() {
        return image;
    }
}