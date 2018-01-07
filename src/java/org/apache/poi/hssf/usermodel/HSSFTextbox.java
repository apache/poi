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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.ddf.*;
import org.apache.poi.hssf.record.*;

/**
 * A textbox is a shape that may hold a rich text string.
 */
public class HSSFTextbox extends HSSFSimpleShape {
    public final static short OBJECT_TYPE_TEXT = 6;

    /**
     * How to align text horizontally
     */
    public final static short HORIZONTAL_ALIGNMENT_LEFT = 1;
    public final static short HORIZONTAL_ALIGNMENT_CENTERED = 2;
    public final static short HORIZONTAL_ALIGNMENT_RIGHT = 3;
    public final static short HORIZONTAL_ALIGNMENT_JUSTIFIED = 4;
    public final static short HORIZONTAL_ALIGNMENT_DISTRIBUTED = 7;

    /**
     * How to align text vertically
     */
    public final static short VERTICAL_ALIGNMENT_TOP = 1;
    public final static short VERTICAL_ALIGNMENT_CENTER = 2;
    public final static short VERTICAL_ALIGNMENT_BOTTOM = 3;
    public final static short VERTICAL_ALIGNMENT_JUSTIFY = 4;
    public final static short VERTICAL_ALIGNMENT_DISTRIBUTED = 7;

    public HSSFTextbox(EscherContainerRecord spContainer, ObjRecord objRecord, TextObjectRecord textObjectRecord) {
        super(spContainer, objRecord, textObjectRecord);
    }

    // Findbugs: URF_UNREAD_FIELD. Do not delete without understanding how this class works.
    //HSSFRichTextString string = new HSSFRichTextString("");

    /**
     * Construct a new textbox with the given parent and anchor.
     *
     * @param parent
     * @param anchor One of HSSFClientAnchor or HSSFChildAnchor
     */
    public HSSFTextbox(HSSFShape parent, HSSFAnchor anchor) {
        super(parent, anchor);
        setHorizontalAlignment(HORIZONTAL_ALIGNMENT_LEFT);
        setVerticalAlignment(VERTICAL_ALIGNMENT_TOP);
        setString(new HSSFRichTextString(""));
    }

    @Override
    protected ObjRecord createObjRecord() {
        ObjRecord obj = new ObjRecord();
        CommonObjectDataSubRecord c = new CommonObjectDataSubRecord();
        c.setObjectType(HSSFTextbox.OBJECT_TYPE_TEXT);
        c.setLocked(true);
        c.setPrintable(true);
        c.setAutofill(true);
        c.setAutoline(true);
        EndSubRecord e = new EndSubRecord();
        obj.addSubRecord(c);
        obj.addSubRecord(e);
        return obj;
    }

