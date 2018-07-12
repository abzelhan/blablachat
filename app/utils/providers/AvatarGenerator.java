package utils.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class AvatarGenerator {
    private static final Logger logger = LoggerFactory.getLogger(AvatarGenerator.class);

    private final int imageCount;
    private final int baseSize;
    private final double fillFactor;
    private final float alphaFactor;
    private final int pixelSize;
    private final int borderSize;
    private final int imageSize;
    private final int pixelCount;

    Random r = new Random();
    Color backgroundColor = new Color(240, 240, 240);

    public AvatarGenerator(int imageCount, int baseSize, double fillFactor, float alphaFactor,
                           int pixelSize, int borderSize, int imageSize) {
        this.imageCount = imageCount;
        this.baseSize = baseSize;
        this.fillFactor = fillFactor;
        this.alphaFactor = alphaFactor;
        this.pixelSize = pixelSize;
        this.borderSize = borderSize;
        this.imageSize = imageSize;
        this.pixelCount = this.baseSize / this.pixelSize;
    }



    public Set<BufferedImage> createMany() {
        logInitialization();
        Set<BufferedImage> set = new HashSet<>();
        for (int i = 0; i < imageCount; i++) {
            set.add(createOne());
        }
        return set;
    }

    public BufferedImage createOne() {
        final BufferedImage bf = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        Color fore = new Color(r.nextFloat(), r.nextFloat(), r.nextFloat(), alphaFactor);
        Graphics2D g = bf.createGraphics();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, imageSize, imageSize);
        g.setColor(fore);
        for (int x = 0; x < pixelCount/2 + pixelCount%2; x++) {
            for (int y = 0; y < pixelCount; y++) {
                tryToDrawPixel(g, x, y);
            }
        }
        return bf;
    }

    private void tryToDrawPixel(Graphics2D g, int x, int y) {
        int iX = x*pixelSize;
        int iY = y*pixelSize;
        if (r.nextFloat() < fillFactor) {
            g.fillRect(borderSize + iX, borderSize + iY, pixelSize, pixelSize);
            if (x<pixelCount/2) {
                g.fillRect(borderSize + baseSize - pixelSize - iX, borderSize + iY, pixelSize, pixelSize);
            }
        }
    }

    private void logInitialization() {
        logger.info(String.format("About to create [%s] images with size [%sx%s] and pixel size [%sx%s]",
                imageCount, imageSize, imageSize, pixelSize, pixelSize));
    }
}
