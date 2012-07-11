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
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.RichTextString;

/**
 * Represents a cell comment - a sticky note associated with a cell.
 *
 * @author Yegor Kozlov
 */
public class HSSFComment extends HSSFTextbox implements Comment {

    /*
      * TODO - make HSSFComment more consistent when created vs read from file.
      * Currently HSSFComment has two main forms (corresponding to the two constructors).   There
      * are certain operations that only work on comment objects in one of the forms (e.g. deleting
      * comments).
      * POI is also deficient in its management of RowRecord fields firstCol and lastCol.  Those
      * fields are supposed to take comments into account, but POI does not do this yet (feb 2009).
      * It seems like HSSFRow should manage a collection of local HSSFComments
      */

    private NoteRecord _note;

    public HSSFComment(EscherContainerRecord spContainer, ObjRecord objRecord, TextObjectRecord textObjectRecord, NoteRecord _note) {
        super(spContainer, objRecord, textObjectRecord);
        this._note = _note;
    }

    /**
     * Construct a new comment with the given parent and anchor.
     *
     * @param parent
     * @param anchor defines position of this anchor in the sheet
     */
    public HSSFComment(HSSFShape parent, HSSFAnchor anchor) {
        super(parent, anchor);
        _note = createNoteRecord();
        //default color for comments
        setFillColor(0x08000050);

        //by default comments are hidden
        setVisible(false);
        setAuthor("");
    }

    protected HSSFComment(NoteRecord note, TextObjectRecord txo) {
        this(null, new HSSFClientAnchor());
        _note = note;
    }

    @Override
    void afterInsert(HSSFPatriarch patriarch) {
        super.afterInsert(patriarch);
        _patriarch._getBoundAggregate().addTailRecord(getNoteRecord());
    }

    @Override
    protected EscherContainerRecord createSpContainer() {
        EscherContainerRecord spContainer = super.createSpContainer();
        EscherOptRecord opt = spContainer.getChildById(EscherOptRecord.RECORD_ID);
        removeEscherProperty(opt, EscherProperties.TEXT__TEXTLEFT);
        removeEscherProperty(opt, EscherProperties.TEXT__TEXTRIGHT);
        removeEscherProperty(opt, EscherProperties.TEXT__TEXTTOP);
        removeEscherProperty(opt, EscherProperties.TEXT__TEXTBOTTOM);
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

    private NoteRecord createNoteRecord(){
        NoteRecord note = new NoteRecord();
        note.setFlags(NoteRecord.NOTE_HIDDEN);
        note.setAuthor("");
        return note;
    }

    @Override
    void setShapeId(int shapeId) {
        super.setShapeId(shapeId);
        CommonObjectDataSubRecord cod = (CommonObjectDataSubRecord) getObjRecord().getSubRecords().get(0);
        cod.setObjectId((short) (shapeId));
        _note.setShapeId(shapeId);
    }

    /**
     * Returns whether this comment is visible.
     *
     * @param visible <code>true</code> if the comment is visible, <code>false</code> otherwise
     */
    public void setVisible(boolean visible) {
        _note.setFlags(visible ? NoteRecord.NOTE_VISIBLE : NoteRecord.NOTE_HIDDEN);
    }

    /**
     * Sets whether this comment is visible.
     *
     * @return <code>true</code> if the comment is visible, <code>false</code> otherwise
     */
    public boolean isVisible() {
        return _note.getFlags() == NoteRecord.NOTE_VISIBLE;
    }

    /**
     * Return the row of the cell that contains the comment
     *
     * @return the 0-based row of the cell that contains the comment
     */
    public int getRow() {
        return _note.getRow();
    }

    /**
     * Set the row of the cell that contains the comment
     *
     * @param row the 0-based row of the cell that contains the comment
     */
    public void setRow(int row) {
        _note.setRow(row);
    }

    /**
     * Return the column of the cell that contains the comment
     *
     * @return the 0-based column of the cell that contains the comment
     */
    public int getColumn() {
        return _note.getColumn();
    }

    /**
     * Set the column of the cell that contains the comment
     *
     * @param col the 0-based column of the cell that contains the comment
     */
    public void setColumn(int col) {
        _note.setColumn(col);
    }

    /**
     * @deprecated (Nov 2009) use {@link HSSFComment#setColumn(int)} }
     */
    @Deprecated
    public void setColumn(short col) {
        setColumn((int) col);
    }

    /**
     * Name of the original comment author
     *
     * @return the name of the original author of the comment
     */
    public String getAuthor() {
        return _note.getAuthor();
    }

    /**
     * Name of the original comment author
     *
     * @param author the name of the original author of the comment
     */
    public void setAuthor(String author) {
        if (_note != null) _note.setAuthor(author);
    }

    /**
     * Returns the underlying Note record
     */
    protected NoteRecord getNoteRecord() {
        return _note;
    }

    @Override
    public void setShapeType(int shapeType) {
        throw new IllegalStateException("Shape type can not be changed in "+this.getClass().getSimpleName());
    }

    public void afterRemove(HSSFPatriarch patriarch){
        patriarch._getBoundAggregate().removeShapeToObjRecord(getEscherContainer().getChildById(EscherClientDataRecord.RECORD_ID));
        patriarch._getBoundAggregate().removeShapeToObjRecord(getEscherContainer().getChildById(EscherTextboxRecord.RECORD_ID));
        patriarch._getBoundAggregate().removeTailRecord(getNoteRecord());
    }
}
