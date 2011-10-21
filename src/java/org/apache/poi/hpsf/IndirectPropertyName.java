package org.apache.poi.hpsf;

import org.apache.poi.util.Internal;

@Internal
class IndirectPropertyName
{
    private CodePageString _value;

    IndirectPropertyName( byte[] data, int offset )
    {
        _value = new CodePageString( data, offset );
    }

    int getSize()
    {
        return _value.getSize();
    }
}
