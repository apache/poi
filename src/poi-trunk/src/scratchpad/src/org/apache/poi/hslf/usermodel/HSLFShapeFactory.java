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

import java.util.List;

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherProperty;
import org.apache.poi.ddf.EscherPropertyFactory;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.hslf.model.MovieShape;
import org.apache.poi.hslf.record.ExObjRefAtom;
import org.apache.poi.hslf.record.HSLFEscherClientDataRecord;
import org.apache.poi.hslf.record.InteractiveInfo;
import org.apache.poi.hslf.record.InteractiveInfoAtom;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordTypes;
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
        EscherRecord opt = HSLFShape.getEscherChild(ecr, RecordTypes.EscherUserDefined);

        if (opt != null) {
            EscherPropertyFactory f = new EscherPropertyFactory();
            List<EscherProperty> props = f.createProperties( opt.serialize(), 8, opt.getInstance() );
            for (EscherProperty ep : props) {
                if (ep.getPropertyNumber() == EscherProperties.GROUPSHAPE__TABLEPROPERTIES
                    && ep instanceof EscherSimpleProperty
                    && (((EscherSimpleProperty)ep).getPropertyValue() & 1) == 1) {
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
                if (parent instanceof HSLFTable) {
                    EscherTextboxRecord etr = spContainer.getChildById(EscherTextboxRecord.RECORD_ID);
                    if (etr == null) {
                        logger.log(POILogger.WARN, "invalid ppt - add EscherTextboxRecord to cell");
                        etr = new EscherTextboxRecord();
                        etr.setRecordId(EscherTextboxRecord.RECORD_ID);
                        etr.setOptions((short)15);
                        spContainer.addChildRecord(etr);
                    }
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
                    return new HSLFObjectShape(spContainer, parent);
                case InteractiveInfoAtom.ACTION_MEDIA:
                    return new MovieShape(spContainer, parent);
                default:
                    break;
            }
        }
        
        ExObjRefAtom oes = getClientDataRecord(spContainer, RecordTypes.ExObjRefAtom.typeID);
        return (oes != null)
            ? new HSLFObjectShape(spContainer, parent)
            : new HSLFPictureShape(spContainer, parent);
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
        HSLFEscherClientDataRecord cldata = spContainer.getChildById(EscherClientDataRecord.RECORD_ID);
        if (cldata != null) for (Record r : cldata.getHSLFChildRecords()) {
            if (r.getRecordType() == recordType) {
                return (T)r;
            }
        }
        return null;
    }

}
