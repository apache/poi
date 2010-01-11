package org.apache.poi.xslf.usermodel;

import org.openxmlformats.schemas.drawingml.x2006.main.CTTableRow;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCell;

public class DrawingTableRow {
    private final CTTableRow row;

    public DrawingTableRow(CTTableRow row) {
        this.row = row;
    }

    public DrawingTableCell[] getCells() {
        CTTableCell[] ctTableCells = row.getTcArray();
        DrawingTableCell[] o = new DrawingTableCell[ctTableCells.length];

        for (int i=0; i<o.length; i++) {
            o[i] = new DrawingTableCell(ctTableCells[i]);
        }

        return o;
    }
}