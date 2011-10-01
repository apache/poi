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

import org.apache.poi.util.Internal;

@Internal
public final class ListFormatOverride
{
    private LFO _lfo;

    private LFOData _lfoData;

    public ListFormatOverride( byte[] buf, int offset )
    {
        _lfo = new LFO( buf, offset );
    }

    public ListFormatOverride( int lsid )
    {
        _lfo = new LFO();
        _lfo.setLsid( lsid );
    }

    public ListFormatOverrideLevel[] getLevelOverrides()
    {
        return _lfoData.getRgLfoLvl();
    }

    LFO getLfo()
    {
        return _lfo;
    }

    LFOData getLfoData()
    {
        return _lfoData;
    }

    public int getLsid()
    {
        return _lfo.getLsid();
    }

    public ListFormatOverrideLevel getOverrideLevel( int level )
    {
        ListFormatOverrideLevel retLevel = null;
        for ( int x = 0; x < getLevelOverrides().length; x++ )
        {
            if ( getLevelOverrides()[x].getLevelNum() == level )
            {
                retLevel = getLevelOverrides()[x];
            }
        }
        return retLevel;
    }

    public int numOverrides()
    {
        return _lfo.getClfolvl();
    }

    void setLfoData( LFOData _lfoData )
    {
        this._lfoData = _lfoData;
    }

    public void setLsid( int lsid )
    {
        _lfo.setLsid( lsid );
    }

    public void setOverride( int index, ListFormatOverrideLevel lfolvl )
    {
        getLevelOverrides()[index] = lfolvl;
    }

    public byte[] toByteArray()
    {
        return _lfo.serialize();
    }
}
