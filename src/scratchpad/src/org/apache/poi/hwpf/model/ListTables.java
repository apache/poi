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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

import org.apache.poi.hwpf.model.io.HWPFOutputStream;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * @author Ryan Ackley
 */
@Internal
public final class ListTables
{
  private static POILogger log = POILogFactory.getLogger(ListTables.class);

    /**
     * Both PlfLst and the following LVLs
     */
    private final LinkedHashMap<Integer, ListData> _listMap = new LinkedHashMap<Integer, ListData>();
    private PlfLfo _plfLfo;

  public ListTables()
  {

  }

    public ListTables( byte[] tableStream, final int lstOffset,
            final int fcPlfLfo, final int lcbPlfLfo )
    {

        /*
         * The PlfLst structure contains the list formatting information for the
         * document. -- Page 425 of 621. [MS-DOC] -- v20110315 Word (.doc)
         * Binary File Format
         */
        int offset = lstOffset;

        int cLst = LittleEndian.getShort( tableStream, offset );
        offset += LittleEndian.SHORT_SIZE;
        int levelOffset = offset + ( cLst * LSTF.getSize() );

        for ( int x = 0; x < cLst; x++ )
        {
            ListData lst = new ListData( tableStream, offset );
            _listMap.put( Integer.valueOf( lst.getLsid() ), lst );
            offset += LSTF.getSize();

            int num = lst.numLevels();
            for ( int y = 0; y < num; y++ )
            {
                ListLevel lvl = new ListLevel();
                levelOffset += lvl.read( tableStream, levelOffset );
                lst.setLevel( y, lvl );
            }
        }

        this._plfLfo = new PlfLfo( tableStream, fcPlfLfo, lcbPlfLfo );
    }

    public void writeListDataTo( FileInformationBlock fib,
            HWPFOutputStream tableStream ) throws IOException
    {
        final int startOffset = tableStream.getOffset();
        fib.setFcPlfLst( startOffset );

    int listSize = _listMap.size();

    // use this stream as a buffer for the levels since their size varies.
    ByteArrayOutputStream levelBuf = new ByteArrayOutputStream();

    byte[] shortHolder = new byte[2];
    LittleEndian.putShort(shortHolder, (short)listSize);
    tableStream.write(shortHolder);

    for(Integer x : _listMap.keySet()) {
      ListData lst = _listMap.get(x);
      tableStream.write(lst.toByteArray());
      ListLevel[] lvls = lst.getLevels();
      for (int y = 0; y < lvls.length; y++)
      {
        levelBuf.write(lvls[y].toByteArray());
      }
    }

        /*
         * An array of LVLs is appended to the PlfLst. lcbPlfLst does not
         * account for the array of LVLs. -- Page 76 of 621 -- [MS-DOC] --
         * v20110315 Word (.doc) Binary File Format
         */
        fib.setLcbPlfLst( tableStream.getOffset() - startOffset );
        tableStream.write( levelBuf.toByteArray() );
    }

    public void writeListOverridesTo( FileInformationBlock fib,
            HWPFOutputStream tableStream ) throws IOException
    {
        _plfLfo.writeTo( fib, tableStream );
    }

    public LFO getLfo( int ilfo ) throws NoSuchElementException
    {
        return _plfLfo.getLfo( ilfo );
    }

    public LFOData getLfoData( int ilfo ) throws NoSuchElementException
    {
        return _plfLfo.getLfoData( ilfo );
    }

    public int getOverrideIndexFromListID( int lsid )
            throws NoSuchElementException
    {
        return _plfLfo.getIlfoByLsid( lsid );
    }

  public ListLevel getLevel(int lsid, int level)
  {
    ListData lst = _listMap.get(Integer.valueOf(lsid));
    if(level < lst.numLevels()) {
    	ListLevel lvl = lst.getLevels()[level];
    	return lvl;
    }
	log.log(POILogger.WARN, "Requested level " + level + " which was greater than the maximum defined (" + lst.numLevels() + ")");
	return null;
  }

  public ListData getListData(int lsid)
  {
    return _listMap.get(Integer.valueOf(lsid));
  }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ( ( _listMap == null ) ? 0 : _listMap.hashCode() );
        result = prime * result
                + ( ( _plfLfo == null ) ? 0 : _plfLfo.hashCode() );
        return result;
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
        ListTables other = (ListTables) obj;
        if ( _listMap == null )
        {
            if ( other._listMap != null )
                return false;
        }
        else if ( !_listMap.equals( other._listMap ) )
            return false;
        if ( _plfLfo == null )
        {
            if ( other._plfLfo != null )
                return false;
        }
        else if ( !_plfLfo.equals( other._plfLfo ) )
            return false;
        return true;
    }

    public int addList( ListData lst, LFO lfo, LFOData lfoData )
    {
        int lsid = lst.getLsid();
        while ( _listMap.get( Integer.valueOf( lsid ) ) != null )
        {
            lsid = lst.resetListID();
            lfo.setLsid( lsid );
        }
        _listMap.put( Integer.valueOf( lsid ), lst );

        if ( lfo == null && lfoData != null )
        {
            throw new IllegalArgumentException(
                    "LFO and LFOData should be specified both or noone" );
        }
        if ( lfo != null )
        {
            _plfLfo.add( lfo, lfoData );
        }
        return lsid;
    }
}
