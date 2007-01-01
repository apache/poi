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
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.usermodel.*;

/**
 * An abstract shape is the lowlevel model for a shape.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public abstract class AbstractShape
{
    /**
     * Create a new shape object used to create the escher records.
     *
     * @param hssfShape     The simple shape this is based on.
     */
    public static AbstractShape createShape( HSSFShape hssfShape, int shapeId )
    {
        AbstractShape shape;
        if (hssfShape instanceof HSSFComment)
        {
            shape = new CommentShape( (HSSFComment)hssfShape, shapeId );
        }
        else if (hssfShape instanceof HSSFTextbox)
        {
            shape = new TextboxShape( (HSSFTextbox)hssfShape, shapeId );
        }
        else if (hssfShape instanceof HSSFPolygon)
        {
            shape = new PolygonShape( (HSSFPolygon) hssfShape, shapeId );
        }
        else if (hssfShape instanceof HSSFSimpleShape)
        {
            HSSFSimpleShape simpleShape = (HSSFSimpleShape) hssfShape;
            switch ( simpleShape.getShapeType() )
            {
                case HSSFSimpleShape.OBJECT_TYPE_PICTURE:
                    shape = new PictureShape( simpleShape, shapeId );
                    break;
                case HSSFSimpleShape.OBJECT_TYPE_LINE:
                    shape = new LineShape( simpleShape, shapeId );
                    break;
                case HSSFSimpleShape.OBJECT_TYPE_OVAL:
                case HSSFSimpleShape.OBJECT_TYPE_RECTANGLE:
                    shape = new SimpleFilledShape( simpleShape, shapeId );
                    break;
                default:
                    throw new IllegalArgumentException("Do not know how to handle this type of shape");
            }
        }
        else
        {
            throw new IllegalArgumentException("Unknown shape type");
        }
        EscherSpRecord sp = shape.getSpContainer().getChildById(EscherSpRecord.RECORD_ID);
        if (hssfShape.getParent() != null)
            sp.setFlags(sp.getFlags() | EscherSpRecord.FLAG_CHILD);
        return shape;
    }

    protected AbstractShape()
    {
    }

    /**
     * @return  The shape container and it's children that can represent this
     *          shape.
     */
    public abstract EscherContainerRecord getSpContainer();

    /**
     * @return  The object record that is associated with this shape.
     */
    public abstract ObjRecord getObjRecord();

    /**
     * Creates an escher anchor record from a HSSFAnchor.
     *
     * @param userAnchor    The high level anchor to convert.
     * @return  An escher anchor record.
     */
    protected EscherRecord createAnchor( HSSFAnchor userAnchor )
    {
        return ConvertAnchor.createAnchor(userAnchor);
    }

    /**
     * Add standard properties to the opt record.  These properties effect
     * all records.
     *
     * @param shape     The user model shape.
     * @param opt       The opt record to add the properties to.
     * @return          The number of options added.
     */
    protected int addStandardOptions( HSSFShape shape, EscherOptRecord opt )
    {
        opt.addEscherProperty( new EscherBoolProperty( EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 0x080000 ) );
//        opt.addEscherProperty( new EscherBoolProperty( EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 0x080008 ) );
        if ( shape.isNoFill() )
        {
            // Wonderful... none of the spec's give any clue as to what these constants mean.
            opt.addEscherProperty( new EscherBoolProperty( EscherProperties.FILL__NOFILLHITTEST, 0x00110000 ) );
        }
        else
        {
            opt.addEscherProperty( new EscherBoolProperty( EscherProperties.FILL__NOFILLHITTEST, 0x00010000 ) );
        }
        opt.addEscherProperty( new EscherRGBProperty( EscherProperties.FILL__FILLCOLOR, shape.getFillColor() ) );
        opt.addEscherProperty( new EscherBoolProperty( EscherProperties.GROUPSHAPE__PRINT, 0x080000 ) );
        opt.addEscherProperty( new EscherRGBProperty( EscherProperties.LINESTYLE__COLOR, shape.getLineStyleColor() ) );
        int options = 5;
        if (shape.getLineWidth() != HSSFShape.LINEWIDTH_DEFAULT)
        {
            opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.LINESTYLE__LINEWIDTH, shape.getLineWidth()));
            options++;
        }
        if (shape.getLineStyle() != HSSFShape.LINESTYLE_SOLID)
        {
            opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.LINESTYLE__LINEDASHING, shape.getLineStyle()));
            opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.LINESTYLE__LINEENDCAPSTYLE, 0));
            if (shape.getLineStyle() == HSSFShape.LINESTYLE_NONE)
                opt.addEscherProperty( new EscherBoolProperty( EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x00080000));
            else
                opt.addEscherProperty( new EscherBoolProperty( EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x00080008));
            options += 3;
        }
        opt.sortProperties();
        return options;   // # options added
    }

}
