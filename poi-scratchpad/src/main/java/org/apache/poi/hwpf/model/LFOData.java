/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.hwpf.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * The LFOData structure contains the Main Document CP of the corresponding LFO,
 * as well as an array of LVL override data.
 */
@Internal
public class LFOData
{
    private int _cp;

    private ListFormatOverrideLevel[] _rgLfoLvl;

    public LFOData()
    {
        _cp = 0;
        _rgLfoLvl = new ListFormatOverrideLevel[0];
    }

    LFOData( byte[] buf, int startOffset, int cLfolvl ) {
        if (cLfolvl < 0) {
            throw new IllegalArgumentException("Cannot create LFOData with negative count");
        }

        int offset = startOffset;

        _cp = LittleEndian.getInt( buf, offset );
        offset += LittleEndianConsts.INT_SIZE;

        _rgLfoLvl = new ListFormatOverrideLevel[cLfolvl];
        for ( int x = 0; x < cLfolvl; x++ )
        {
            _rgLfoLvl[x] = new ListFormatOverrideLevel( buf, offset );
            offset += _rgLfoLvl[x].getSizeInBytes();
        }
    }

    public int getCp()
    {
        return _cp;
    }

    public ListFormatOverrideLevel[] getRgLfoLvl()
    {
        return _rgLfoLvl;
    }

    public int getSizeInBytes()
    {
        int result = 0;
        result += LittleEndianConsts.INT_SIZE;

        for ( ListFormatOverrideLevel lfolvl : _rgLfoLvl )
            result += lfolvl.getSizeInBytes();

        return result;
    }

    void writeTo( ByteArrayOutputStream tableStream ) throws IOException
    {
        LittleEndian.putInt( _cp, tableStream );
        for ( ListFormatOverrideLevel lfolvl : _rgLfoLvl )
        {
            tableStream.write( lfolvl.toByteArray() );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LFOData lfoData = (LFOData) o;

        if (_cp != lfoData._cp) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(_rgLfoLvl, lfoData._rgLfoLvl);

    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_rgLfoLvl);
    }
}
