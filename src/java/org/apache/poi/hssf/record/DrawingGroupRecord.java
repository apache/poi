package org.apache.poi.hssf.record;

public class DrawingGroupRecord extends AbstractEscherHolderRecord
{
    public static final short sid = 0xEB;

    public DrawingGroupRecord()
    {
    }

    public DrawingGroupRecord( short id, short size, byte[] data )
    {
        super( id, size, data );
    }

    public DrawingGroupRecord( short id, short size, byte[] data, int offset )
    {
        super( id, size, data, offset );
    }

    protected String getRecordName()
    {
        return "MSODRAWINGGROUP";
    }

    public short getSid()
    {
        return sid;
    }
}
