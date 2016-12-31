package org.apache.poi.dev;

import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestOOXMLPrettyPrint {
    @Test
    public void testMain() throws Exception {
        File file = XSSFTestDataSamples.getSampleFile("Formatting.xlsx");
        File outFile = TempFile.createTempFile("Formatting", "-pretty.xlsx");

        assertTrue(outFile.delete());
        assertFalse(outFile.exists());

        OOXMLPrettyPrint.main(new String[] {
            file.getAbsolutePath(), outFile.getAbsolutePath()
        });

        assertTrue(outFile.exists());
        assertTrue(outFile.delete());
    }
}