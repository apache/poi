package org.apache.poi.hssf.converter;

import org.apache.poi.hssf.util.HSSFColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestExcelUtils {
    @Test
    void testGetColor() {
        assertEquals("#800000", AbstractExcelUtils.getColor(HSSFColor.HSSFColorPredefined.DARK_RED.getColor()));
        assertEquals("white", AbstractExcelUtils.getColor(HSSFColor.HSSFColorPredefined.WHITE.getColor()));
    }
}
