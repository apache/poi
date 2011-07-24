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

import org.apache.poi.hwpf.model.types.LFOAbstractType;

public final class ListFormatOverride extends LFOAbstractType
{
    private ListFormatOverrideLevel[] _levelOverrides;

    public ListFormatOverride( byte[] buf, int offset )
    {
        fillFields( buf, offset );

        _levelOverrides = new ListFormatOverrideLevel[getClfolvl()];
    }

    public ListFormatOverride( int lsid )
    {
        setLsid( lsid );
        _levelOverrides = new ListFormatOverrideLevel[0];
    }

    public ListFormatOverrideLevel[] getLevelOverrides()
    {
        return _levelOverrides;
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
        return getClfolvl();
    }

    public void setOverride( int index, ListFormatOverrideLevel lfolvl )
    {
        _levelOverrides[index] = lfolvl;
    }

    public byte[] toByteArray()
    {
        byte[] bs = new byte[getSize()];
        serialize( bs, 0 );
        return bs;
    }
}
