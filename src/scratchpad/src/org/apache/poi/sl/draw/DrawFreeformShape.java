package org.apache.poi.sl.draw;

import org.apache.poi.sl.usermodel.*;

public class DrawFreeformShape<T extends FreeformShape> extends DrawAutoShape<T> {
    public DrawFreeformShape(T shape) {
        super(shape);
    }
}
