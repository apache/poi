package org.apache.poi.hssf.record;

/**
 * This is purely for the biff viewer.  During normal operations we don't want
 * to be seeing this.
 */
public class DrawingRecordForBiffViewer
        extends AbstractEscherHolderRecord
{
    public static final short sid = 0xEC;

    public DrawingRecordForBiffViewer()
    {
    }

    public DrawingRecordForBiffViewer( short id, short size, byte[] data )
    {
        super( id, size, data );
    }

    public DrawingRecordForBiffViewer( short id, short size, byte[] data, int offset )
    {
        super( id, size, data, offset );
    }

    protected String getRecordName()
    {
        return "MSODRAWING";
    }

    public short getSid()
    {
        return sid;
    }
}
