package org.apache.poi.hssf.record;

public class DrawingSelectionRecord extends AbstractEscherHolderRecord
{
    public static final short sid = 0xED;

    public DrawingSelectionRecord()
    {
    }

    public DrawingSelectionRecord( short id, short size, byte[] data )
    {
        super( id, size, data );
    }

    public DrawingSelectionRecord( short id, short size, byte[] data, int offset )
    {
        super( id, size, data, offset );
    }

    protected String getRecordName()
    {
        return "MSODRAWINGSELECTION";
    }

    public short getSid()
    {
        return sid;
    }
}
