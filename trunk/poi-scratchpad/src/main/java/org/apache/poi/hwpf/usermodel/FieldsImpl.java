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
package org.apache.poi.hwpf.usermodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hwpf.model.FieldDescriptor;
import org.apache.poi.hwpf.model.FieldsDocumentPart;
import org.apache.poi.hwpf.model.FieldsTables;
import org.apache.poi.hwpf.model.PlexOfField;
import org.apache.poi.util.Internal;

/**
 * Default implementation of {@link Field}
 */
@Internal
public class FieldsImpl implements Fields
{
    /**
     * This is port and adaptation of Arrays.binarySearch from Java 6 (Apache
     * Harmony).
     */
    private static int binarySearch( List<PlexOfField> list,
            int startIndex, int endIndex, int requiredStartOffset )
    {
        checkIndexForBinarySearch( list.size(), startIndex, endIndex );

        int low = startIndex, mid = -1, high = endIndex - 1;
        while ( low <= high )
        {
            mid = ( low + high ) >>> 1;
            int midStart = list.get( mid ).getFcStart();

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
                if ( requiredStartOffset < list.get( index ).getFcStart() )
                {
                    insertPoint = index;
                }
            }
            return -insertPoint - 1;
        }
        return -mid - 1;
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

    private Map<FieldsDocumentPart, Map<Integer, FieldImpl>> _fieldsByOffset;

    private PlexOfFieldComparator comparator = new PlexOfFieldComparator();

    public FieldsImpl( FieldsTables fieldsTables )
    {
        _fieldsByOffset = new HashMap<>(
                FieldsDocumentPart.values().length);

        for ( FieldsDocumentPart part : FieldsDocumentPart.values() )
        {
            List<PlexOfField> plexOfCps = fieldsTables.getFieldsPLCF( part );
            _fieldsByOffset.put( part, parseFieldStructure( plexOfCps ) );
        }
    }

    public Collection<Field> getFields( FieldsDocumentPart part )
    {
        Map<Integer, FieldImpl> map = _fieldsByOffset.get( part );
        if ( map == null || map.isEmpty() )
            return Collections.emptySet();

        return Collections.unmodifiableCollection( map.values() );
    }

    public FieldImpl getFieldByStartOffset( FieldsDocumentPart documentPart,
            int offset )
    {
        Map<Integer, FieldImpl> map = _fieldsByOffset.get( documentPart );
        if ( map == null || map.isEmpty() )
            return null;

        return map.get(offset);
    }

    private Map<Integer, FieldImpl> parseFieldStructure(
            List<PlexOfField> plexOfFields )
    {
        if ( plexOfFields == null || plexOfFields.isEmpty() )
            return new HashMap<>();

        plexOfFields.sort(comparator);
        List<FieldImpl> fields = new ArrayList<>(
                plexOfFields.size() / 3 + 1);
        parseFieldStructureImpl( plexOfFields, 0, plexOfFields.size(), fields );

        HashMap<Integer, FieldImpl> result = new HashMap<>(
                fields.size());
        for ( FieldImpl field : fields )
        {
            result.put(field.getFieldStartOffset(), field );
        }
        return result;
    }

    @SuppressWarnings("UnnecessaryContinue")
    private void parseFieldStructureImpl(List<PlexOfField> plexOfFields,
                                         int startOffsetInclusive, int endOffsetExclusive,
                                         List<FieldImpl> result )
    {
        int next = startOffsetInclusive;
        while ( next < endOffsetExclusive )
        {
            PlexOfField startPlexOfField = plexOfFields.get( next );
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
            int nextNodePositionInList = binarySearch( plexOfFields, next + 1,
                    endOffsetExclusive, startPlexOfField.getFcEnd() );
            if ( nextNodePositionInList < 0 )
            {
                /*
                 * too bad, this start field mark doesn't have corresponding end
                 * field mark or separator field mark in fields table
                 */
                next++;
                continue;
            }
            PlexOfField nextPlexOfField = plexOfFields
                    .get( nextNodePositionInList );

            switch ( nextPlexOfField.getFld().getBoundaryType() )
            {
            case FieldDescriptor.FIELD_SEPARATOR_MARK:
            {

                int endNodePositionInList = binarySearch( plexOfFields,
                        nextNodePositionInList, endOffsetExclusive,
                        nextPlexOfField.getFcEnd() );
                if ( endNodePositionInList < 0 )
                {
                    /*
                     * too bad, this separator field mark doesn't have
                     * corresponding end field mark in fields table
                     */
                    next++;
                    continue;
                }
                PlexOfField endPlexOfField = plexOfFields
                        .get( endNodePositionInList );

                if ( endPlexOfField.getFld().getBoundaryType() != FieldDescriptor.FIELD_END_MARK )
                {
                    /* Not and ending mark */
                    next++;
                    continue;
                }

                FieldImpl field = new FieldImpl( startPlexOfField,
                        nextPlexOfField, endPlexOfField );
                result.add( field );

                // adding included fields
                if ( startPlexOfField.getFcStart() + 1 < nextPlexOfField
                        .getFcStart() - 1 )
                {
                    parseFieldStructureImpl( plexOfFields, next + 1,
                            nextNodePositionInList, result );
                }
                if ( nextPlexOfField.getFcStart() + 1 < endPlexOfField
                        .getFcStart() - 1 )
                {
                    parseFieldStructureImpl( plexOfFields,
                            nextNodePositionInList + 1, endNodePositionInList,
                            result );
                }

                next = endNodePositionInList + 1;

                break;
            }
            case FieldDescriptor.FIELD_END_MARK:
            {
                // we have no separator
                FieldImpl field = new FieldImpl( startPlexOfField, null,
                        nextPlexOfField );
                result.add( field );

                // adding included fields
                if ( startPlexOfField.getFcStart() + 1 < nextPlexOfField
                        .getFcStart() - 1 )
                {
                    parseFieldStructureImpl( plexOfFields, next + 1,
                            nextNodePositionInList, result );
                }

                next = nextNodePositionInList + 1;
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

    private static final class PlexOfFieldComparator implements Comparator<PlexOfField>, Serializable {
        public int compare( PlexOfField o1, PlexOfField o2 )
        {
            int thisVal = o1.getFcStart();
            int anotherVal = o2.getFcStart();
            return Integer.compare(thisVal, anotherVal);
        }
    }

}
