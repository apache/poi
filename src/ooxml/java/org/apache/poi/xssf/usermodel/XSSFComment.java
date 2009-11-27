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
package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.model.CommentsTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;
import schemasMicrosoftComVml.CTShape;

import java.math.BigInteger;

public class XSSFComment implements Comment {
	
	private final CTComment _comment;
	private final CommentsTable _comments;
    private final CTShape _vmlShape;

    /**
     * cached reference to the string with the comment text
     */
    private XSSFRichTextString _str;

    /**
	 * Creates a new XSSFComment, associated with a given
	 *  low level comment object.
	 */
	public XSSFComment(CommentsTable comments, CTComment comment, CTShape vmlShape) {
		_comment = comment;
		_comments = comments;
        _vmlShape = vmlShape;
	}

    /**
     *
     * @return Name of the original comment author. Default value is blank.
     */
    public String getAuthor() {
		return _comments.getAuthor((int) _comment.getAuthorId());
	}

    /**
     * Name of the original comment author. Default value is blank.
     *
     * @param author the name of the original author of the comment
     */
    public void setAuthor(String author) {
        _comment.setAuthorId(
                _comments.findAuthor(author)
        );
    }

    /**
     * @return the 0-based column of the cell that the comment is associated with.
     */
	public int getColumn() {
		return new CellReference(_comment.getRef()).getCol();
	}

    /**
     * @return the 0-based row index of the cell that the comment is associated with.
     */
	public int getRow() {
		return new CellReference(_comment.getRef()).getRow();
	}

    /**
     * @return whether the comment is visible
     */
    public boolean isVisible() {
        boolean visible = false;
        if(_vmlShape != null){
            String style = _vmlShape.getStyle();
            visible = style != null && style.indexOf("visibility:visible") != -1;
        }
		return visible;
	}

    /**
     * @param visible whether the comment is visible
     */
    public void setVisible(boolean visible) {
        if(_vmlShape != null){
            String style;
            if(visible) style = "position:absolute;visibility:visible";
            else style = "position:absolute;visibility:hidden";
            _vmlShape.setStyle(style);
        }
    }

    /**
     * Set the column of the cell that contains the comment
     *
     * @param col the 0-based column of the cell that contains the comment
     */
    public void setColumn(int col) {
        CellReference ref = new CellReference(getRow(), col);
		_comment.setRef(ref.formatAsString());
        if(_vmlShape != null) _vmlShape.getClientDataArray(0).setColumnArray(0, new BigInteger(String.valueOf(col)));
	}

    /**
     * Set the row of the cell that contains the comment
     *
     * @param row the 0-based row of the cell that contains the comment
     */
	public void setRow(int row) {
		String newRef =
			(new CellReference(row, getColumn())).formatAsString();
		_comment.setRef(newRef);
        if(_vmlShape != null) _vmlShape.getClientDataArray(0).setRowArray(0, new BigInteger(String.valueOf(row)));
    }
	
    /**
     * @return the rich text string of the comment
     */
	public XSSFRichTextString getString() {
		if(_str == null) {
            CTRst rst = _comment.getText();
            if(rst != null) _str = new XSSFRichTextString(_comment.getText());
        }
        return _str;
	}

    /**
     * Sets the rich text string used by this comment.
     *
     * @param string  the XSSFRichTextString used by this object.
     */
	public void setString(RichTextString string) {
        if(!(string instanceof XSSFRichTextString)){
            throw new IllegalArgumentException("Only XSSFRichTextString argument is supported");
        }
        _str = (XSSFRichTextString)string;
        _comment.setText(_str.getCTRst());
	}
	
	public void setString(String string) {
		setString(new XSSFRichTextString(string));
	}

    /**
     * @return the xml bean holding this comment's properties
     */
    protected CTComment getCTComment(){
        return _comment;
    }

    protected CTShape getCTShape(){
        return _vmlShape;
    }
}
