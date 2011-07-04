package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.model.types.TLPAbstractType;

public class TableAutoformatLookSpecifier extends TLPAbstractType
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
            throws CloneNotSupportedException
    {
        return (TableAutoformatLookSpecifier) super.clone();
    }
}
