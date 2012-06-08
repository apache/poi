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

import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.ddf.EscherSpgrRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.hssf.model.TextboxShape;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.TextObjectRecord;
import org.apache.poi.hssf.usermodel.drawing.HSSFShapeType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author evgeniy
 * date: 05.06.12
 */
public class HSSFShapeFactory {

    private static final Map<Short, Class> shapeTypeToClass = new HashMap<Short, Class>(HSSFShapeType.values().length);
    private static final ReflectionConstructorShapeCreator shapeCreator = new ReflectionConstructorShapeCreator(shapeTypeToClass);

    static {
        for (HSSFShapeType type: HSSFShapeType.values()){
            shapeTypeToClass.put(type.getType(), type.getShape());
        }
    }

    private static class ReflectionConstructorShapeCreator {

        private final Map<Short, Class> shapeTypeToClass;

        private ReflectionConstructorShapeCreator(Map<Short, Class> shapeTypeToClass) {
            this.shapeTypeToClass = shapeTypeToClass;
        }

        public HSSFShape createNewShape(Short type, EscherContainerRecord spContainer, ObjRecord objRecord){
            if (!shapeTypeToClass.containsKey(type)){
                return new HSSFUnknownShape(spContainer, objRecord);
            }
            Class clazz = shapeTypeToClass.get(type);
            if (null == clazz){
                System.out.println("No class attached to shape type: "+type);
                return new HSSFUnknownShape(spContainer, objRecord);
            }
            try{
                Constructor constructor = clazz.getConstructor(new Class[]{EscherContainerRecord.class, ObjRecord.class});
                return (HSSFShape) constructor.newInstance(spContainer, objRecord);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(clazz.getName() +" doesn't have required for shapes constructor");
            } catch (Exception e) {
                throw new IllegalStateException("Couldn't create new instance of " + clazz.getName());
            }
        }
    }

    public static HSSFShape createShape(EscherRecord container, ObjRecord objRecord){
        if (0 == container.getChildRecords().size()){
            throw new IllegalArgumentException("Couldn't create shape from empty escher container");
        }
        if (container.getChild(0) instanceof EscherSpgrRecord){
            return new HSSFShapeGroup((EscherContainerRecord) container, objRecord);
        }

        //TODO implement cases for all shapes
        return new HSSFUnknownShape(container, objRecord);
    }

    public static HSSFShapeGroup createShapeGroup(){
        return null;
    }

    public static HSSFShapeGroup createSimpleShape(EscherRecord container, ObjRecord objRecord){
        return null;
    }

    public static void createShapeTree(EscherContainerRecord container, EscherAggregate agg, HSSFShapeContainer out){
        if(container.getRecordId() == EscherContainerRecord.SPGR_CONTAINER){
            HSSFShapeGroup group = new HSSFShapeGroup(container,
                    null /* shape containers don't have a associated Obj record*/);
            List<EscherContainerRecord> children = container.getChildContainers();
            // skip the first child record, it is group descriptor
            for(int i = 0; i < children.size(); i++) {
                EscherContainerRecord spContainer = children.get(i);
                if(i == 0){
                    EscherSpgrRecord spgr = (EscherSpgrRecord)spContainer.getChildById(EscherSpgrRecord.RECORD_ID);
                    group.setCoordinates(
                            spgr.getRectX1(), spgr.getRectY1(),
                            spgr.getRectX2(), spgr.getRectY2()
                    );
                } else {
                    createShapeTree(spContainer, agg, group);
                }
            }
            out.addShape(group);
        } else if (container.getRecordId() == EscherContainerRecord.SP_CONTAINER){
            Map<EscherRecord, Record> shapeToObj = agg.getShapeToObjMapping();
            EscherSpRecord spRecord = null;
            ObjRecord objRecord = null;
            TextObjectRecord txtRecord = null;

            for(EscherRecord record : container.getChildRecords()) {
                switch(record.getRecordId()) {
                    case EscherSpRecord.RECORD_ID:
                        spRecord = (EscherSpRecord)record;
                        break;
                    case EscherClientDataRecord.RECORD_ID:
                        objRecord = (ObjRecord)shapeToObj.get(record);
                        break;
                    case EscherTextboxRecord.RECORD_ID:
                        txtRecord = (TextObjectRecord)shapeToObj.get(record);
                        break;
                }
            }
            if (null != objRecord){
                HSSFShape shape = shapeCreator.createNewShape(spRecord.getShapeType(), container, objRecord);
                out.addShape(shape);
            }
            if (null != txtRecord){
                //TODO resolve textbox
//                TextboxShape shape = new TextboxShape(container, txtRecord);
//                out.a
            }
//
//            //TODO decide what shape to create based on ObjRecord / EscherSpRecord
//            HSSFShape shape = new HSSFUnknownShape(container, objRecord);
//            out.addShape(shape);
        }
    }
}
