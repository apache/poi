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
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;

import java.util.List;

/**
 * Create a <code>Shape</code> object depending on its type
 *
 * @author Yegor Kozlov
 */
public class ShapeFactory {
    // For logging
    protected static POILogger logger = POILogFactory.getLogger(ShapeFactory.class);

    /**
     * Create a new shape from the data provided.  
     */
    public static Shape createShape(EscherContainerRecord spContainer, Shape parent){
        if (spContainer.getRecordId() == EscherContainerRecord.SPGR_CONTAINER){
            return createShapeGroup(spContainer, parent);
        } else {
            return createSimpeShape(spContainer, parent);

        }
    }

    public static ShapeGroup createShapeGroup(EscherContainerRecord spContainer, Shape parent){
        ShapeGroup group = null;
        UnknownEscherRecord opt = (UnknownEscherRecord)Shape.getEscherChild((EscherContainerRecord)spContainer.getChild(0), (short)0xF122);
        if(opt != null){
            try {
                EscherPropertyFactory f = new EscherPropertyFactory();
                List props = f.createProperties( opt.getData(), 0, opt.getInstance() );
                EscherSimpleProperty p = (EscherSimpleProperty)props.get(0);
                if(p.getPropertyNumber() == 0x39F && p.getPropertyValue() == 1){
                    group = new ShapeGroup(spContainer, parent);
                } else {
                    group = new ShapeGroup(spContainer, parent);
                }
            } catch (Exception e){
                logger.log(POILogger.WARN, e.getMessage());
                group = new ShapeGroup(spContainer, parent);
            }
        }  else {
            group = new ShapeGroup(spContainer, parent);
        }

        return group;
     }

    public static Shape createSimpeShape(EscherContainerRecord spContainer, Shape parent){
        Shape shape;
        EscherSpRecord spRecord = spContainer.getChildById(EscherSpRecord.RECORD_ID);

        int type = spRecord.getOptions() >> 4;
        switch (type){
            case ShapeTypes.TextBox:
                shape = new TextBox(spContainer, parent);
                break;
            case ShapeTypes.PictureFrame: {
                EscherOptRecord opt = (EscherOptRecord)Shape.getEscherChild(spContainer, EscherOptRecord.RECORD_ID);
                EscherProperty prop = Shape.getEscherProperty(opt, EscherProperties.BLIP__PICTUREID);
                if(prop != null)
                    shape = new OLEShape(spContainer, parent); //presence of BLIP__PICTUREID indicates it is an embedded object 
                else
                    shape = new Picture(spContainer, parent);
                break;
            }
            case ShapeTypes.Line:
                shape = new Line(spContainer, parent);
                break;
            case ShapeTypes.NotPrimitive: {
                EscherOptRecord opt = (EscherOptRecord)Shape.getEscherChild(spContainer, EscherOptRecord.RECORD_ID);
                EscherProperty prop = Shape.getEscherProperty(opt, EscherProperties.GEOMETRY__VERTICES);
                if(prop != null)
                    shape = new Freeform(spContainer, parent);
                else {

                    logger.log(POILogger.WARN, "Creating AutoShape for a NotPrimitive shape");
                    shape = new AutoShape(spContainer, parent);
                }
                break;
            }
            default:
                shape = new AutoShape(spContainer, parent);
                break;
        }
        return shape;

    }
}
