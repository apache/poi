package org.apache.poi.ddf;

import junit.framework.TestCase;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;

public class TestEscherDgRecord extends TestCase
{
    public void testSerialize() throws Exception
    {
        EscherDgRecord r = createRecord();

        byte[] data = new byte[16];
        int bytesWritten = r.serialize( 0, data, new NullEscherSerializationListener() );
        assertEquals( 16, bytesWritten );
        assertEquals( "[10, 00, " +
                "08, F0, " +
                "08, 00, 00, 00, " +
                "02, 00, 00, 00, " +     // num shapes in drawing
                "01, 04, 00, 00, ]",     // The last MSOSPID given to an SP in this DG
                HexDump.toHex( data ) );
    }

    public void testFillFields() throws Exception
    {
        String hexData = "10 00 " +
                "08 F0 " +
                "08 00 00 00 " +
                "02 00 00 00 " +
                "01 04 00 00 ";
        byte[] data = HexRead.readFromString( hexData );
        EscherDgRecord r = new EscherDgRecord();
        int bytesWritten = r.fillFields( data, new DefaultEscherRecordFactory() );

        assertEquals( 16, bytesWritten );
        assertEquals( 2, r.getNumShapes() );
        assertEquals( 1025, r.getLastMSOSPID() );
    }

    public void testToString() throws Exception
    {
        String nl = System.getProperty("line.separator");

        String expected = "org.apache.poi.ddf.EscherDgRecord:" + nl +
                "  RecordId: 0xF008" + nl +
                "  Options: 0x0010" + nl +
                "  NumShapes: 2" + nl +
                "  LastMSOSPID: 1025" + nl;
        assertEquals( expected, createRecord().toString() );
    }

    private EscherDgRecord createRecord()
    {
        EscherDgRecord r = new EscherDgRecord();
        r.setOptions( (short) 0x0010 );
        r.setRecordId( EscherDgRecord.RECORD_ID );
        r.setNumShapes(2);
        r.setLastMSOSPID(1025);
        return r;
    }

}
