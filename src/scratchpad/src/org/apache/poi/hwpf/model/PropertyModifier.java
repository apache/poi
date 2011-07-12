package org.apache.poi.hwpf.model;

import org.apache.poi.util.BitField;

public final class PropertyModifier implements Cloneable
{
    /**
     * <li>"Set to 0 for variant 1" <li>"Set to 1 for variant 2"
     */
    private static BitField _fComplex = new BitField( 0x0001 );

    /**
     * "Index to a grpprl stored in CLX portion of file"
     */
    private static BitField _figrpprl = new BitField( 0xfffe );

    /**
     * "Index to entry into rgsprmPrm"
     */
    private static BitField _fisprm = new BitField( 0x00fe );

    /**
     * "sprm's operand"
     */
    private static BitField _fval = new BitField( 0xff00 );

    private short value;

    public PropertyModifier( short value )
    {
        this.value = value;
    }

    @Override
    protected PropertyModifier clone() throws CloneNotSupportedException
    {
        return new PropertyModifier( value );
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
        PropertyModifier other = (PropertyModifier) obj;
        if ( value != other.value )
            return false;
        return true;
    }

    /**
     * "Index to a grpprl stored in CLX portion of file"
     */
    public short getIgrpprl()
    {
        if ( !isComplex() )
            throw new IllegalStateException( "Not complex" );

        return _figrpprl.getShortValue( value );
    }

    public short getIsprm()
    {
        if ( isComplex() )
            throw new IllegalStateException( "Not simple" );

        return _fisprm.getShortValue( value );
    }

    public short getVal()
    {
        if ( isComplex() )
            throw new IllegalStateException( "Not simple" );

        return _fval.getShortValue( value );
    }

    public short getValue()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + value;
        return result;
    }

    public boolean isComplex()
    {
        return _fComplex.isSet( value );
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "[PRM] (complex: " );
        stringBuilder.append( isComplex() );
        stringBuilder.append( "; " );
        if ( isComplex() )
        {
            stringBuilder.append( "igrpprl: " );
            stringBuilder.append( getIgrpprl() );
            stringBuilder.append( "; " );
        }
        else
        {
            stringBuilder.append( "isprm: " );
            stringBuilder.append( getIsprm() );
            stringBuilder.append( "; " );
            stringBuilder.append( "val: " );
            stringBuilder.append( getVal() );
            stringBuilder.append( "; " );
        }
        stringBuilder.append( ")" );
        return stringBuilder.toString();
    }
}
