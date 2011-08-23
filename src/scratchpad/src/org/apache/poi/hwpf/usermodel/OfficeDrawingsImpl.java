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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ddf.EscherTertiaryOptRecord;

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherBlipRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordFactory;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hwpf.model.EscherRecordHolder;
import org.apache.poi.hwpf.model.FSPA;
import org.apache.poi.hwpf.model.FSPATable;

public class OfficeDrawingsImpl implements OfficeDrawings
{
    private final EscherRecordHolder _escherRecordHolder;
    private final FSPATable _fspaTable;
    private final byte[] _mainStream;

    public OfficeDrawingsImpl( FSPATable fspaTable,
            EscherRecordHolder escherRecordHolder, byte[] mainStream )
    {
        this._fspaTable = fspaTable;
        this._escherRecordHolder = escherRecordHolder;
        this._mainStream = mainStream;
    }

    private EscherBlipRecord getBitmapRecord( int bitmapIndex )
    {
        List<? extends EscherContainerRecord> bContainers = _escherRecordHolder
                .getBStoreContainers();
        if ( bContainers == null || bContainers.size() != 1 )
            return null;

        EscherContainerRecord bContainer = bContainers.get( 0 );
        final List<EscherRecord> bitmapRecords = bContainer.getChildRecords();

        if ( bitmapRecords.size() < bitmapIndex )
            return null;

        EscherRecord imageRecord = bitmapRecords.get( bitmapIndex - 1 );

        if ( imageRecord instanceof EscherBlipRecord )
        {
            return (EscherBlipRecord) imageRecord;
        }

        if ( imageRecord instanceof EscherBSERecord )
        {
            EscherBSERecord bseRecord = (EscherBSERecord) imageRecord;

            EscherBlipRecord blip = bseRecord.getBlipRecord();
            if ( blip != null )
            {
                return blip;
            }

            if ( bseRecord.getOffset() > 0 )
            {
                /*
                 * Blip stored in delay stream, which in a word doc, is the main
                 * stream
                 */
                EscherRecordFactory recordFactory = new DefaultEscherRecordFactory();
                EscherRecord record = recordFactory.createRecord( _mainStream,
                        bseRecord.getOffset() );

                if ( record instanceof EscherBlipRecord )
                {
                    record.fillFields( _mainStream, bseRecord.getOffset(),
                            recordFactory );
                    return (EscherBlipRecord) record;
                }
            }
        }

        return null;
    }

    private EscherContainerRecord getEscherShapeRecordContainer(
            final int shapeId )
    {
        for ( EscherContainerRecord spContainer : _escherRecordHolder
                .getSpContainers() )
        {
            EscherSpRecord escherSpRecord = spContainer
                    .getChildById( (short) 0xF00A );
            if ( escherSpRecord != null
                    && escherSpRecord.getShapeId() == shapeId )
                return spContainer;
        }

        return null;
    }

