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
import org.apache.poi.xssf.usermodel.helpers.RichTextStringHelper;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.ss.util.CellReference;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;

public class XSSFComment implements Comment {
	
	private CTComment comment;
	private CommentsTable comments;

	/**
	 * Creates a new XSSFComment, associated with a given
	 *  low level comment object.
	 * If, as an end user, you want a new XSSFComment
	 *  object, the please ask your sheet for one.
	 */
	public XSSFComment(CommentsTable comments, CTComment comment) {
		this.comment = comment;
		this.comments = comments;
	}

	public String getAuthor() {
		return comments.getAuthor((int)comment.getAuthorId());
	}

	public int getColumn() {
		return (new CellReference(comment.getRef())).getCol();
	}

	public int getRow() {
		return (new CellReference(comment.getRef())).getRow();
	}

	public boolean isVisible() {
		// TODO Auto-generated method stub
		return true;
	}

	public void setAuthor(String author) {
		comment.setAuthorId(
				comments.findAuthor(author)
		);
	}

	public void setColumn(short col) {
		initializeRef();
		String newRef = 
			(new CellReference(getRow(), col)).formatAsString();
		comment.setRef(newRef);
	}

	private void initializeRef() {
		if (comment.getRef() == null) {
			comment.setRef("A1");
		}
	}

	public void setRow(int row) {
		initializeRef();
		String newRef =
			(new CellReference(row, getColumn())).formatAsString();
		comment.setRef(newRef);
	}
	
	public RichTextString getString() {
		return RichTextStringHelper.convertFromRst(comment.getText());
	}

	public void setString(RichTextString string) {
		CTRst text = comment.addNewText();
		RichTextStringHelper.convertToRst(string, text);
	}
	
	public void setString(String string) {
		RichTextString richTextString = new XSSFRichTextString(string);
		setString(richTextString);
	}

	public void setVisible(boolean visible) {
		// TODO Auto-generated method stub
	}
}
