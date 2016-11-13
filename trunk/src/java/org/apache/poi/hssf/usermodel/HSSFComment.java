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

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.EndSubRecord;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.NoteStructureSubRecord;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.record.TextObjectRecord;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.util.CellAddress;

/**
 * Represents a cell comment - a sticky note associated with a cell.
 */
public class HSSFComment extends HSSFTextbox implements Comment {

    private final static int FILL_TYPE_SOLID = 0;
    private final static int FILL_TYPE_PICTURE = 3;

    private final static int GROUP_SHAPE_PROPERTY_DEFAULT_VALUE = 655362;
    private final static int GROUP_SHAPE_HIDDEN_MASK = 0x1000002;
    private final static int GROUP_SHAPE_NOT_HIDDEN_MASK = 0xFEFFFFFD;

    /*
      * TODO - make HSSFComment more consistent when created vs read from file.
      * Currently HSSFComment has two main forms (corresponding to the two constructors).   There
      * are certain operations that only work on comment objects in one of the forms (e.g. deleting
      * comments).
      * POI is also deficient in its management of RowRecord fields firstCol and lastCol.  Those
      * fields are supposed to take comments into account, but POI does not do this yet (feb 2009).
      * It seems like HSSFRow should manage a collection of local HSSFComments
      */

    private final NoteRecord _note;

    public HSSFComment(EscherContainerRecord spContainer, ObjRecord objRecord, TextObjectRecord textObjectRecord, NoteRecord note) {
        super(spContainer, objRecord, textObjectRecord);
        _note = note;
    }

    /**
     * Construct a new comment with the given parent and anchor.
     *
     * @param parent
     * @param anchor defines position of this anchor in the sheet
     */
    public HSSFComment(HSSFShape parent, HSSFAnchor anchor) {
        this(parent, anchor, createNoteRecord());
    }

    private HSSFComment(HSSFShape parent, HSSFAnchor anchor, NoteRecord note) {
        super(parent, anchor);
        _note = note;
        //default color for comments
        setFillColor(0x08000050);

        //by default comments are hidden
        setVisible(false);
        setAuthor("");
        CommonObjectDataSubRecord cod = (CommonObjectDataSubRecord) getObjRecord().getSubRecords().get(0);
        cod.setObjectType(CommonObjectDataSubRecord.OBJECT_TYPE_COMMENT);
    }

    protected HSSFComment(NoteRecord note, TextObjectRecord txo) {
        this(null, new HSSFClientAnchor(), note);
    }

    @Override
    void afterInsert(HSSFPatriarch patriarch) {
        super.afterInsert(patriarch);
        patriarch.getBoundAggregate().addTailRecord(getNoteRecord());
    }

    @Override
    protected EscherContainerRecord createSpContainer() {
        EscherContainerRecord spContainer = super.createSpContainer();
        EscherOptRecord opt = spContainer.getChildById(EscherOptRecord.RECORD_ID);
        opt.removeEscherProperty(EscherProperties.TEXT__TEXTLEFT);
        opt.removeEscherProperty(EscherProperties.TEXT__TEXTRIGHT);
        opt.removeEscherProperty(EscherProperties.TEXT__TEXTTOP);
        opt.removeEscherProperty(EscherProperties.TEXT__TEXTBOTTOM);
        opt.setEscherProperty(new EscherSimpleProperty(EscherProperties.GROUPSHAPE__PRINT, false, false, GROUP_SHAPE_PROPERTY_DEFAULT_VALUE));
        return spContainer;
    }

    @Override
    protected ObjRecord createObjRecord() {
        ObjRecord obj = new ObjRecord();
        CommonObjectDataSubRecord c = new CommonObjectDataSubRecord();
        c.setObjectType(OBJECT_TYPE_COMMENT);
        c.setLocked(true);
        c.setPrintable(true);
        c.setAutofill(false);
        c.setAutoline(true);

        NoteStructureSubRecord u = new NoteStructureSubRecord();
        EndSubRecord e = new EndSubRecord();
        obj.addSubRecord(c);
        obj.addSubRecord(u);
        obj.addSubRecord(e);
        return obj;
    }

    private static NoteRecord createNoteRecord() {
        NoteRecord note = new NoteRecord();
        note.setFlags(NoteRecord.NOTE_HIDDEN);
        note.setAuthor("");
        return note;
    }

    @Override
    void setShapeId(int shapeId) {
        if(shapeId > 65535) {
            throw new IllegalArgumentException("Cannot add more than " + 65535 + " shapes");
        }
        super.setShapeId(shapeId);
        CommonObjectDataSubRecord cod = (CommonObjectDataSubRecord) getObjRecord().getSubRecords().get(0);
        cod.setObjectId(shapeId);
        _note.setShapeId(shapeId);
    }

    /**
     * Sets whether this comment is visible.
     *
     * @param visible <code>true</code> if the comment is visible, <code>false</code> otherwise
     */
    @Override
    public void setVisible(boolean visible) {
        _note.setFlags(visible ? NoteRecord.NOTE_VISIBLE : NoteRecord.NOTE_HIDDEN);
        setHidden(!visible);
    }

    /**
     * Returns whether this comment is visible.
     *
     * @return <code>true</code> if the comment is visible, <code>false</code> otherwise
     */
    @Override
    public boolean isVisible() {
        return _note.getFlags() == NoteRecord.NOTE_VISIBLE;
    }
    
