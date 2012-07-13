package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;
import org.apache.poi.hssf.HSSFTestDataSamples;

import java.io.IOException;
import java.util.List;

/**
 * @author Evgeniy Berlog
 * @date 13.07.12
 */
public class TestEmbeddedObjects extends TestCase{
    
    public void testReadExistingObject() throws IOException {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls");
        List<HSSFObjectData> list = wb.getAllEmbeddedObjects();
        assertEquals(list.size(), 1);
        HSSFObjectData obj = list.get(0);
        assertNotNull(obj.getObjectData());
        assertNotNull(obj.getDirectory());
        assertNotNull(obj.getOLE2ClassName());
    }
}
