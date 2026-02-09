package org.apache.poi.ss.usermodel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the short-based ClientAnchor column setters introduced for Bug 69935.
 */
public class TestClientAnchorShortSetter {

    @Test
    void shortSettersDelegateToIntSetters() throws Exception {
        try (Workbook wb = new HSSFWorkbook()) {
            CreationHelper helper = wb.getCreationHelper();

            // The actual implementation returned here is HSSFClientAnchor
            ClientAnchor anchor = helper.createClientAnchor();

            // Use the new short-based overloads
            anchor.setCol1((short) 2);
            anchor.setCol2((short) 5);

            // Setting rows is not strictly required, but keeps the anchor fully initialized
            anchor.setRow1(1);
            anchor.setRow2(3);

            // getColX() still returns short; compare using int for convenience
            assertEquals(2, anchor.getCol1());
            assertEquals(5, anchor.getCol2());
        }
    }

    @Test
    void intSettersStillWorkAsBefore() throws Exception {
        try (Workbook wb = new HSSFWorkbook()) {
            CreationHelper helper = wb.getCreationHelper();

            ClientAnchor anchor = helper.createClientAnchor();

            // Verify that the existing int-based setters still behave as before
            anchor.setCol1(3);
            anchor.setCol2(7);

            assertEquals(3, anchor.getCol1());
            assertEquals(7, anchor.getCol2());
        }
    }
}
