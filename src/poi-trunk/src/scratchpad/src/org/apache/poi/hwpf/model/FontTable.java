/* ====================================================================
     Licensed to the Apache Software Foundation (ASF) under one or more
     contributor license agreements.    See the NOTICE file distributed with
     this work for additional information regarding copyright ownership.
     The ASF licenses this file to You under the Apache License, Version 2.0
     (the "License"); you may not use this file except in compliance with
     the License.    You may obtain a copy of the License at

             http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
==================================================================== */

package org.apache.poi.hwpf.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.hwpf.model.io.HWPFFileSystem;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * FontTable or in MS terminology sttbfffn is a common data structure written in all
 * Word files. The sttbfffn is an sttbf where each string is an FFN structure instead
 * of pascal-style strings. An sttbf is a string Table stored in file. Thus sttbffn
 * is like an Sttbf with an array of FFN structures that stores the font name strings
 *
 * @author Praveen Mathew
 */
@Internal
public final class FontTable
{
    private final static POILogger _logger = POILogFactory.getLogger(FontTable.class);
    private short _stringCount;// how many strings are included in the string table
    private short _extraDataSz;// size in bytes of the extra data

    // added extra facilitator members
    private int lcbSttbfffn;// count of bytes in sttbfffn
    private int fcSttbfffn;// table stream offset for sttbfffn

    // FFN structure containing strings of font names
    private Ffn[] _fontNames;


    public FontTable(byte[] buf, int offset, int lcbSttbfffn)
    {
        this.lcbSttbfffn = lcbSttbfffn;
        this.fcSttbfffn = offset;

        _stringCount = LittleEndian.getShort(buf, offset);
        offset += LittleEndian.SHORT_SIZE;
        _extraDataSz = LittleEndian.getShort(buf, offset);
        offset += LittleEndian.SHORT_SIZE;

        _fontNames = new Ffn[_stringCount]; //Ffn corresponds to a Pascal style String in STTBF.

        for(int i = 0;i<_stringCount; i++)
        {
            _fontNames[i] = new Ffn(buf,offset);
            offset += _fontNames[i].getSize();
        }
    }

    public short getStringCount()
    {
        return _stringCount;
    }

    public short getExtraDataSz()
    {
        return _extraDataSz;
    }

    public Ffn[] getFontNames()
    {
        return _fontNames;
    }

    public int getSize()
    {
        return lcbSttbfffn;
    }

    public String getMainFont(int chpFtc )
    {
        if(chpFtc >= _stringCount)
        {
            _logger.log(POILogger.INFO, "Mismatch in chpFtc with stringCount");
            return null;
        }

        return _fontNames[chpFtc].getMainFontName();
    }

    public String getAltFont(int chpFtc )
    {
        if(chpFtc >= _stringCount)
        {
            _logger.log(POILogger.INFO, "Mismatch in chpFtc with stringCount");
            return null;
        }

        return _fontNames[chpFtc].getAltFontName();
    }

    public void setStringCount(short stringCount)
    {
        this._stringCount = stringCount;
    }

    @Deprecated
    public void writeTo( HWPFFileSystem sys ) throws IOException
    {
        ByteArrayOutputStream tableStream = sys.getStream( "1Table" );
        writeTo( tableStream );
    }

    public void writeTo( ByteArrayOutputStream tableStream ) throws IOException
    {
        byte[] buf = new byte[LittleEndian.SHORT_SIZE];
        LittleEndian.putShort(buf, 0, _stringCount);
        tableStream.write(buf);
        LittleEndian.putShort(buf, 0, _extraDataSz);
        tableStream.write(buf);

        for(int i = 0; i < _fontNames.length; i++)
        {
            tableStream.write(_fontNames[i].toByteArray());
        }

    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof FontTable)) return false;
        FontTable o = (FontTable)other;

        if (o._stringCount != this._stringCount
                || o._extraDataSz != this._extraDataSz
                || o._fontNames.length != this._fontNames.length
        ) return false;
        
        for (int i=0; i<o._fontNames.length; i++) {
            if (!o._fontNames[i].equals(this._fontNames[i])) return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }

}
