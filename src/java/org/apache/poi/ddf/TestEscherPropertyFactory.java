package org.apache.poi.ddf;

import junit.framework.TestCase;

import java.util.List;

import org.apache.poi.util.HexRead;
import org.apache.poi.util.HexDump;

/**
 * @author Glen Stampoultzis  (glens @ superlinksoftware.com)
 */
public class TestEscherPropertyFactory extends TestCase
{
    public void testCreateProperties() throws Exception
    {
        String dataStr = "41 C1 " +     // propid, complex ind
                "03 00 00 00 " +         // size of complex property
                "01 00 " +              // propid, complex ind
                "00 00 00 00 " +         // value
                "41 C1 " +              // propid, complex ind
                "03 00 00 00 " +         // size of complex property
                "01 02 03 " +
                "01 02 03 "
                ;
        byte[] data = HexRead.readFromString( dataStr );
        EscherPropertyFactory f = new EscherPropertyFactory();
        List props = f.createProperties( data, 0, (short)3 );
        EscherComplexProperty p1 = (EscherComplexProperty) props.get( 0 );
        assertEquals( (short)0xC141, p1.getId() );
        assertEquals( "[01, 02, 03, ]", HexDump.toHex( p1.getComplexData() ) );

        EscherComplexProperty p3 = (EscherComplexProperty) props.get( 2 );
        assertEquals( (short)0xC141, p3.getId() );
        assertEquals( "[01, 02, 03, ]", HexDump.toHex( p3.getComplexData() ) );


    }



}
