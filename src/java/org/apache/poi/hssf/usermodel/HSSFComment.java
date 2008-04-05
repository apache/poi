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

    private boolean visible;
    private short col, row;
    private String author;

    private NoteRecord note = null;
    private TextObjectRecord txo = null;

    /**
     * Construct a new comment with the given parent and anchor.
     *
     * @param parent
     * @param anchor  defines position of this anchor in the sheet
     */
    public HSSFComment( HSSFShape parent, HSSFAnchor anchor )
    {
        super( parent, anchor );
        setShapeType(OBJECT_TYPE_COMMENT);

        //default color for comments
        fillColor = 0x08000050;

        //by default comments are hidden
        visible = false;

        author = "";
    }

    protected HSSFComment( NoteRecord note, TextObjectRecord txo )
    {
        this( (HSSFShape)null, (HSSFAnchor)null );
        this.txo = txo;
        this.note = note;
    }

    /**
     * Returns whether this comment is visible.
     *
     * @param visible <code>true</code> if the comment is visible, <code>false</code> otherwise
     */
    public void setVisible(boolean visible){
        if(note != null) note.setFlags(visible ? NoteRecord.NOTE_VISIBLE : NoteRecord.NOTE_HIDDEN);
        this.visible = visible;
    }

    /**
     * Sets whether this comment is visible.
     *
     * @return <code>true</code> if the comment is visible, <code>false</code> otherwise
     */
    public boolean isVisible(){
        return this.visible;
    }

    /**
     * Return the row of the cell that contains the comment
     *
     * @return the 0-based row of the cell that contains the comment
     */
    public int getRow(){
        return row;
    }

    /**
     * Set the row of the cell that contains the comment
     *
     * @param row the 0-based row of the cell that contains the comment
     */
    public void setRow(int row){
        if(note != null) note.setRow((short)row);
        this.row = (short)row;
    }

    /**
     * Return the column of the cell that contains the comment
     *
     * @return the 0-based column of the cell that contains the comment
     */
    public int getColumn(){
        return col;
    }

    /**
     * Set the column of the cell that contains the comment
     *
     * @param col the 0-based column of the cell that contains the comment
     */
    public void setColumn(short col){
        if(note != null) note.setColumn(col);
        this.col = col;
    }

    /**
     * Name of the original comment author
     *
     * @return the name of the original author of the comment
     */
    public String getAuthor(){
        return author;
    }

    /**
     * Name of the original comment author
     *
     * @param author the name of the original author of the comment
     */
    public void setAuthor(String author){
        if(note != null) note.setAuthor(author);
        this.author = author;
    }
    
    /**
     * Sets the rich text string used by this comment.
     *
     * @param string    Sets the rich text string used by this object.
     */
    public void setString( RichTextString string )  {
        HSSFRichTextString hstring = (HSSFRichTextString) string;
        //if font is not set we must set the default one
        if (hstring.numFormattingRuns() == 0) hstring.applyFont((short)0);

        if (txo != null) {
            int frLength = ( hstring.numFormattingRuns() + 1 ) * 8;
            txo.setFormattingRunLength( (short) frLength );
            txo.setTextLength( (short) hstring.length() );
            txo.setStr( hstring );
        }
        super.setString(string);
    }
}
