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
                if (ep.getPropertyNumber() == 0x39F
                    && ep instanceof EscherSimpleProperty
                    && ((EscherSimpleProperty)ep).getPropertyValue() == 1) {
                    isTable = true;
                    break;
                }
            }
        }
        
        HSLFGroupShape group = (isTable)
            ? new HSLFTable(spContainer, parent)
            : new HSLFGroupShape(spContainer, parent);
        
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
            case FRAME: {
                InteractiveInfo info = getClientDataRecord(spContainer, RecordTypes.InteractiveInfo.typeID);
                OEShapeAtom oes = getClientDataRecord(spContainer, RecordTypes.OEShapeAtom.typeID);
                if(info != null && info.getInteractiveInfoAtom() != null){
                    switch(info.getInteractiveInfoAtom().getAction()){
                        case InteractiveInfoAtom.ACTION_OLE:
                            shape = new OLEShape(spContainer, parent);
                            break;
                        case InteractiveInfoAtom.ACTION_MEDIA:
                            shape = new MovieShape(spContainer, parent);
                            break;
                        default:
                            break;
                    }
                } else if (oes != null){
                    shape = new OLEShape(spContainer, parent);
                }

                if(shape == null) shape = new HSLFPictureShape(spContainer, parent);
                break;
            }
            case LINE:
                shape = new HSLFLine(spContainer, parent);
                break;
            case NOT_PRIMITIVE: {
                AbstractEscherOptRecord opt = HSLFShape.getEscherChild(spContainer, EscherOptRecord.RECORD_ID);
                EscherProperty prop = HSLFShape.getEscherProperty(opt, EscherProperties.GEOMETRY__VERTICES);
                if(prop != null)
                    shape = new HSLFFreeformShape(spContainer, parent);
                else {
                    logger.log(POILogger.INFO, "Creating AutoShape for a NotPrimitive shape");
                    shape = new HSLFAutoShape(spContainer, parent);
                }
                break;
            }
            default:
                shape = new HSLFAutoShape(spContainer, parent);
                break;
        }
        return shape;

    }

    @SuppressWarnings("unchecked")
    protected static <T extends Record> T getClientDataRecord(EscherContainerRecord spContainer, int recordType) {
        Record oep = null;
        for (Iterator<EscherRecord> it = spContainer.getChildIterator(); it.hasNext();) {
            EscherRecord obj = it.next();
            if (obj.getRecordId() == EscherClientDataRecord.RECORD_ID) {
                byte[] data = obj.serialize();
                Record[] records = Record.findChildRecords(data, 8, data.length - 8);
                for (int j = 0; j < records.length; j++) {
                    if (records[j].getRecordType() == recordType) {
                        return (T)records[j];
                    }
                }
            }
        }
        return (T)oep;
    }

}
