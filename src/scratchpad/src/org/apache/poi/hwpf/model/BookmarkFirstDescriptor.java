package org.apache.poi.hwpf.model;

import org.apache.poi.hwpf.model.types.BKFAbstractType;

public final class BookmarkFirstDescriptor extends BKFAbstractType implements
        Cloneable
{
    public BookmarkFirstDescriptor()
    {
    }

    public BookmarkFirstDescriptor( byte[] data, int offset )
    {
        fillFields( data, offset );
    }

    @Override
    protected BookmarkFirstDescriptor clone()
    {
        try
        {
            return (BookmarkFirstDescriptor) super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        BookmarkFirstDescriptor other = (BookmarkFirstDescriptor) obj;
        if ( field_1_ibkl != other.field_1_ibkl )
            return false;
        if ( field_2_bkf_flags != other.field_2_bkf_flags )
            return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + field_1_ibkl;
        result = prime * result + field_2_bkf_flags;
        return result;
    }

    public boolean isEmpty()
    {
        return field_1_ibkl == 0 && field_2_bkf_flags == 0;
    }

    @Override
    public String toString()
    {
        if ( isEmpty() )
            return "[BKF] EMPTY";

        return super.toString();
    }
}
