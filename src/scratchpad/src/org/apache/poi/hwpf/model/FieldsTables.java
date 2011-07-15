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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hwpf.model.io.HWPFOutputStream;

/**
 * This class provides access to all the fields Plex.
 * 
 * @author Cedric Bosdonnat <cbosdonnat@novell.com>
 * 
 */
public class FieldsTables
{
    private static final class GenericPropertyNodeComparator implements
            Comparator<GenericPropertyNode>
    {
        public int compare( GenericPropertyNode o1, GenericPropertyNode o2 )
        {
            int thisVal = o1.getStart();
            int anotherVal = o2.getStart();
            return thisVal < anotherVal ? -1 : thisVal == anotherVal ? 0 : 1;
        }
    }

    private GenericPropertyNodeComparator comparator = new GenericPropertyNodeComparator();

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

    private Map<Integer, PlexOfCps> _tables;
    private Map<Integer, Map<Integer, Field>> _fieldsByOffset;

    public FieldsTables( byte[] tableStream, FileInformationBlock fib )
    {
        _tables = new HashMap<Integer, PlexOfCps>( PLCFFLDTXBX - PLCFFLDATN + 1 );
        _fieldsByOffset = new HashMap<Integer, Map<Integer, Field>>(
                PLCFFLDTXBX - PLCFFLDATN + 1 );

        for ( int i = PLCFFLDATN; i <= PLCFFLDTXBX; i++ )
        {
            final PlexOfCps plexOfCps = readPLCF( tableStream, fib, i );
            _fieldsByOffset.put( Integer.valueOf( i ),
                    parseFieldStructure( plexOfCps ) );
            _tables.put( Integer.valueOf( i ), plexOfCps );
        }
    }

    private Map<Integer, Field> parseFieldStructure( PlexOfCps plexOfCps )
    {
        if (plexOfCps == null)
            return new HashMap<Integer, Field>();

        GenericPropertyNode[] nodes = plexOfCps.toPropertiesArray();
        Arrays.sort( nodes, comparator );
        List<Field> fields = new ArrayList<Field>( nodes.length / 3 + 1 );
        parseFieldStructureImpl( nodes, 0, nodes.length, fields );

        HashMap<Integer, Field> result = new HashMap<Integer, Field>(
                fields.size() );
        for ( Field field : fields )
        {
            result.put( Integer.valueOf( field.getFieldStartOffset() ), field );
        }
        return result;
    }

    private void parseFieldStructureImpl( GenericPropertyNode[] nodes,
            int startOffsetInclusive, int endOffsetExclusive, List<Field> result )
    {
        int next = startOffsetInclusive;
        while ( next < endOffsetExclusive )
        {
            GenericPropertyNode startNode = nodes[next];
            PlexOfField startPlexOfField = new PlexOfField( startNode );
            if ( startPlexOfField.getFld().getBoundaryType() != FieldDescriptor.FIELD_BEGIN_MARK )
            {
                /* Start mark seems to be missing */
                next++;
                continue;
            }

            /*
             * we have start node. end offset points to next node, separator or
             * end
             */
            int nextNodePositionInArray = binarySearch( nodes, next + 1,
                    endOffsetExclusive, startNode.getEnd() );
            if ( nextNodePositionInArray < 0 )
            {
                /*
                 * too bad, this start field mark doesn't have corresponding end
                 * field mark or separator field mark in fields table
                 */
                next++;
                continue;
            }
            GenericPropertyNode nextNode = nodes[nextNodePositionInArray];
            PlexOfField nextPlexOfField = new PlexOfField( nextNode );

            switch ( nextPlexOfField.getFld().getBoundaryType() )
            {
            case FieldDescriptor.FIELD_SEPARATOR_MARK:
            {
                GenericPropertyNode separatorNode = nextNode;
                PlexOfField separatorPlexOfField = nextPlexOfField;

                int endNodePositionInArray = binarySearch( nodes,
                        nextNodePositionInArray, endOffsetExclusive,
                        separatorNode.getEnd() );
                if ( endNodePositionInArray < 0 )
                {
                    /*
                     * too bad, this separator field mark doesn't have
                     * corresponding end field mark in fields table
                     */
                    next++;
                    continue;
                }
                GenericPropertyNode endNode = nodes[endNodePositionInArray];
                PlexOfField endPlexOfField = new PlexOfField( endNode );

                if ( endPlexOfField.getFld().getBoundaryType() != FieldDescriptor.FIELD_END_MARK )
                {
                    /* Not and ending mark */
                    next++;
                    continue;
                }

                Field field = new Field( startPlexOfField,
                        separatorPlexOfField, endPlexOfField );
                result.add( field );

                // adding included fields
                if ( startNode.getStart() + 1 < separatorNode.getStart() - 1 )
                {
                    parseFieldStructureImpl( nodes, next + 1,
                            nextNodePositionInArray, result );
                }
                if ( separatorNode.getStart() + 1 < endNode.getStart() - 1 )
                {
                    parseFieldStructureImpl( nodes,
                            nextNodePositionInArray + 1,
                            endNodePositionInArray, result );
                }

                next = endNodePositionInArray + 1;

                break;
            }
            case FieldDescriptor.FIELD_END_MARK:
            {
                // we have no separator
                Field field = new Field( startPlexOfField, null,
                        nextPlexOfField );
                result.add( field );

                // adding included fields
                if ( startNode.getStart() + 1 < nextNode.getStart() - 1 )
                {
                    parseFieldStructureImpl( nodes, next + 1,
                            nextNodePositionInArray, result );
                }

                next = nextNodePositionInArray + 1;
                break;
            }
            case FieldDescriptor.FIELD_BEGIN_MARK:
            default:
            {
                /* something is wrong, ignoring this mark along with start mark */
                next++;
                continue;
            }
            }
        }
    }

    /**
     * This is port and adaptation of Arrays.binarySearch from Java 6 (Apache
     * Harmony).
     */
    public static <T> int binarySearch( GenericPropertyNode[] array,
            int startIndex, int endIndex, int requiredStartOffset )
    {
        checkIndexForBinarySearch( array.length, startIndex, endIndex );

        int low = startIndex, mid = -1, high = endIndex - 1, result = 0;
        while ( low <= high )
        {
            mid = ( low + high ) >>> 1;
            int midStart = array[mid].getStart();

            if ( midStart == requiredStartOffset )
            {
                return mid;
            }
            else if ( midStart < requiredStartOffset )
            {
                low = mid + 1;
            }
            else
            {
                high = mid - 1;
            }
        }
        if ( mid < 0 )
        {
            int insertPoint = endIndex;
            for ( int index = startIndex; index < endIndex; index++ )
            {
                if ( requiredStartOffset < array[index].getStart() )
                {
                    insertPoint = index;
                }
            }
            return -insertPoint - 1;
        }
        return -mid - ( result >= 0 ? 1 : 2 );
    }

    private static void checkIndexForBinarySearch( int length, int start,
            int end )
    {
        if ( start > end )
        {
            throw new IllegalArgumentException();
        }
        if ( length < end || 0 > start )
        {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public Field lookupFieldByStartOffset( int documentPart, int offset )
    {
        Map<Integer, Field> map = _fieldsByOffset.get( Integer
                .valueOf( documentPart ) );
        if ( map == null || map.isEmpty() )
            return null;

        return map.get( Integer.valueOf( offset ) );
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
