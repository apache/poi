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

import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.TextObjectRecord;
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
	
    private boolean _visible;
    private int _row;
    private int _col;
    private String _author;

    private NoteRecord _note;
    private TextObjectRecord _txo;

    /**
     * Construct a new comment with the given parent and anchor.
     *
     * @param parent
     * @param anchor  defines position of this anchor in the sheet
     */
    public HSSFComment(HSSFShape parent, HSSFAnchor anchor) {
        super(parent, anchor);
        setShapeType(OBJECT_TYPE_COMMENT);

        //default color for comments
        _fillColor = 0x08000050;

        //by default comments are hidden
        _visible = false;

        _author = "";
    }

    protected HSSFComment(NoteRecord note, TextObjectRecord txo) {
        this((HSSFShape) null, (HSSFAnchor) null);
        _txo = txo;
        _note = note;
    }

    /**
     * Returns whether this comment is visible.
     *
     * @param visible <code>true</code> if the comment is visible, <code>false</code> otherwise
     */
    public void setVisible(boolean visible){
        if(_note != null) {
			_note.setFlags(visible ? NoteRecord.NOTE_VISIBLE : NoteRecord.NOTE_HIDDEN);
		}
        _visible = visible;
    }

    /**
     * Sets whether this comment is visible.
     *
     * @return <code>true</code> if the comment is visible, <code>false</code> otherwise
     */
    public boolean isVisible() {
        return _visible;
    }

    /**
     * Return the row of the cell that contains the comment
     *
     * @return the 0-based row of the cell that contains the comment
     */
    public int getRow() {
        return _row;
    }

    /**
     * Set the row of the cell that contains the comment
     *
     * @param row the 0-based row of the cell that contains the comment
     */
    public void setRow(int row) {
        if(_note != null) {
			_note.setRow(row);
        }
        _row = row;
    }

    /**
     * Return the column of the cell that contains the comment
     *
     * @return the 0-based column of the cell that contains the comment
     */
    public int getColumn(){
        return _col;
    }

    /**
     * Set the column of the cell that contains the comment
     *
     * @param col the 0-based column of the cell that contains the comment
     */
    public void setColumn(int col) {
        if(_note != null) {
		    _note.setColumn(col);
        }
        _col = col;
    }
    /**
     * @deprecated (Nov 2009) use {@link HSSFComment#setColumn(int)} }
     */
    @Deprecated
    public void setColumn(short col) {
        setColumn((int)col);
    }

    /**
     * Name of the original comment author
     *
     * @return the name of the original author of the comment
     */
    public String getAuthor() {
        return _author;
    }

    /**
     * Name of the original comment author
     *
     * @param author the name of the original author of the comment
     */
    public void setAuthor(String author){
        if(_note != null) _note.setAuthor(author);
        this._author = author;
    }
    
    /**
     * Sets the rich text string used by this comment.
     *
     * @param string    Sets the rich text string used by this object.
     */
    public void setString(RichTextString string) {
        HSSFRichTextString hstring = (HSSFRichTextString) string;
        //if font is not set we must set the default one
        if (hstring.numFormattingRuns() == 0) hstring.applyFont((short)0);

        if (_txo != null) {
            _txo.setStr(hstring);
        }
        super.setString(string);
    }
    
    /**
     * Returns the underlying Note record
     */
    protected NoteRecord getNoteRecord() {
	    return _note;
	}
    /**
     * Returns the underlying Text record
     */
    protected TextObjectRecord getTextObjectRecord() {
	    return _txo;
	}
}
