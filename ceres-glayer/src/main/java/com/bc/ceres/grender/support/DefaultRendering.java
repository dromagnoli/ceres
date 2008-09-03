package com.bc.ceres.grender.support;

import com.bc.ceres.core.Assert;
import com.bc.ceres.grender.Rendering;
import com.bc.ceres.grender.Viewport;

import java.awt.*;

/**
 * A default implementation of the {@link com.bc.ceres.grender.Rendering} interface.
 */
public class DefaultRendering implements Rendering {
    private Graphics2D graphics;
    private Viewport viewport;
    private Rectangle bounds;

    public DefaultRendering(Graphics2D graphics, Viewport viewport) {
        this(graphics, viewport, viewport.getBounds());
    }

    public DefaultRendering(Graphics2D graphics, Viewport viewport, Rectangle bounds) {
        setBounds(bounds);
        setViewport(viewport);
        setGraphics(graphics);
    }

    public Graphics2D getGraphics() {
        return graphics;
    }

    public void setGraphics(Graphics2D graphics) {
        Assert.notNull(graphics, "graphics");
        this.graphics = graphics;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void setViewport(Viewport viewport) {
        Assert.notNull(viewport, "viewport");
        this.viewport = viewport;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        Assert.notNull(bounds, "bounds");
        this.bounds = bounds;
    }
}