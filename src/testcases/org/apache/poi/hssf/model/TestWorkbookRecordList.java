package org.apache.poi.hssf.model;

import org.apache.poi.hssf.record.chart.ChartRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.hssf.HSSFTestDataSamples.openSampleWorkbook;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestWorkbookRecordList {

    @Test
    public void tabposIsOnlyUpdatedIfWorkbookHasTabIdRecord() throws IOException {
        try (HSSFWorkbook wb = openSampleWorkbook("55982.xls")) {
            WorkbookRecordList records = wb.getInternalWorkbook().getWorkbookRecordList();
            assertEquals(-1, records.getTabpos());

            // Add an arbitrary record to the front of the list
            records.add(0, new ChartRecord());

            assertEquals(-1, records.getTabpos());
        }
    }
}
