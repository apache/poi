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

package org.apache.poi.xssf.model;

import org.apache.poi.xssf.usermodel.XSSFComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCommentList;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComments;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;

import junit.framework.TestCase;


public class TestCommentsTable extends TestCase {
	
	private static final String TEST_A2_TEXT = "test A2 text";
	private static final String TEST_A1_TEXT = "test A1 text";
	private static final String TEST_AUTHOR = "test author";

	public void testfindAuthor() {
		CTComments comments = CTComments.Factory.newInstance();
		CommentsTable sheetComments = new CommentsTable(comments);

		assertEquals(0, sheetComments.findAuthor(TEST_AUTHOR));
		assertEquals(1, sheetComments.findAuthor("another author"));
		assertEquals(0, sheetComments.findAuthor(TEST_AUTHOR));
	}
	
	public void testGetCellComment() {
		CTComments comments = CTComments.Factory.newInstance();
		CommentsTable sheetComments = new CommentsTable(comments);
		CTCommentList commentList = comments.addNewCommentList();
		
		// Create 2 comments for A1 and A" cells
		CTComment comment0 = commentList.insertNewComment(0);
		comment0.setRef("A1");
		CTRst ctrst0 = CTRst.Factory.newInstance();
		ctrst0.setT(TEST_A1_TEXT);
		comment0.setText(ctrst0);
		CTComment comment1 = commentList.insertNewComment(0);
		comment1.setRef("A2");
		CTRst ctrst1 = CTRst.Factory.newInstance();
		ctrst1.setT(TEST_A2_TEXT);
		comment1.setText(ctrst1);
		
		// test finding the right comment for a cell
		assertEquals(TEST_A1_TEXT, sheetComments.findCellComment("A1").getString());
		assertEquals(TEST_A1_TEXT, sheetComments.findCellComment(0, 0).getString());
		assertEquals(TEST_A2_TEXT, sheetComments.findCellComment("A2").getString());
		assertEquals(TEST_A2_TEXT, sheetComments.findCellComment(1, 0).getString());
		assertNull(sheetComments.findCellComment("A3"));
		assertNull(sheetComments.findCellComment(2, 0));
	}
	
	public void testSetCellComment() {
		CTComments comments = CTComments.Factory.newInstance();
		CommentsTable sheetComments = new CommentsTable(comments);
		CTCommentList commentList = comments.addNewCommentList();
		assertEquals(0, commentList.sizeOfCommentArray());
		XSSFComment comment = new XSSFComment(sheetComments);
		comment.setAuthor("test A1 author");
		
		sheetComments.setCellComment("A1", comment);
		assertEquals(1, commentList.sizeOfCommentArray());
		assertEquals("test A1 author", sheetComments.getAuthor(commentList.getCommentArray(0).getAuthorId()));
		
	}
    
}
