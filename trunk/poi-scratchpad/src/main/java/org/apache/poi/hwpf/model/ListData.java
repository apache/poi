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

import java.util.Arrays;

import org.apache.poi.util.Internal;

@Internal
public final class ListData
{
    private ListLevel[] _levels;

    private LSTF _lstf;

    ListData( byte[] buf, int offset )
    {
        _lstf = new LSTF( buf, offset );

        if ( _lstf.isFSimpleList() )
        {
            _levels = new ListLevel[1];
        }
        else
        {
            _levels = new ListLevel[9];
        }
    }

    public ListData( int listID, boolean numbered )
    {
        _lstf = new LSTF();
        _lstf.setLsid( listID );
        _lstf.setRgistdPara( new short[9] );
        Arrays.fill( _lstf.getRgistdPara(), (short) StyleSheet.NIL_STYLE );

        _levels = new ListLevel[9];
        for ( int x = 0; x < _levels.length; x++ )
        {
            _levels[x] = new ListLevel( x, numbered );
        }
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
        ListData other = (ListData) obj;
        if ( !Arrays.equals( _levels, other._levels ) )
            return false;
        if ( _lstf == null )
        {
            if ( other._lstf != null )
                return false;
        }
        else if ( !_lstf.equals( other._lstf ) )
            return false;
        return true;
    }

    /**
     * Gets the level associated to a particular List at a particular index.
     *
     * @param index
     *            1-based index
     * @return a list level
     */
    public ListLevel getLevel( int index )
    {
        return _levels[index - 1];
    }

    public ListLevel[] getLevels()
    {
        return _levels;
    }

    public int getLevelStyle( int index )
    {
        return _lstf.getRgistdPara()[index];
    }

    public int getLsid()
    {
        return _lstf.getLsid();
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{_levels,_lstf});
    }

    public int numLevels()
    {
        return _levels.length;
    }

    int resetListID()
    {
        _lstf.setLsid( (int) ( Math.random() * System.currentTimeMillis() ) );
        return _lstf.getLsid();
    }

    public void setLevel( int index, ListLevel level )
    {
        _levels[index] = level;
    }

    public void setLevelStyle( int index, int styleIndex )
    {
        _lstf.getRgistdPara()[index] = (short) styleIndex;
    }

    public byte[] toByteArray()
    {
        return _lstf.serialize();
    }
}
