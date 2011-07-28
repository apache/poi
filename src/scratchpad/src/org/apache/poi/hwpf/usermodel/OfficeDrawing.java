package org.apache.poi.hwpf.usermodel;

public interface OfficeDrawing
{
    /**
     * Shape Identifier
     */
    int getShapeId();

    /**
     * Left of rectangle enclosing shape relative to the origin of the shape
     */
    int getRectangleLeft();

    /**
     * Top of rectangle enclosing shape relative to the origin of the shape
     */
    int getRectangleTop();

    /**
     * Right of rectangle enclosing shape relative to the origin of the shape
     */
    int getRectangleRight();

    /**
     * Bottom of the rectangle enclosing shape relative to the origin of the
     * shape
     */
    int getRectangleBottom();

}
