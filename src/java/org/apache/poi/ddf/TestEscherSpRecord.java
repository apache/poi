package org.apache.poi.ddf;

import junit.framework.TestCase;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;

public class TestEscherSpRecord extends TestCase
{
    public void testSerialize() throws Exception
    {
        EscherSpRecord r = createRecord();

        byte[] data = new byte[16];
        int bytesWritten = r.serialize( 0, data, new NullEscherSerializationListener() );
        assertEquals( 16, bytesWritten );
        assertEquals( "[02, 00, " +
                "0A, F0, " +
                "08, 00, 00, 00, " +
                "00, 04, 00, 00, " +
                "05, 00, 00, 00, ]",
                HexDump.toHex( data ) );
    }

    public void testFillFields() throws Exception
    {
        String hexData = "02 00 " +
                "0A F0 " +
                "08 00 00 00 " +
                "00 04 00 00 " +
                "05 00 00 00 ";
        byte[] data = HexRead.readFromString( hexData );
        EscherSpRecord r = new EscherSpRecord();
        int bytesWritten = r.fillFields( data, new DefaultEscherRecordFactory() );

        assertEquals( 16, bytesWritten );
        assertEquals( 0x0400, r.getShapeId() );
        assertEquals( 0x05, r.getFlags() );
    }

    public void testToString() throws Exception
    {
        String nl = System.getProperty("line.separator");

        String expected = "org.apache.poi.ddf.EscherSpRecord:" + nl +
                "  RecordId: 0xF00A" + nl +
                "  Options: 0x0002" + nl +
                "  ShapeId: 1024" + nl +
                "  Flags: GROUP|PATRIARCH (0x00000005)" + nl;
        assertEquals( expected, createRecord().toString() );
    }

    private EscherSpRecord createRecord()
    {
        EscherSpRecord r = new EscherSpRecord();
        r.setOptions( (short) 0x0002 );
        r.setRecordId( EscherSpRecord.RECORD_ID );
        r.setShapeId(0x0400);
        r.setFlags(0x05);
        return r;
    }

}
