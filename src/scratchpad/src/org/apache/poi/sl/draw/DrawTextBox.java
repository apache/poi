package org.apache.poi.sl.draw;

import org.apache.poi.sl.usermodel.*;

public class DrawTextBox<T extends TextBox> extends DrawAutoShape<T> {
    public DrawTextBox(T shape) {
        super(shape);
    }
}
