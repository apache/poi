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

package org.apache.poi.hslf.model;

import org.apache.poi.ddf.*;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;

/**
 * Represents a line in a PowerPoint drawing
 *
 *  @author Yegor Kozlov
 */
public final class Line extends SimpleShape {
    /**
    * Solid (continuous) pen
    */
    public static final int PEN_SOLID = 1;
    /**
     *  PS_DASH system   dash style
     */
    public static final int PEN_PS_DASH = 2;
    /**
     *  PS_DOT system   dash style
     */
    public static final int PEN_DOT = 3;
    /**
     * PS_DASHDOT system dash style
     */
    public static final int PEN_DASHDOT = 4;
    /**
     * PS_DASHDOTDOT system dash style
     */
    public static final int PEN_DASHDOTDOT = 5;
    /**
     *  square dot style
     */
    public static final int PEN_DOTGEL = 6;
    /**
     *  dash style
     */
    public static final int PEN_DASH = 7;
    /**
     *  long dash style
     */
    public static final int PEN_LONGDASHGEL = 8;
    /**
     * dash short dash
     */
    public static final int PEN_DASHDOTGEL = 9;
    /**
     * long dash short dash
     */
    public static final int PEN_LONGDASHDOTGEL = 10;
    /**
     * long dash short dash short dash
     */
    public static final int PEN_LONGDASHDOTDOTGEL = 11;

    /**
     *  Single line (of width lineWidth)
     */
    public static final int LINE_SIMPLE = 0;
    /**
     * Double lines of equal width
     */
    public static final int LINE_DOUBLE = 1;
    /**
     * Double lines, one thick, one thin
     */
    public static final int LINE_THICKTHIN = 2;
    /**
     *  Double lines, reverse order
     */
    public static final int LINE_THINTHICK = 3;
    /**
     * Three lines, thin, thick, thin
     */
    public static final int LINE_TRIPLE = 4;


    protected Line(EscherContainerRecord escherRecord, Shape parent){
        super(escherRecord, parent);
    }

    public Line(Shape parent){
        super(null, parent);
        _escherContainer = createSpContainer(parent instanceof ShapeGroup);
    }

    public Line(){
        this(null);
    }

    protected EscherContainerRecord createSpContainer(boolean isChild){
        _escherContainer = super.createSpContainer(isChild);

        EscherSpRecord spRecord = _escherContainer.getChildById(EscherSpRecord.RECORD_ID);
        short type = (ShapeTypes.Line << 4) | 0x2;
        spRecord.setOptions(type);

        //set default properties for a line
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);

        //default line properties
        setEscherProperty(opt, EscherProperties.GEOMETRY__SHAPEPATH, 4);
        setEscherProperty(opt, EscherProperties.GEOMETRY__FILLOK, 0x10000);
        setEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST, 0x100000);
        setEscherProperty(opt, EscherProperties.LINESTYLE__COLOR, 0x8000001);
        setEscherProperty(opt, EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0xA0008);
        setEscherProperty(opt, EscherProperties.SHADOWSTYLE__COLOR, 0x8000002);

        return _escherContainer;
    }

    public java.awt.Shape getOutline(){
        Rectangle2D anchor = getLogicalAnchor2D();
        return new Line2D.Double(anchor.getX(), anchor.getY(), anchor.getX() + anchor.getWidth(), anchor.getY() + anchor.getHeight());
    }
}