    @Override
    protected EscherContainerRecord createSpContainer() {
        EscherContainerRecord spContainer = new EscherContainerRecord();
        EscherSpRecord sp = new EscherSpRecord();
        EscherOptRecord opt = new EscherOptRecord();
        EscherClientDataRecord clientData = new EscherClientDataRecord();
        EscherTextboxRecord escherTextbox = new EscherTextboxRecord();

        spContainer.setRecordId(EscherContainerRecord.SP_CONTAINER);
        spContainer.setOptions((short) 0x000F);
        sp.setRecordId(EscherSpRecord.RECORD_ID);
        sp.setOptions((short) ((EscherAggregate.ST_TEXTBOX << 4) | 0x2));

        sp.setFlags(EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_HASSHAPETYPE);
        opt.setRecordId(EscherOptRecord.RECORD_ID);
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.TEXT__TEXTID, 0));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.TEXT__WRAPTEXT, 0));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.TEXT__ANCHORTEXT, 0));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GROUPSHAPE__PRINT, 0x00080000));

        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.TEXT__TEXTLEFT, 0));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.TEXT__TEXTRIGHT, 0));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.TEXT__TEXTTOP, 0));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.TEXT__TEXTBOTTOM, 0));

        opt.setEscherProperty(new EscherSimpleProperty(EscherProperties.LINESTYLE__LINEDASHING, LINESTYLE_SOLID));
        opt.setEscherProperty(new EscherBoolProperty(EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x00080008));
        opt.setEscherProperty(new EscherSimpleProperty(EscherProperties.LINESTYLE__LINEWIDTH, LINEWIDTH_DEFAULT));
        opt.setEscherProperty(new EscherRGBProperty(EscherProperties.FILL__FILLCOLOR, FILL__FILLCOLOR_DEFAULT));
        opt.setEscherProperty(new EscherRGBProperty(EscherProperties.LINESTYLE__COLOR, LINESTYLE__COLOR_DEFAULT));
        opt.setEscherProperty(new EscherBoolProperty(EscherProperties.FILL__NOFILLHITTEST, NO_FILLHITTEST_FALSE));
        opt.setEscherProperty(new EscherBoolProperty(EscherProperties.GROUPSHAPE__PRINT, 0x080000));

        EscherRecord anchor = getAnchor().getEscherAnchor();
        clientData.setRecordId(EscherClientDataRecord.RECORD_ID);
        clientData.setOptions((short) 0x0000);
        escherTextbox.setRecordId(EscherTextboxRecord.RECORD_ID);
        escherTextbox.setOptions((short) 0x0000);

        spContainer.addChildRecord(sp);
        spContainer.addChildRecord(opt);
        spContainer.addChildRecord(anchor);
        spContainer.addChildRecord(clientData);
        spContainer.addChildRecord(escherTextbox);

        return spContainer;
    }

    @Override
    void afterInsert(HSSFPatriarch patriarch) {
        EscherAggregate agg = patriarch.getBoundAggregate();
        agg.associateShapeToObjRecord(getEscherContainer().getChildById(EscherClientDataRecord.RECORD_ID), getObjRecord());
        if (getTextObjectRecord() != null){
            agg.associateShapeToObjRecord(getEscherContainer().getChildById(EscherTextboxRecord.RECORD_ID), getTextObjectRecord());
        }
    }

    /**
     * @return Returns the left margin within the textbox.
     */
    public int getMarginLeft() {
        EscherSimpleProperty property = getOptRecord().lookup(EscherProperties.TEXT__TEXTLEFT);
        return property == null ? 0 : property.getPropertyValue();
    }

    /**
     * Sets the left margin within the textbox.
     */
    public void setMarginLeft(int marginLeft) {
        setPropertyValue(new EscherSimpleProperty(EscherProperties.TEXT__TEXTLEFT, marginLeft));
    }

    /**
     * @return returns the right margin within the textbox.
     */
    public int getMarginRight() {
        EscherSimpleProperty property = getOptRecord().lookup(EscherProperties.TEXT__TEXTRIGHT);
        return property == null ? 0 : property.getPropertyValue();
    }

    /**
     * Sets the right margin within the textbox.
     */
    public void setMarginRight(int marginRight) {
        setPropertyValue(new EscherSimpleProperty(EscherProperties.TEXT__TEXTRIGHT, marginRight));
    }

    /**
     * @return returns the top margin within the textbox.
     */
    public int getMarginTop() {
        EscherSimpleProperty property = getOptRecord().lookup(EscherProperties.TEXT__TEXTTOP);
        return property == null ? 0 : property.getPropertyValue();
    }

    /**
     * Sets the top margin within the textbox.
     */
    public void setMarginTop(int marginTop) {
        setPropertyValue(new EscherSimpleProperty(EscherProperties.TEXT__TEXTTOP, marginTop));
    }

    /**
     * Gets the bottom margin within the textbox.
     */
    public int getMarginBottom() {
        EscherSimpleProperty property = getOptRecord().lookup(EscherProperties.TEXT__TEXTBOTTOM);
        return property == null ? 0 : property.getPropertyValue();
    }

    /**
     * Sets the bottom margin within the textbox.
     */
    public void setMarginBottom(int marginBottom) {
        setPropertyValue(new EscherSimpleProperty(EscherProperties.TEXT__TEXTBOTTOM, marginBottom));
    }

    /**
     * Gets the horizontal alignment.
     */
    public short getHorizontalAlignment() {
        return (short) getTextObjectRecord().getHorizontalTextAlignment();
    }

    /**
     * Sets the horizontal alignment.
     */
    public void setHorizontalAlignment(short align) {
        getTextObjectRecord().setHorizontalTextAlignment(align);
    }

    /**
     * Gets the vertical alignment.
     */
    public short getVerticalAlignment() {
        return (short) getTextObjectRecord().getVerticalTextAlignment();
    }

    /**
     * Sets the vertical alignment.
     */
    public void setVerticalAlignment(short align) {
        getTextObjectRecord().setVerticalTextAlignment(align);
    }

    @Override
    public void setShapeType(int shapeType) {
        throw new IllegalStateException("Shape type can not be changed in " + this.getClass().getSimpleName());
    }

    @Override
    protected HSSFShape cloneShape() {
        TextObjectRecord txo = getTextObjectRecord() == null ? null : (TextObjectRecord) getTextObjectRecord().cloneViaReserialise();
        EscherContainerRecord spContainer = new EscherContainerRecord();
        byte[] inSp = getEscherContainer().serialize();
        spContainer.fillFields(inSp, 0, new DefaultEscherRecordFactory());
        ObjRecord obj = (ObjRecord) getObjRecord().cloneViaReserialise();
        return new HSSFTextbox(spContainer, obj, txo);
    }

    @Override
    protected void afterRemove(HSSFPatriarch patriarch) {
        patriarch.getBoundAggregate().removeShapeToObjRecord(getEscherContainer().getChildById(EscherClientDataRecord.RECORD_ID));
        patriarch.getBoundAggregate().removeShapeToObjRecord(getEscherContainer().getChildById(EscherTextboxRecord.RECORD_ID));
    }
}
