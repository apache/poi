package org.apache.poi.ddf;

import junit.framework.TestCase;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;

public class TestEscherChildAnchorRecord extends TestCase
{
    public void testSerialize() throws Exception
    {
        EscherChildAnchorRecord r = createRecord();

        byte[] data = new byte[8 + 16];
        int bytesWritten = r.serialize( 0, data, new NullEscherSerializationListener() );
        assertEquals( 24, bytesWritten );
        assertEquals( "[01, 00, " +
                "0F, F0, " +
                "10, 00, 00, 00, " +
                "01, 00, 00, 00, " +
                "02, 00, 00, 00, " +
                "03, 00, 00, 00, " +
                "04, 00, 00, 00, ]", HexDump.toHex( data ) );
    }

    public void testFillFields() throws Exception
    {
        String hexData = "01 00 " +
                "0F F0 " +
                "10 00 00 00 " +
                "01 00 00 00 " +
                "02 00 00 00 " +
                "03 00 00 00 " +
                "04 00 00 00 ";

        byte[] data = HexRead.readFromString( hexData );
        EscherChildAnchorRecord r = new EscherChildAnchorRecord();
        int bytesWritten = r.fillFields( data, new DefaultEscherRecordFactory() );

        assertEquals( 24, bytesWritten );
        assertEquals( 1, r.getDx1() );
        assertEquals( 2, r.getDy1() );
        assertEquals( 3, r.getDx2() );
        assertEquals( 4, r.getDy2() );
        assertEquals( (short) 0x0001, r.getOptions() );
    }

    public void testToString() throws Exception
    {
        String nl = System.getProperty( "line.separator" );

        String expected = "org.apache.poi.ddf.EscherChildAnchorRecord:" + nl +
                "  RecordId: 0xF00F" + nl +
                "  Options: 0x0001" + nl +
                "  X1: 1" + nl +
                "  Y1: 2" + nl +
                "  X2: 3" + nl +
                "  Y2: 4" + nl;
        assertEquals( expected, createRecord().toString() );
    }

    private EscherChildAnchorRecord createRecord()
    {
        EscherChildAnchorRecord r = new EscherChildAnchorRecord();
        r.setRecordId( EscherChildAnchorRecord.RECORD_ID );
        r.setOptions( (short) 0x0001 );
        r.setDx1( 1 );
        r.setDy1( 2 );
        r.setDx2( 3 );
        r.setDy2( 4 );
        return r;
    }

}
