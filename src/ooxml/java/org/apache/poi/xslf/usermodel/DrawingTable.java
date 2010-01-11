package org.apache.poi.xslf.usermodel;

import org.openxmlformats.schemas.drawingml.x2006.main.CTTable;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableRow;

public class DrawingTable {
    private final CTTable table;

    public DrawingTable(CTTable table) {
        this.table = table;
    }

    public DrawingTableRow[] getRows() {
        CTTableRow[] ctTableRows = table.getTrArray();
        DrawingTableRow[] o = new DrawingTableRow[ctTableRows.length];

        for (int i=0; i<o.length; i++) {
            o[i] = new DrawingTableRow(ctTableRows[i]);
        }

        return o;
    }
}
