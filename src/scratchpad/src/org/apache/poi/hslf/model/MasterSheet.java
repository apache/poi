/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.poi.hslf.model;

import org.apache.poi.hslf.record.SheetContainer;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.model.textproperties.TextProp;

/**
 * The superclass of all master sheets - Slide masters, Notes masters, etc.
 *
 * For now it's empty. When we understand more about masters in ppt we will add the common functionality here.
 * 
 * @author Yegor Kozlov
 */
public abstract class MasterSheet extends Sheet {
    public MasterSheet(SheetContainer container, int sheetNo){
        super(container, sheetNo);
    }

    /**
     * Pickup a style attribute from the master.
     * This is the "workhorse" which returns the default style attrubutes.
     */
    public abstract TextProp getStyleAttribute(int txtype, int level, String name, boolean isCharacter) ;


    /**
     * Checks if the shape is a placeholder.
     * (placeholders aren't normal shapes, they are visible only in the Edit Master mode)
     *
     *
     * @return true if the shape is a placeholder
     */
    public static boolean isPlaceholder(Shape shape){
        if(!(shape instanceof TextShape)) return false;

        TextShape tx = (TextShape)shape;
        TextRun run = tx.getTextRun();
        if(run == null) return false;

        Record[] records = run._records;
        for (int i = 0; i < records.length; i++) {
            int type = (int)records[i].getRecordType();
            if (type == RecordTypes.BaseTextPropAtom.typeID ||
                type == RecordTypes.DateTimeMCAtom.typeID ||
                type == RecordTypes.GenericDateMCAtom.typeID ||
                type == RecordTypes.FooterMCAtom.typeID ||
                type == RecordTypes.SlideNumberMCAtom.typeID
                    ) return true;

        }
        return false;
    }

    /**
     * Return placeholder by text type
     */
    public TextShape getPlaceholder(int type){
        Shape[] shape = getShapes();
        for (int i = 0; i < shape.length; i++) {
            if(shape[i] instanceof TextShape){
                TextShape tx = (TextShape)shape[i];
                TextRun run = tx.getTextRun();
                if(run != null && run.getRunType() == type){
                    return tx;
                }
            }
        }
        return null;
    }
}
