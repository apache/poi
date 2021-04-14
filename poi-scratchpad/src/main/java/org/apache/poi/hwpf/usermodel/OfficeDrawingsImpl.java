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

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherBlipRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordFactory;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.ddf.EscherTertiaryOptRecord;
import org.apache.poi.hwpf.model.FSPA;
import org.apache.poi.hwpf.model.FSPATable;
import org.apache.poi.hwpf.model.OfficeArtContent;

public class OfficeDrawingsImpl implements OfficeDrawings
{
    private final OfficeArtContent officeArtContent;
    private final FSPATable _fspaTable;
    private final byte[] _mainStream;

    public OfficeDrawingsImpl(FSPATable fspaTable,
                              OfficeArtContent officeArtContent, byte[] mainStream )
    {
        this._fspaTable = fspaTable;
        this.officeArtContent = officeArtContent;
        this._mainStream = mainStream;
    }

    private EscherBlipRecord getBitmapRecord( int bitmapIndex )
    {
        EscherContainerRecord bContainer = officeArtContent.getBStoreContainer();
        if (bContainer == null)
            return null;

        if ( bContainer.getChildCount() < bitmapIndex )
            return null;

        EscherRecord imageRecord = bContainer.getChild( bitmapIndex - 1 );

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
        for ( EscherContainerRecord spContainer : officeArtContent
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
            @Override
            public HorizontalPositioning getHorizontalPositioning()
            {
                int value = getTertiaryPropertyValue(EscherPropertyTypes.GROUPSHAPE__POSH );

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

            @Override
            public HorizontalRelativeElement getHorizontalRelative()
            {
                int value = getTertiaryPropertyValue( EscherPropertyTypes.GROUPSHAPE__POSRELH );

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

            @Override
            public EscherContainerRecord getOfficeArtSpContainer()
            {
                return getEscherShapeRecordContainer( getShapeId() );
            }

            @Override
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
                        .lookup( EscherPropertyTypes.BLIP__BLIPTODISPLAY );
                if ( escherProperty == null )
                    return null;

                int bitmapIndex = escherProperty.getPropertyValue();
                EscherBlipRecord escherBlipRecord = getBitmapRecord( bitmapIndex );
                if ( escherBlipRecord == null )
                    return null;

                return escherBlipRecord.getPicturedata();
            }

            @Override
            public int getRectangleBottom()
            {
                return fspa.getYaBottom();
            }

            @Override
            public int getRectangleLeft()
            {
                return fspa.getXaLeft();
            }

            @Override
            public int getRectangleRight()
            {
                return fspa.getXaRight();
            }

            @Override
            public int getRectangleTop()
            {
                return fspa.getYaTop();
            }

            @Override
            public int getShapeId()
            {
                return fspa.getSpid();
            }

            private int getTertiaryPropertyValue( EscherPropertyTypes type ) {
                EscherContainerRecord shapeDescription = getEscherShapeRecordContainer( getShapeId() );
                if ( shapeDescription == null ) {
                    return -1;
                }

                EscherTertiaryOptRecord escherTertiaryOptRecord = shapeDescription
                        .getChildById( EscherTertiaryOptRecord.RECORD_ID );
                if ( escherTertiaryOptRecord == null ) {
                    return -1;
                }

                EscherSimpleProperty escherProperty = escherTertiaryOptRecord.lookup( type );
                return ( escherProperty == null ) ? -1 : escherProperty.getPropertyValue();
            }

            @Override
            public VerticalPositioning getVerticalPositioning()
            {
                int value = getTertiaryPropertyValue( EscherPropertyTypes.GROUPSHAPE__POSV );

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

            @Override
            public VerticalRelativeElement getVerticalRelativeElement()
            {
                int value = getTertiaryPropertyValue( EscherPropertyTypes.GROUPSHAPE__POSV );

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
                return "OfficeDrawingImpl: " + fspa;
            }
        };
    }

    @Override
    public OfficeDrawing getOfficeDrawingAt( int characterPosition )
    {
        final FSPA fspa = _fspaTable.getFspaFromCp( characterPosition );
        if ( fspa == null )
            return null;

        return getOfficeDrawing( fspa );
    }

    @Override
    public Collection<OfficeDrawing> getOfficeDrawings()
    {
        List<OfficeDrawing> result = new ArrayList<>();
        for ( FSPA fspa : _fspaTable.getShapes() )
        {
            result.add( getOfficeDrawing( fspa ) );
        }
        return Collections.unmodifiableList( result );
    }
}
