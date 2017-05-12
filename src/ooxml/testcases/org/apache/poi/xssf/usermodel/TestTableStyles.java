package org.apache.poi.xssf.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.poi.ss.usermodel.DifferentialStyleProvider;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.ss.usermodel.TableStyle;
import org.apache.poi.ss.usermodel.TableStyleInfo;
import org.apache.poi.ss.usermodel.TableStyleType;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

/**
 * Test built-in table styles
 */
public class TestTableStyles {

    /**
     * Test that a built-in style is initialized properly
     */
    @Test
    public void testBuiltinStyleInit() {
        TableStyle style = XSSFBuiltinTableStyle.TableStyleMedium2.getStyle();
        assertNotNull("no style found for Medium2", style);
        assertNull("Should not have style info for blankRow", style.getStyle(TableStyleType.blankRow));
        DifferentialStyleProvider headerRow = style.getStyle(TableStyleType.headerRow);
        assertNotNull("no header row style", headerRow);
        FontFormatting font = headerRow.getFontFormatting();
        assertNotNull("No header row font formatting", font);
        assertTrue("header row not bold", font.isBold());
        PatternFormatting fill = headerRow.getPatternFormatting();
        assertNotNull("No header fill", fill);
        assertEquals("wrong header fill", 4, ((XSSFColor) fill.getFillBackgroundColorColor()).getTheme());
    }

    @Test
    public void testCustomStyle() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("tableStyle.xlsx");
        
        Table table = wb.getTable("Table1");
        assertNotNull("missing table", table);
        
        TableStyleInfo style = table.getStyle();
        assertNotNull("Missing table style info", style);
        assertNotNull("Missing table style", style.getStyle());
        assertEquals("Wrong name", "TestTableStyle", style.getName());
        assertEquals("Wrong name", "TestTableStyle", style.getStyle().getName());

        DifferentialStyleProvider firstColumn = style.getStyle().getStyle(TableStyleType.firstColumn);
        assertNotNull("no first column style", firstColumn);
        FontFormatting font = firstColumn.getFontFormatting();
        assertNotNull("no first col font", font);
        assertTrue("wrong first col bold", font.isBold());
        
        wb.close();
    }
}