    @Override
    public CellAddress getAddress() {
        return new CellAddress(getRow(), getColumn());
    }
    
    @Override
    public void setAddress(CellAddress address) {
        setRow(address.getRow());
        setColumn(address.getColumn());
    }
    
    @Override
    public void setAddress(int row, int col) {
        setRow(row);
        setColumn(col);
    }

    /**
     * Return the row of the cell that contains the comment
     *
     * @return the 0-based row of the cell that contains the comment
     */
    @Override
    public int getRow() {
        return _note.getRow();
    }

    /**
     * Set the row of the cell that contains the comment
     *
     * @param row the 0-based row of the cell that contains the comment
     */
    @Override
    public void setRow(int row) {
        _note.setRow(row);
    }

    /**
     * Return the column of the cell that contains the comment
     *
     * @return the 0-based column of the cell that contains the comment
     */
    @Override
    public int getColumn() {
        return _note.getColumn();
    }

    /**
     * Set the column of the cell that contains the comment
     *
     * @param col the 0-based column of the cell that contains the comment
     */
    @Override
    public void setColumn(int col) {
        _note.setColumn(col);
    }

    /**
     * Name of the original comment author
     *
     * @return the name of the original author of the comment
     */
    @Override
    public String getAuthor() {
        return _note.getAuthor();
    }

    /**
     * Name of the original comment author
     *
     * @param author the name of the original author of the comment
     */
    @Override
    public void setAuthor(String author) {
        if (_note != null) _note.setAuthor(author);
    }

    /**
     * Returns the underlying Note record
     */
    protected NoteRecord getNoteRecord() {
        return _note;
    }
    
    /**
     * Do we know which cell this comment belongs to?
     */
    public boolean hasPosition() {
        if (_note == null) return false;
        if (getColumn() < 0 || getRow() < 0) return false;
        return true;
    }

    @Override
    public ClientAnchor getClientAnchor() {
        HSSFAnchor ha = super.getAnchor();
        if (ha instanceof ClientAnchor) {
            return (ClientAnchor) ha;
        }

        throw new IllegalStateException("Anchor can not be changed in "
                + ClientAnchor.class.getSimpleName());
    }

    @Override
    public void setShapeType(int shapeType) {
        throw new IllegalStateException("Shape type can not be changed in "+this.getClass().getSimpleName());
    }

    @Override
    public void afterRemove(HSSFPatriarch patriarch){
        super.afterRemove(patriarch);
        patriarch.getBoundAggregate().removeTailRecord(getNoteRecord());
    }

    @Override
    protected HSSFShape cloneShape() {
        TextObjectRecord txo = (TextObjectRecord) getTextObjectRecord().cloneViaReserialise();
        EscherContainerRecord spContainer = new EscherContainerRecord();
        byte [] inSp = getEscherContainer().serialize();
        spContainer.fillFields(inSp, 0, new DefaultEscherRecordFactory());
        ObjRecord obj = (ObjRecord) getObjRecord().cloneViaReserialise();
        NoteRecord note = (NoteRecord) getNoteRecord().cloneViaReserialise();
        return new HSSFComment(spContainer, obj, txo, note);
    }
    
    public void setBackgroundImage(int pictureIndex){
        setPropertyValue(new EscherSimpleProperty( EscherProperties.FILL__PATTERNTEXTURE, false, true, pictureIndex));
        setPropertyValue(new EscherSimpleProperty( EscherProperties.FILL__FILLTYPE, false, false, FILL_TYPE_PICTURE));
        EscherBSERecord bse = getPatriarch().getSheet().getWorkbook().getWorkbook().getBSERecord(pictureIndex);
        bse.setRef(bse.getRef() + 1);
    }
    
    public void resetBackgroundImage(){
        EscherSimpleProperty property = getOptRecord().lookup(EscherProperties.FILL__PATTERNTEXTURE);
        if (null != property){
            EscherBSERecord bse = getPatriarch().getSheet().getWorkbook().getWorkbook().getBSERecord(property.getPropertyValue());
            bse.setRef(bse.getRef() - 1);
            getOptRecord().removeEscherProperty(EscherProperties.FILL__PATTERNTEXTURE);
        }
        setPropertyValue(new EscherSimpleProperty( EscherProperties.FILL__FILLTYPE, false, false, FILL_TYPE_SOLID));
    }
    
    public int getBackgroundImageId(){
        EscherSimpleProperty property = getOptRecord().lookup(EscherProperties.FILL__PATTERNTEXTURE);
        return property == null ? 0 : property.getPropertyValue();
    }

    private void setHidden(boolean value){
        EscherSimpleProperty property = getOptRecord().lookup(EscherProperties.GROUPSHAPE__PRINT);
        // see http://msdn.microsoft.com/en-us/library/dd949807(v=office.12).aspx
        if (value){
            setPropertyValue(new EscherSimpleProperty(EscherProperties.GROUPSHAPE__PRINT, false, false, property.getPropertyValue() | GROUP_SHAPE_HIDDEN_MASK));
        } else {
            setPropertyValue(new EscherSimpleProperty(EscherProperties.GROUPSHAPE__PRINT, false, false, property.getPropertyValue() & GROUP_SHAPE_NOT_HIDDEN_MASK));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HSSFComment)) {
            return false;
        }
        HSSFComment other = (HSSFComment) obj;
        return getNoteRecord().equals(other.getNoteRecord());
    }

    @Override
    public int hashCode() {
        return ((getRow()*17) + getColumn())*31;
    }
}
