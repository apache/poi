package org.apache.poi.hssf.record;

import junit.framework.TestCase;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.util.HexDump;

public class TestDrawingGroupRecord extends TestCase
{
    public void testGetRecordSize()
            throws Exception
    {
        DrawingGroupRecord r = new DrawingGroupRecord();
        assertEquals(4, r.getRecordSize());

        EscherSpRecord sp = new EscherSpRecord();
        sp.setRecordId(EscherSpRecord.RECORD_ID);
        sp.setOptions((short) 0x1111);
        sp.setFlags(-1);
        sp.setShapeId(-1);
        EscherContainerRecord dggContainer = new EscherContainerRecord();
        dggContainer.setOptions((short) 0x000F);
        dggContainer.setRecordId((short) 0xF000);
        dggContainer.addChildRecord(sp);

        r.addEscherRecord(dggContainer);
        assertEquals(28, r.getRecordSize());

        byte[] data = new byte[28];
        int size = r.serialize(0, data);
        assertEquals("[EB, 00, 18, 00, 0F, 00, 00, F0, 10, 00, 00, 00, 11, 11, 0A, F0, 08, 00, 00, 00, FF, FF, FF, FF, FF, FF, FF, FF, ]", HexDump.toHex(data));
        assertEquals(28, size);

        assertEquals(24, dggContainer.getRecordSize());
    }
}
