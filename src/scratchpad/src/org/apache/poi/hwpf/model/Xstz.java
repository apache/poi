package org.apache.poi.hwpf.model;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

@Internal
public class Xstz
{
    private static final POILogger log = POILogFactory.getLogger( Xstz.class );

    private final short _chTerm = 0;
    private Xst _xst;

    public Xstz()
    {
        _xst = new Xst();
    }

    public Xstz( byte[] data, int startOffset )
    {
        fillFields( data, startOffset );
    }

    public void fillFields( byte[] data, int startOffset )
    {
        int offset = startOffset;

        _xst = new Xst( data, offset );
        offset += _xst.getSize();

        short term = LittleEndian.getShort( data, offset );
        if ( term != 0 )
        {
            log.log( POILogger.WARN, "chTerm at the end of Xstz at offset ",
                    offset, " is not 0" );
        }
    }

    public String getAsJavaString()
    {
        return _xst.getAsJavaString();
    }

    public int getSize()
    {
        return _xst.getSize() + LittleEndian.SHORT_SIZE;
    }

    public int serialize( byte[] data, int startOffset )
    {
        int offset = startOffset;

        _xst.serialize( data, offset );
        offset += _xst.getSize();

        LittleEndian.putUShort( data, offset, _chTerm );
        offset += LittleEndian.SHORT_SIZE;

        return offset - startOffset;
    }

    @Override
    public String toString()
    {
        return "[Xstz]" + _xst.getAsJavaString() + "[/Xstz]";
    }
}
