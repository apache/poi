/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.apache.poi.hssf.usermodel;

import java.util.List;
import java.util.Map;

import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherProperty;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.EmbeddedObjectRefSubRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SubRecord;
import org.apache.poi.hssf.record.TextObjectRecord;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.util.RecordFormatException;

/**
 * Factory class for producing Excel Shapes from Escher records
 */
public class HSSFShapeFactory {
    /**
     * build shape tree from escher container
     * @param container root escher container from which escher records must be taken
     * @param agg - EscherAggregate
     * @param out - shape container to which shapes must be added
     * @param root - node to create HSSFObjectData shapes
     */
    public static void createShapeTree(EscherContainerRecord container, EscherAggregate agg, HSSFShapeContainer out, DirectoryNode root) {
        if (container.getRecordId() == EscherContainerRecord.SPGR_CONTAINER) {
            ObjRecord obj = null;
            EscherClientDataRecord clientData = ((EscherContainerRecord) container.getChild(0)).getChildById(EscherClientDataRecord.RECORD_ID);
            if (null != clientData) {
                obj = (ObjRecord) agg.getShapeToObjMapping().get(clientData);
            }
            HSSFShapeGroup group = new HSSFShapeGroup(container, obj);
            List<EscherContainerRecord> children = container.getChildContainers();
            // skip the first child record, it is group descriptor
            for (int i = 0; i < children.size(); i++) {
                EscherContainerRecord spContainer = children.get(i);
                if (i != 0) {
                    createShapeTree(spContainer, agg, group, root);
                }
            }
            out.addShape(group);
        } else if (container.getRecordId() == EscherContainerRecord.SP_CONTAINER) {
            Map<EscherRecord, Record> shapeToObj = agg.getShapeToObjMapping();
            ObjRecord objRecord = null;
            TextObjectRecord txtRecord = null;

            for (EscherRecord record : container) {
                switch (record.getRecordId()) {
                    case EscherClientDataRecord.RECORD_ID:
                        objRecord = (ObjRecord) shapeToObj.get(record);
                        break;
                    case EscherTextboxRecord.RECORD_ID:
                        txtRecord = (TextObjectRecord) shapeToObj.get(record);
                        break;
                    default:
                        break;
                }
            }
            if (objRecord == null) {
                throw new RecordFormatException("EscherClientDataRecord can't be found.");
            }
            if (isEmbeddedObject(objRecord)) {
                HSSFObjectData objectData = new HSSFObjectData(container, objRecord, root);
                out.addShape(objectData);
                return;
            }
            CommonObjectDataSubRecord cmo = (CommonObjectDataSubRecord) objRecord.getSubRecords().get(0);
            final HSSFShape shape;
            switch (cmo.getObjectType()) {
                case CommonObjectDataSubRecord.OBJECT_TYPE_PICTURE:
                    shape = new HSSFPicture(container, objRecord);
                    break;
                case CommonObjectDataSubRecord.OBJECT_TYPE_RECTANGLE:
                    shape = new HSSFSimpleShape(container, objRecord, txtRecord);
                    break;
                case CommonObjectDataSubRecord.OBJECT_TYPE_LINE:
                    shape = new HSSFSimpleShape(container, objRecord);
                    break;
                case CommonObjectDataSubRecord.OBJECT_TYPE_COMBO_BOX:
                    shape = new HSSFCombobox(container, objRecord);
                    break;
                case CommonObjectDataSubRecord.OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING:
                    EscherOptRecord optRecord = container.getChildById(EscherOptRecord.RECORD_ID);
                    if(optRecord == null) {
                    	shape = new HSSFSimpleShape(container, objRecord, txtRecord);
                    } else {
                        EscherProperty property = optRecord.lookup(EscherProperties.GEOMETRY__VERTICES);
                        if (null != property) {
                            shape = new HSSFPolygon(container, objRecord, txtRecord);
                        } else {
                            shape = new HSSFSimpleShape(container, objRecord, txtRecord);
                        }
                    }
                    break;
                case CommonObjectDataSubRecord.OBJECT_TYPE_TEXT:
                    shape = new HSSFTextbox(container, objRecord, txtRecord);
                    break;
                case CommonObjectDataSubRecord.OBJECT_TYPE_COMMENT:
                    shape = new HSSFComment(container, objRecord, txtRecord, agg.getNoteRecordByObj(objRecord));
                    break;
                default:
                    shape = new HSSFSimpleShape(container, objRecord, txtRecord);
            }
            out.addShape(shape);
        }
    }

    private static boolean isEmbeddedObject(ObjRecord obj) {
        for (SubRecord sub : obj.getSubRecords()) {
            if (sub instanceof EmbeddedObjectRefSubRecord) {
                return true;
            }
        }
        return false;
    }
}
