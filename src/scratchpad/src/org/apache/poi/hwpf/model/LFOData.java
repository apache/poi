package org.apache.poi.hwpf.model;

import java.io.IOException;

import org.apache.poi.hwpf.model.io.HWPFOutputStream;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * The LFOData structure contains the Main Document CP of the corresponding LFO,
 * as well as an array of LVL override data.
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
@Internal
class LFOData
{
    private int _cp;

    private ListFormatOverrideLevel[] _rgLfoLvl;

    LFOData( byte[] buf, int startOffset, int cLfolvl )
    {
        int offset = startOffset;

        _cp = LittleEndian.getInt( buf, offset );
        offset += LittleEndian.INT_SIZE;

        _rgLfoLvl = new ListFormatOverrideLevel[cLfolvl];
        for ( int x = 0; x < cLfolvl; x++ )
        {
            _rgLfoLvl[x] = new ListFormatOverrideLevel( buf, offset );
            offset += _rgLfoLvl[x].getSizeInBytes();
        }
    }

    int getCp()
    {
        return _cp;
    }

    ListFormatOverrideLevel[] getRgLfoLvl()
    {
        return _rgLfoLvl;
    }

    int getSizeInBytes()
    {
        int result = 0;
        result += LittleEndian.INT_SIZE;

        for ( ListFormatOverrideLevel lfolvl : _rgLfoLvl )
            result += lfolvl.getSizeInBytes();

        return result;
    }

    void writeTo( HWPFOutputStream tableStream ) throws IOException
    {
        LittleEndian.putInt( _cp, tableStream );
        for ( ListFormatOverrideLevel lfolvl : _rgLfoLvl )
        {
            tableStream.write( lfolvl.toByteArray() );
        }
    }

}
