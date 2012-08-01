package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;
import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.EscherAggregate;

/**
 * @author Evgeniy Berlog
 * @date 01.08.12
 */
public class TestPatriarch extends TestCase {

    public void testGetPatriarch(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        assertNull(sh.getDrawingPatriarch());

        HSSFPatriarch patriarch = sh.createDrawingPatriarch();
        assertNotNull(patriarch);
        patriarch.createSimpleShape(new HSSFClientAnchor());
        patriarch.createSimpleShape(new HSSFClientAnchor());

        assertSame(patriarch, sh.getDrawingPatriarch());

        EscherAggregate agg = patriarch._getBoundAggregate();

        EscherDgRecord dg = agg.getEscherContainer().getChildById(EscherDgRecord.RECORD_ID);
        int lastId = dg.getLastMSOSPID();
        
        assertSame(patriarch, sh.createDrawingPatriarch());
        
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.createDrawingPatriarch();
        dg = patriarch._getBoundAggregate().getEscherContainer().getChildById(EscherDgRecord.RECORD_ID);

        assertEquals(lastId, dg.getLastMSOSPID());
    }
}
