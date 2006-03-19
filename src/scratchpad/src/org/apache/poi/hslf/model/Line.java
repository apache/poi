/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hslf.model;

import org.apache.poi.ddf.*;

import java.awt.*;

/**
 * Represents a line in a PowerPoint drawing
 *
 *  @author Yegor Kozlov
 */
public class Line extends SimpleShape {
    /**
    * Solid (continuous) pen
    */
    public static final int LineSolid = 1;
    /**
     *  PS_DASH system   dash style
     */
    public static final int LineDashSys = 2;
    /**
     *  PS_DOT system   dash style
     */
    public static final int LineDotSys = 3;
    /**
     * PS_DASHDOT system dash style
     */
    public static final int LineDashDotSys = 4;

    /**
     * PS_DASHDOTDOT system dash style
     */
    public static final int LineDashDotDotSys = 5;
    /**
     *  square dot style
     */
    public static final int LineDotGEL = 6;
    /**
     *  dash style
     */
    public static final int LineDashGEL = 7;
    /**
     *  long dash style
     */
    public static final int LineLongDashGEL = 8;
    /**
     * dash short dash
     */
    public static final int LineDashDotGEL = 9;
    /**
     * long dash short dash
     */
    public static final int LineLongDashDotGEL = 10;
    /**
     * long dash short dash short dash
     */
    public static final int LineLongDashDotDotGEL = 11;

    /**
     * Decoration of the end of line,
     * reserved in API but not supported.
     */

    /**
     *  Line ends at end point
     */
    public static final int EndCapFlat = 0;
    /**
     *  Rounded ends - the default
     */
    public static final int EndCapRound = 1;
    /**
     * Square protrudes by half line width
     */
    public static final int EndCapSquare = 2;

    protected Line(EscherContainerRecord escherRecord, Shape parent){
        super(escherRecord, parent);
    }

    public Line(Shape parent){
        super(null, parent);
        _escherContainer = create(parent instanceof ShapeGroup);
    }

    public Line(){
        this(null);
    }

    protected EscherContainerRecord create(boolean isChild){
        EscherContainerRecord spcont = super.create(isChild);
        spcont.setOptions((short)15);

        EscherSpRecord spRecord = spcont.getChildById(EscherSpRecord.RECORD_ID);
        short type = (ShapeTypes.Line << 4) + 2;
        spRecord.setOptions(type);
  
        //set default properties for a line
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(spcont, EscherOptRecord.RECORD_ID);

        //opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GEOMETRY__SHAPEPATH, 4));
        opt.sortProperties();

        return spcont;
    }

}
