package org.apache.poi.hssf.view.brush;

import java.awt.*;

/**
 * This is the type you must implement to create a brush that will be used for a
 * spreadsheet border.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public interface Brush extends Stroke {
    /** Returns the width of the brush. */
    float getLineWidth();
}