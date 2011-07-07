package org.apache.poi.hwpf.usermodel;

import junit.framework.TestCase;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;

public class TestTableRow extends TestCase
{
    public void testInnerTableCellsDetection() throws Exception
    {
        HWPFDocument hwpfDocument = new HWPFDocument( POIDataSamples
                .getDocumentInstance().openResourceAsStream( "innertable.doc" ) );
        hwpfDocument.getRange();

        Range documentRange = hwpfDocument.getRange();
        Paragraph startOfInnerTable = documentRange.getParagraph( 6 );

        Table innerTable = documentRange.getTable( startOfInnerTable );
        assertEquals( 2, innerTable.numRows() );

        TableRow tableRow = innerTable.getRow( 0 );
        assertEquals( 2, tableRow.numCells() );
    }

    public void testOuterTableCellsDetection() throws Exception
    {
        HWPFDocument hwpfDocument = new HWPFDocument( POIDataSamples
                .getDocumentInstance().openResourceAsStream( "innertable.doc" ) );
        hwpfDocument.getRange();

        Range documentRange = hwpfDocument.getRange();
        Paragraph startOfOuterTable = documentRange.getParagraph( 0 );

        Table outerTable = documentRange.getTable( startOfOuterTable );
        assertEquals( 3, outerTable.numRows() );

        assertEquals( 3, outerTable.getRow( 0 ).numCells() );
        assertEquals( 3, outerTable.getRow( 1 ).numCells() );
        assertEquals( 3, outerTable.getRow( 2 ).numCells() );
    }

}
