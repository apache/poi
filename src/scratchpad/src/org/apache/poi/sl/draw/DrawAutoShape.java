package org.apache.poi.sl.draw;

import org.apache.poi.sl.usermodel.*;


public class DrawAutoShape<T extends AutoShape<? extends TextParagraph<? extends TextRun>>> extends DrawTextShape<T> {
    public DrawAutoShape(T shape) {
        super(shape);
    }
}
