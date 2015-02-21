package org.apache.poi.sl.draw;

import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.Shape;


public class DrawMasterSheet extends DrawSheet {

    public DrawMasterSheet(MasterSheet sheet) {
        super(sheet);
    }

    /**
     * Checks if this <code>sheet</code> displays the specified shape.
     *
     * Subclasses can override it and skip certain shapes from drawings,
     * for instance, slide masters and layouts don't display placeholders
     */
    protected boolean canDraw(Shape shape){
        return !shape.isPlaceholder();
    }
}
