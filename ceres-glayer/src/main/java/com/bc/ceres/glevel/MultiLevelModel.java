package com.bc.ceres.glevel;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * The {@code MultiLevelModel} class represents a layout model for multi-resolution images such as image pyramids.
 * <p>It comprises the number of resolution levels, the affine transformation
 * from image (pixel) to model coordinates and the bounds in model coordinates.</p>
 * <p>The resolution level is an integer number ranging from zero (the highest resolution)
 * to {@link #getLevelCount()}-1 (the lowest resolution).</p>
 *
 * @author Norman Fomferra
 * @author Marco Zuehlke
 * @version $revision$ $date$
 */
public interface MultiLevelModel {
    /**
     * Gets the number of resolution levels, which is always greater than zero.
     *
     * @return The number of resolution levels.
     */
    int getLevelCount();

    /**
     * Gets the resolution level for a given scaling factor, e.g. {@code level=log(scale)/log(2)}.
     *
     * @param scale The scaling factor, will always be a greater than or equal to 1.
     * @return The resolution level, must be in the range 0 to {@link #getLevelCount() levelCount}-1.
     * @see MultiLevelSource#getImage(int)
     */
    int getLevel(double scale);

    /**
     * Gets the scale for a given resolution level, e.g. {@code scale=pow(2,level)}.
     *
     * @param level The resolution level, must be in the range 0 to {@link #getLevelCount() levelCount}-1.
     * @return The scaling factor, must be greater than or equal to 1.
     *         {@link #getLevel(double) getLevel(scale)} shall return {@code level}.
     * @see MultiLevelSource#getImage(int)
     */
    double getScale(int level);

    /**
     * Gets a copy (non-life object) of the affine transformation from image to model coordinates for the given level.
     *
     * @param level The resolution level, must be in the range 0 to {@link #getLevelCount() levelCount}-1.
     * @return The affine transformation from image to model coordinates.
     */
    AffineTransform getImageToModelTransform(int level);

    /**
     * Gets a copy (non-life object) of the affine transformation from model to image coordinates for the given level.
     *
     * @param level The resolution level, must be in the range 0 to {@link #getLevelCount() levelCount}-1.
     * @return The affine transformation from model to image coordinates.
     */
    AffineTransform getModelToImageTransform(int level);

    /**
     * Returns the bounding box in model coordinates.
     *
     * @return The bounding box.
     */
    Rectangle2D getModelBounds();
}