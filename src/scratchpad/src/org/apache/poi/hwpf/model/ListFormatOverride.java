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

    private ListFormatOverrideLevel[] _levelOverrides;

    private LFO _lfo;

    public ListFormatOverride( byte[] buf, int offset )
    {
        _lfo = new LFO( buf, offset );
        _levelOverrides = new ListFormatOverrideLevel[_lfo.getClfolvl()];
    }

    public ListFormatOverride( int lsid )
    {
        _lfo = new LFO();
        _lfo.setLsid( lsid );
        _levelOverrides = new ListFormatOverrideLevel[0];
    }

    public ListFormatOverrideLevel[] getLevelOverrides()
    {
        return _levelOverrides;
    }

    public int getLsid()
    {
        return _lfo.getLsid();
    }

    public ListFormatOverrideLevel getOverrideLevel( int level )
    {

        ListFormatOverrideLevel retLevel = null;

        for ( int x = 0; x < _levelOverrides.length; x++ )
        {
            if ( _levelOverrides[x].getLevelNum() == level )
            {
                retLevel = _levelOverrides[x];
            }
        }
        return retLevel;
    }

    public int numOverrides()
    {
        return _lfo.getClfolvl();
    }

    public void setLsid( int lsid )
    {
        _lfo.setLsid( lsid );
    }

    public void setOverride( int index, ListFormatOverrideLevel lfolvl )
    {
        _levelOverrides[index] = lfolvl;
    }

    public byte[] toByteArray()
    {
        return _lfo.serialize();
    }
}
