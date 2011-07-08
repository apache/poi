/*
 *  ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.apache.poi.hwpf.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.hwpf.model.io.HWPFOutputStream;

/**
 * This class provides access to all the fields Plex.
 * 
 * @author Cedric Bosdonnat <cbosdonnat@novell.com>
 * 
 */
public class FieldsTables
{
    /**
     * annotation subdocument
     */
    public static final int PLCFFLDATN = 0;
    /**
     * endnote subdocument
     */
    public static final int PLCFFLDEDN = 1;
    /**
     * footnote subdocument
     */
    public static final int PLCFFLDFTN = 2;
    /**
     * header subdocument
     */
    public static final int PLCFFLDHDR = 3;
    /**
     * header textbox subdoc
     */
    public static final int PLCFFLDHDRTXBX = 4;
    /**
     * main document
     */
    public static final int PLCFFLDMOM = 5;
    /**
     * textbox subdoc
     */
    public static final int PLCFFLDTXBX = 6;

    // The size in bytes of the FLD data structure
    private static final int FLD_SIZE = 2;

    private HashMap<Integer, PlexOfCps> _tables;

    public FieldsTables( byte[] tableStream, FileInformationBlock fib )
    {
        _tables = new HashMap<Integer, PlexOfCps>();

        for ( int i = PLCFFLDATN; i <= PLCFFLDTXBX; i++ )
        {
            _tables.put( Integer.valueOf( i ), readPLCF( tableStream, fib, i ) );
        }
    }

    private PlexOfCps readPLCF( byte[] tableStream, FileInformationBlock fib,
            int type )
    {
        int start = 0;
        int length = 0;

        switch ( type )
        {
        case PLCFFLDATN:
            start = fib.getFcPlcffldAtn();
            length = fib.getLcbPlcffldAtn();
            break;
        case PLCFFLDEDN:
            start = fib.getFcPlcffldEdn();
            length = fib.getLcbPlcffldEdn();
            break;
        case PLCFFLDFTN:
            start = fib.getFcPlcffldFtn();
            length = fib.getLcbPlcffldFtn();
            break;
        case PLCFFLDHDR:
            start = fib.getFcPlcffldHdr();
            length = fib.getLcbPlcffldHdr();
            break;
        case PLCFFLDHDRTXBX:
            start = fib.getFcPlcffldHdrtxbx();
            length = fib.getLcbPlcffldHdrtxbx();
            break;
        case PLCFFLDMOM:
            start = fib.getFcPlcffldMom();
            length = fib.getLcbPlcffldMom();
            break;
        case PLCFFLDTXBX:
            start = fib.getFcPlcffldTxbx();
            length = fib.getLcbPlcffldTxbx();
            break;
        default:
            break;
        }

        if ( start <= 0 || length <= 0 )
            return null;

        return new PlexOfCps( tableStream, start, length, FLD_SIZE );
    }

    public ArrayList<PlexOfField> getFieldsPLCF( int type )
    {
        return toArrayList( _tables.get( Integer.valueOf( type ) ) );
    }

    private static ArrayList<PlexOfField> toArrayList( PlexOfCps plexOfCps )
    {
        if ( plexOfCps == null )
            return new ArrayList<PlexOfField>();

        ArrayList<PlexOfField> fields = new ArrayList<PlexOfField>();
        fields.ensureCapacity( plexOfCps.length() );

        for ( int i = 0; i < plexOfCps.length(); i++ )
        {
            GenericPropertyNode propNode = plexOfCps.getProperty( i );
            PlexOfField plex = new PlexOfField( propNode );
            fields.add( plex );
        }

        return fields;
    }

    private int savePlex( PlexOfCps plexOfCps, int type,
            FileInformationBlock fib, HWPFOutputStream outputStream )
            throws IOException
    {
        if ( plexOfCps == null || plexOfCps.length() == 0 )
            return 0;

        byte[] data = plexOfCps.toByteArray();

        int start = outputStream.getOffset();
        int length = data.length;

        outputStream.write( data );

        switch ( type )
        {
        case PLCFFLDATN:
            fib.setFcPlcffldAtn( start );
            fib.setLcbPlcffldAtn( length );
            break;
        case PLCFFLDEDN:
            fib.setFcPlcffldEdn( start );
            fib.setLcbPlcffldEdn( length );
            break;
        case PLCFFLDFTN:
            fib.setFcPlcffldFtn( start );
            fib.setLcbPlcffldFtn( length );
            break;
        case PLCFFLDHDR:
            fib.setFcPlcffldHdr( start );
            fib.setLcbPlcffldHdr( length );
            break;
        case PLCFFLDHDRTXBX:
            fib.setFcPlcffldHdrtxbx( start );
            fib.setLcbPlcffldHdrtxbx( length );
            break;
        case PLCFFLDMOM:
            fib.setFcPlcffldMom( start );
            fib.setLcbPlcffldMom( length );
            break;
        case PLCFFLDTXBX:
            fib.setFcPlcffldTxbx( start );
            fib.setLcbPlcffldTxbx( length );
            break;
        default:
            return 0;
        }

        return length;
    }

    public void write( FileInformationBlock fib, HWPFOutputStream tableStream )
            throws IOException
    {
        for ( int i = PLCFFLDATN; i <= PLCFFLDTXBX; i++ )
        {
            PlexOfCps plexOfCps = _tables.get( Integer.valueOf( i ) );
            savePlex( plexOfCps, i, fib, tableStream );
        }
    }
}
