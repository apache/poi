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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hwpf.model.types.FSPAAbstractType;
import org.apache.poi.util.Internal;

/**
 * This class holds all the FSPA (File Shape Address) structures.
 */
@Internal
public final class FSPATable
{

    private final Map<Integer, GenericPropertyNode> _byStart = new LinkedHashMap<>();

    public FSPATable( byte[] tableStream, FileInformationBlock fib,
            FSPADocumentPart part )
    {
        int offset = fib.getFSPAPlcfOffset( part );
        int length = fib.getFSPAPlcfLength( part );

        PlexOfCps plex = new PlexOfCps(tableStream, offset, length, FSPAAbstractType.getSize() );
        for ( int i = 0; i < plex.length(); i++ )
        {
            GenericPropertyNode property = plex.getProperty( i );
            _byStart.put(property.getStart(), property );
        }
    }

    public FSPA getFspaFromCp( int cp )
    {
        GenericPropertyNode propertyNode = _byStart.get(cp);
        if ( propertyNode == null )
        {
            return null;
        }
        return new FSPA( propertyNode.getBytes(), 0 );
    }

    public List<FSPA> getShapes()
    {
        List<FSPA> result = new ArrayList<>(_byStart.size());
        for ( GenericPropertyNode propertyNode : _byStart.values() )
        {
            result.add( new FSPA( propertyNode.getBytes(), 0 ) );
        }
        return result;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append( "[FPSA PLC size=" ).append( _byStart.size() ).append( "]\n" );

        for ( Map.Entry<Integer, GenericPropertyNode> entry : _byStart.entrySet() ) {
            Integer i = entry.getKey();
            buf.append( "  " ).append(i).append( " => \t" );

            try {
                FSPA fspa = getFspaFromCp(i);
                buf.append(fspa);
            } catch ( Exception exc ) {
                buf.append( exc.getMessage() );
            }
            buf.append( "\n" );
        }
        buf.append( "[/FSPA PLC]" );
        return buf.toString();
    }
}
