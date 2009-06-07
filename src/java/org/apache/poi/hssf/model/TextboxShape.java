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


package org.apache.poi.hssf.model;

import org.apache.poi.ddf.*;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.usermodel.*;

/**
 * Represents an textbox shape and converts between the highlevel records
 * and lowlevel records for an oval.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TextboxShape
        extends AbstractShape
{
    private EscherContainerRecord spContainer;
    private TextObjectRecord textObjectRecord;
    private ObjRecord objRecord;
    private EscherTextboxRecord escherTextbox;

    /**
     * Creates the low evel records for an textbox.
     *
     * @param hssfShape  The highlevel shape.
     * @param shapeId    The shape id to use for this shape.
     */
    TextboxShape( HSSFTextbox hssfShape, int shapeId )
    {
        spContainer = createSpContainer( hssfShape, shapeId );
        objRecord = createObjRecord( hssfShape, shapeId );
        textObjectRecord = createTextObjectRecord( hssfShape, shapeId );
    }

    /**
     * Creates the low level OBJ record for this shape.
     */
    private ObjRecord createObjRecord( HSSFTextbox hssfShape, int shapeId )
    {
        HSSFShape shape = hssfShape;

        ObjRecord obj = new ObjRecord();
        CommonObjectDataSubRecord c = new CommonObjectDataSubRecord();
        c.setObjectType( (short) ( (HSSFSimpleShape) shape ).getShapeType() );
        c.setObjectId( shapeId );
        c.setLocked( true );
        c.setPrintable( true );
        c.setAutofill( true );
        c.setAutoline( true );
        EndSubRecord e = new EndSubRecord();

        obj.addSubRecord( c );
        obj.addSubRecord( e );

        return obj;
    }

    /**
     * Generates the escher shape records for this shape.
     *
     * @param hssfShape
     * @param shapeId
     */
    private EscherContainerRecord createSpContainer( HSSFTextbox hssfShape, int shapeId )
    {
        HSSFTextbox shape = hssfShape;

        EscherContainerRecord spContainer = new EscherContainerRecord();
        EscherSpRecord sp = new EscherSpRecord();
        EscherOptRecord opt = new EscherOptRecord();
        EscherRecord anchor = new EscherClientAnchorRecord();
        EscherClientDataRecord clientData = new EscherClientDataRecord();
        escherTextbox = new EscherTextboxRecord();

        spContainer.setRecordId( EscherContainerRecord.SP_CONTAINER );
        spContainer.setOptions( (short) 0x000F );
        sp.setRecordId( EscherSpRecord.RECORD_ID );
        sp.setOptions( (short) ( ( EscherAggregate.ST_TEXTBOX << 4 ) | 0x2 ) );

        sp.setShapeId( shapeId );
        sp.setFlags( EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_HASSHAPETYPE );
        opt.setRecordId( EscherOptRecord.RECORD_ID );
        //        opt.addEscherProperty( new EscherBoolProperty( EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, 262144 ) );
        opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.TEXT__TEXTID, 0 ) );
        opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.TEXT__TEXTLEFT, shape.getMarginLeft() ) );
        opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.TEXT__TEXTRIGHT, shape.getMarginRight() ) );
        opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.TEXT__TEXTBOTTOM, shape.getMarginBottom() ) );
        opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.TEXT__TEXTTOP, shape.getMarginTop() ) );

        opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.TEXT__WRAPTEXT, 0 ) );
        opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.TEXT__ANCHORTEXT, 0 ) );
        opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.GROUPSHAPE__PRINT, 0x00080000 ) );

        addStandardOptions( shape, opt );
        HSSFAnchor userAnchor = shape.getAnchor();
        //        if (userAnchor.isHorizontallyFlipped())
        //            sp.setFlags(sp.getFlags() | EscherSpRecord.FLAG_FLIPHORIZ);
        //        if (userAnchor.isVerticallyFlipped())
        //            sp.setFlags(sp.getFlags() | EscherSpRecord.FLAG_FLIPVERT);
        anchor = createAnchor( userAnchor );
        clientData.setRecordId( EscherClientDataRecord.RECORD_ID );
        clientData.setOptions( (short) 0x0000 );
        escherTextbox.setRecordId( EscherTextboxRecord.RECORD_ID );
        escherTextbox.setOptions( (short) 0x0000 );

        spContainer.addChildRecord( sp );
        spContainer.addChildRecord( opt );
        spContainer.addChildRecord( anchor );
        spContainer.addChildRecord( clientData );
        spContainer.addChildRecord( escherTextbox );

        return spContainer;
    }

    /**
     * Textboxes also have an extra TXO record associated with them that most
     * other shapes dont have.
     */
    private TextObjectRecord createTextObjectRecord( HSSFTextbox hssfShape, int shapeId )
    {
        HSSFTextbox shape = hssfShape;

        TextObjectRecord obj = new TextObjectRecord();
        obj.setHorizontalTextAlignment(hssfShape.getHorizontalAlignment());
        obj.setVerticalTextAlignment(hssfShape.getVerticalAlignment());
        obj.setTextLocked(true);
        obj.setTextOrientation(TextObjectRecord.TEXT_ORIENTATION_NONE);
        obj.setStr(shape.getString());

        return obj;
    }

    public EscherContainerRecord getSpContainer()
    {
        return spContainer;
    }

    public ObjRecord getObjRecord()
    {
        return objRecord;
    }

    public TextObjectRecord getTextObjectRecord()
    {
        return textObjectRecord;
    }

    public EscherRecord getEscherTextbox()
    {
        return escherTextbox;
    }
}
