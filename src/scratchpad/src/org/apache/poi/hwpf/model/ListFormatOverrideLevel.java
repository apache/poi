/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.hwpf.model;

import java.util.Objects;

import org.apache.poi.hwpf.model.types.LFOLVLBaseAbstractType;
import org.apache.poi.util.Internal;

/**
 * The LFOLVL structure contains information that is used to override the
 * formatting information of a corresponding LVL.
 * <p>
 * Class and fields descriptions are quoted from Microsoft Office Word 97-2007
 * Binary File Format and [MS-DOC] - v20110608 Word (.doc) Binary File Format
 */
@Internal
public final class ListFormatOverrideLevel
{
    private LFOLVLBase _base;
    private ListLevel _lvl;

    public ListFormatOverrideLevel( byte[] buf, int offset )
    {
        _base = new LFOLVLBase( buf, offset );
        offset += LFOLVLBaseAbstractType.getSize();

        if ( _base.isFFormatting() )
        {
            _lvl = new ListLevel( buf, offset );
        }
    }

    public boolean equals( Object obj )
    {
        if (!(obj instanceof ListFormatOverrideLevel)) return false;
        ListFormatOverrideLevel lfolvl = (ListFormatOverrideLevel) obj;
        boolean lvlEquality = false;
        if ( _lvl != null )
        {
            lvlEquality = _lvl.equals( lfolvl._lvl );
        }
        else
        {
            lvlEquality = lfolvl._lvl == null;
        }

        return lvlEquality && lfolvl._base.equals( _base );
    }

    public int getIStartAt()
    {
        return _base.getIStartAt();
    }

    public ListLevel getLevel()
    {
        return _lvl;
    }

    public int getLevelNum()
    {
        return _base.getILvl();
    }

    public int getSizeInBytes()
    {
        return _lvl == null ? LFOLVLBaseAbstractType.getSize() : LFOLVLBaseAbstractType.getSize()
                + _lvl.getSizeInBytes();
    }

    @Override
    public int hashCode() {
        return Objects.hash(_base,_lvl);
    }

    public boolean isFormatting()
    {
        return _base.isFFormatting();
    }

    public boolean isStartAt()
    {
        return _base.isFStartAt();
    }

    public byte[] toByteArray()
    {
        int offset = 0;

        byte[] buf = new byte[getSizeInBytes()];
        _base.serialize( buf, offset );
        offset += LFOLVLBaseAbstractType.getSize();

        if ( _lvl != null )
        {
            byte[] levelBuf = _lvl.toByteArray();
            System.arraycopy( levelBuf, 0, buf, offset, levelBuf.length );
        }

        return buf;
    }
}
