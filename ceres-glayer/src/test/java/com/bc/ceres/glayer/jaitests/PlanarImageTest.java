package com.bc.ceres.glayer.jaitests;

import junit.framework.TestCase;

import javax.media.jai.*;
import javax.media.jai.operator.FormatDescriptor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.HashSet;

public class PlanarImageTest extends TestCase {

    public void testThatTileSchedulerFiresEventsForAlreadyAvailableTiles() {
        final RenderedOp tiledImage = createTiledAndCachedImage();

        final Point[] indices = tiledImage.getTileIndices(new Rectangle(1, 1, 511, 511));
        assertNotNull(indices);
        assertEquals(16, indices.length);

        final Raster[] expectedTiles = tiledImage.getTiles();

        final MyTileComputationListener listener = new MyTileComputationListener(16);
        tiledImage.addTileComputationListener(listener);
        tiledImage.queueTiles(indices);

        try {
            synchronized (listener) {
                listener.wait(1000);
            }
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(listener.expectedSize, listener.computedTiles.size());
        for (Raster expectedTile : expectedTiles) {
            assertTrue(listener.computedTiles.contains(expectedTile));
        }
    }

    private static RenderedOp createTiledAndCachedImage() {
        final BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        final ImageLayout imageLayout = new ImageLayout();
        imageLayout.setTileWidth(128);
        imageLayout.setTileHeight(128);
        final RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, imageLayout);
        hints.add(new RenderingHints(JAI.KEY_TILE_CACHE, JAI.getDefaultInstance().getTileCache()));
        return FormatDescriptor.create(image, image.getSampleModel().getDataType(), hints);
    }

    private static class MyTileComputationListener implements TileComputationListener {

        private final int expectedSize;
        private HashSet<Raster> computedTiles;

        private MyTileComputationListener(int expectedSize) {
            this.expectedSize = expectedSize;
            computedTiles = new HashSet<Raster>(expectedSize * 2);
        }

        public synchronized void tileComputed(Object o, TileRequest[] tileRequests, PlanarImage planarImage, int tileX, int tileY, Raster raster) {
            computedTiles.add(raster);
            if (computedTiles.size() == expectedSize) {
                notify();
            }
        }

        public void tileCancelled(Object o, TileRequest[] tileRequests, PlanarImage planarImage, int tileX, int tileY) {
        }

        public void tileComputationFailure(Object o, TileRequest[] tileRequests, PlanarImage planarImage, int tileX, int tileY, Throwable throwable) {
        }
    }
}