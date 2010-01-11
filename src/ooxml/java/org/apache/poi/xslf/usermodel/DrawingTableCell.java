package org.apache.poi.xslf.usermodel;

import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCell;

public class DrawingTableCell {
    private final CTTableCell cell;
    private final DrawingTextBody drawingTextBody;

    public DrawingTableCell(CTTableCell cell) {
        this.cell = cell;
        drawingTextBody = new DrawingTextBody(this.cell.getTxBody());
    }

    public DrawingTextBody getTextBody() {
        return drawingTextBody;
    }
}