    private OfficeDrawing getOfficeDrawing( final FSPA fspa )
    {
        return new OfficeDrawing()
        {
            public HorizontalPositioning getHorizontalPositioning()
            {
                int value = getTertiaryPropertyValue(
                        EscherProperties.GROUPSHAPE__POSH, -1 );

                switch ( value )
                {
                case 0:
                    return HorizontalPositioning.ABSOLUTE;
                case 1:
                    return HorizontalPositioning.LEFT;
                case 2:
                    return HorizontalPositioning.CENTER;
                case 3:
                    return HorizontalPositioning.RIGHT;
                case 4:
                    return HorizontalPositioning.INSIDE;
                case 5:
                    return HorizontalPositioning.OUTSIDE;
                }

                return HorizontalPositioning.ABSOLUTE;
            }

            public HorizontalRelativeElement getHorizontalRelative()
            {
                int value = getTertiaryPropertyValue(
                        EscherProperties.GROUPSHAPE__POSRELH, -1 );

                switch ( value )
                {
                case 1:
                    return HorizontalRelativeElement.MARGIN;
                case 2:
                    return HorizontalRelativeElement.PAGE;
                case 3:
                    return HorizontalRelativeElement.TEXT;
                case 4:
                    return HorizontalRelativeElement.CHAR;
                }

                return HorizontalRelativeElement.TEXT;
            }

            public EscherContainerRecord getOfficeArtSpContainer()
            {
                return getEscherShapeRecordContainer( getShapeId() );
            }

            public byte[] getPictureData()
            {
                EscherContainerRecord shapeDescription = getEscherShapeRecordContainer( getShapeId() );
                if ( shapeDescription == null )
                    return null;

                EscherOptRecord escherOptRecord = shapeDescription
                        .getChildById( EscherOptRecord.RECORD_ID );
                if ( escherOptRecord == null )
                    return null;

                EscherSimpleProperty escherProperty = escherOptRecord
                        .lookup( EscherProperties.BLIP__BLIPTODISPLAY );
                if ( escherProperty == null )
                    return null;

                int bitmapIndex = escherProperty.getPropertyValue();
                EscherBlipRecord escherBlipRecord = getBitmapRecord( bitmapIndex );
                if ( escherBlipRecord == null )
                    return null;

                return escherBlipRecord.getPicturedata();
            }

            public int getRectangleBottom()
            {
                return fspa.getYaBottom();
            }

            public int getRectangleLeft()
            {
                return fspa.getXaLeft();
            }

            public int getRectangleRight()
            {
                return fspa.getXaRight();
            }

            public int getRectangleTop()
            {
                return fspa.getYaTop();
            }

            public int getShapeId()
            {
                return fspa.getSpid();
            }

            private int getTertiaryPropertyValue( int propertyId,
                    int defaultValue )
            {
                EscherContainerRecord shapeDescription = getEscherShapeRecordContainer( getShapeId() );
                if ( shapeDescription == null )
                    return defaultValue;

                EscherTertiaryOptRecord escherTertiaryOptRecord = shapeDescription
                        .getChildById( EscherTertiaryOptRecord.RECORD_ID );
                if ( escherTertiaryOptRecord == null )
                    return defaultValue;

                EscherSimpleProperty escherProperty = escherTertiaryOptRecord
                        .lookup( propertyId );
                if ( escherProperty == null )
                    return defaultValue;
                int value = escherProperty.getPropertyValue();

                return value;
            }

            public VerticalPositioning getVerticalPositioning()
            {
                int value = getTertiaryPropertyValue(
                        EscherProperties.GROUPSHAPE__POSV, -1 );

                switch ( value )
                {
                case 0:
                    return VerticalPositioning.ABSOLUTE;
                case 1:
                    return VerticalPositioning.TOP;
                case 2:
                    return VerticalPositioning.CENTER;
                case 3:
                    return VerticalPositioning.BOTTOM;
                case 4:
                    return VerticalPositioning.INSIDE;
                case 5:
                    return VerticalPositioning.OUTSIDE;
                }

                return VerticalPositioning.ABSOLUTE;
            }

            public VerticalRelativeElement getVerticalRelativeElement()
            {
                int value = getTertiaryPropertyValue(
                        EscherProperties.GROUPSHAPE__POSV, -1 );

                switch ( value )
                {
                case 1:
                    return VerticalRelativeElement.MARGIN;
                case 2:
                    return VerticalRelativeElement.PAGE;
                case 3:
                    return VerticalRelativeElement.TEXT;
                case 4:
                    return VerticalRelativeElement.LINE;
                }

                return VerticalRelativeElement.TEXT;
            }

            @Override
            public String toString()
            {
                return "OfficeDrawingImpl: " + fspa.toString();
            }
        };
    }

    public OfficeDrawing getOfficeDrawingAt( int characterPosition )
    {
        final FSPA fspa = _fspaTable.getFspaFromCp( characterPosition );
        if ( fspa == null )
            return null;

        return getOfficeDrawing( fspa );
    }

    public Collection<OfficeDrawing> getOfficeDrawings()
    {
        List<OfficeDrawing> result = new ArrayList<OfficeDrawing>();
        for ( FSPA fspa : _fspaTable.getShapes() )
        {
            result.add( getOfficeDrawing( fspa ) );
        }
        return Collections.unmodifiableList( result );
    }
}
