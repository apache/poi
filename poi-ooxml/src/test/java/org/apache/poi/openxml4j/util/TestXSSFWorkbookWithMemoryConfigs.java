package org.apache.poi.openxml4j.util;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.apache.poi.xssf.XSSFTestDataSamples.openSampleWorkbook;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Isolated //changes IOUtils and other static changes
class TestXSSFWorkbookWithMemoryConfigs {
    @Test
    void loadXslsxWithLowThreshold() {
        final int defaultMaxEntrySize = ZipArchiveFakeEntry.getMaxEntrySize();
        ZipArchiveFakeEntry.setMaxEntrySize(100);
        try {
            assertThrows(RecordFormatException.class, () -> openSampleWorkbook("Formatting.xlsx"));
            IOUtils.setByteArrayMaxOverride(Integer.MAX_VALUE);
            assertDoesNotThrow(() -> {
                try(XSSFWorkbook wb = openSampleWorkbook("Formatting.xlsx")) {
                    assertNotNull(wb.getSheetAt(0));
                }
            });
        } finally {
            ZipArchiveFakeEntry.setMaxEntrySize(defaultMaxEntrySize);
            IOUtils.setByteArrayMaxOverride(-1);
        }
    }
}
