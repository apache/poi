package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.model.types.TLPAbstractType;

public class TableAutoformatLookSpecifier extends TLPAbstractType implements
        Cloneable
{
    public static final int SIZE = 4;

    public TableAutoformatLookSpecifier()
    {
        super();
    }

    public TableAutoformatLookSpecifier( byte[] data, int offset )
    {
        super();
        fillFields( data, offset );
    }

    @Override
    public TableAutoformatLookSpecifier clone()
    {
        try
        {
            return (TableAutoformatLookSpecifier) super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            throw new Error( e.getMessage(), e );
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
        TableAutoformatLookSpecifier other = (TableAutoformatLookSpecifier) obj;
        if ( field_1_itl != other.field_1_itl )
            return false;
        if ( field_2_tlp_flags != other.field_2_tlp_flags )
            return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + field_1_itl;
        result = prime * result + field_2_tlp_flags;
        return result;
    }

    public boolean isEmpty()
    {
        return field_1_itl == 0 && field_2_tlp_flags == 0;
    }
}
