package org.apache.poi.sl.draw;

import org.apache.poi.sl.usermodel.*;


public class DrawMasterSheet<T extends MasterSheet<? extends Shape, ? extends SlideShow>> extends DrawSheet<T> {

    public DrawMasterSheet(T sheet) {
        super(sheet);
    }

    /**
     * Checks if this <code>sheet</code> displays the specified shape.
     *
     * Subclasses can override it and skip certain shapes from drawings,
     * for instance, slide masters and layouts don't display placeholders
     */
    protected boolean canDraw(Shape shape){
        return !(shape instanceof SimpleShape) || !((SimpleShape)shape).isPlaceholder();
    }
}
