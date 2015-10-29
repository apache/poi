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

package org.apache.poi.hslf.usermodel;

import java.util.Iterator;
import java.util.List;

import org.apache.poi.ddf.*;
import org.apache.poi.hslf.model.*;
import org.apache.poi.hslf.record.*;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Create a <code>Shape</code> object depending on its type
 *
 * @author Yegor Kozlov
 */
public final class HSLFShapeFactory {
    // For logging
    protected static final POILogger logger = POILogFactory.getLogger(HSLFShapeFactory.class);

    /**
     * Create a new shape from the data provided.
     */
    public static HSLFShape createShape(EscherContainerRecord spContainer, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        if (spContainer.getRecordId() == EscherContainerRecord.SPGR_CONTAINER){
            return createShapeGroup(spContainer, parent);
        }
        return createSimpleShape(spContainer, parent);
    }

    public static HSLFGroupShape createShapeGroup(EscherContainerRecord spContainer, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        boolean isTable = false;
        EscherContainerRecord ecr = (EscherContainerRecord)spContainer.getChild(0);
        EscherRecord opt = HSLFShape.getEscherChild(ecr, (short)RecordTypes.EscherUserDefined);

        if (opt != null) {
            EscherPropertyFactory f = new EscherPropertyFactory();
            List<EscherProperty> props = f.createProperties( opt.serialize(), 8, opt.getInstance() );
            for (EscherProperty ep : props) {
                if (ep.getPropertyNumber() == EscherProperties.GROUPSHAPE__TABLEPROPERTIES
                    && ep instanceof EscherSimpleProperty
                    && ((EscherSimpleProperty)ep).getPropertyValue() == 1) {
                    isTable = true;
                    break;
                }
            }
        }
        
        HSLFGroupShape group;
        if (isTable) {
            group = new HSLFTable(spContainer, parent);
            
        } else {
            group = new HSLFGroupShape(spContainer, parent);
        }
        
        return group;
     }

    public static HSLFShape createSimpleShape(EscherContainerRecord spContainer, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        HSLFShape shape = null;
        EscherSpRecord spRecord = spContainer.getChildById(EscherSpRecord.RECORD_ID);

        ShapeType type = ShapeType.forId(spRecord.getShapeType(), false);
        switch (type){
            case TEXT_BOX:
                shape = new HSLFTextBox(spContainer, parent);
                break;
            case HOST_CONTROL:
            case FRAME:
                shape = createFrame(spContainer, parent);
                break;
            case LINE:
                shape = new HSLFLine(spContainer, parent);
                break;
            case NOT_PRIMITIVE:
                shape = createNonPrimitive(spContainer, parent);
                break;
            default:
                EscherTextboxRecord etr = spContainer.getChildById(EscherTextboxRecord.RECORD_ID);
                if (parent instanceof HSLFTable && etr != null) {
                    shape = new HSLFTableCell(spContainer, (HSLFTable)parent);
                } else {
                    shape = new HSLFAutoShape(spContainer, parent);
                }
                break;
        }
        return shape;
    }

    private static HSLFShape createFrame(EscherContainerRecord spContainer, ShapeContainer<HSLFShape,HSLFTextParagraph> parent) {
        InteractiveInfo info = getClientDataRecord(spContainer, RecordTypes.InteractiveInfo.typeID);
        if(info != null && info.getInteractiveInfoAtom() != null){
            switch(info.getInteractiveInfoAtom().getAction()){
                case InteractiveInfoAtom.ACTION_OLE:
                    return new OLEShape(spContainer, parent);
                case InteractiveInfoAtom.ACTION_MEDIA:
                    return new MovieShape(spContainer, parent);
                default:
                    break;
            }
        }
        
        OEShapeAtom oes = getClientDataRecord(spContainer, RecordTypes.OEShapeAtom.typeID);
        if (oes != null){
            return new OLEShape(spContainer, parent);
        }

        return new HSLFPictureShape(spContainer, parent);
    }
    
    private static HSLFShape createNonPrimitive(EscherContainerRecord spContainer, ShapeContainer<HSLFShape,HSLFTextParagraph> parent) {
        AbstractEscherOptRecord opt = HSLFShape.getEscherChild(spContainer, EscherOptRecord.RECORD_ID);
        EscherProperty prop = HSLFShape.getEscherProperty(opt, EscherProperties.GEOMETRY__VERTICES);
        if(prop != null) {
            return new HSLFFreeformShape(spContainer, parent);
        }
        
        logger.log(POILogger.INFO, "Creating AutoShape for a NotPrimitive shape");
        return new HSLFAutoShape(spContainer, parent);
    }
    
    @SuppressWarnings("unchecked")
    protected static <T extends Record> T getClientDataRecord(EscherContainerRecord spContainer, int recordType) {
        for (Iterator<EscherRecord> it = spContainer.getChildIterator(); it.hasNext();) {
            EscherRecord obj = it.next();
            if (obj.getRecordId() == EscherClientDataRecord.RECORD_ID) {
                byte[] data = obj.serialize();
                for (Record r : Record.findChildRecords(data, 8, data.length - 8)) {
                    if (r.getRecordType() == recordType) {
                        return (T)r;
                    }
                }
            }
        }
        return null;
    }

}
