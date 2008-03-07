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

import org.apache.poi.xssf.usermodel.extensions.XSSFSheetComments;
import org.apache.poi.xssf.util.CellReference;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAuthors;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComments;

import junit.framework.TestCase;


public class TestXSSFComment extends TestCase {
    
	private static final String TEST_AUTHOR = "test_author";

	public void testConstructors() {
		XSSFSheetComments sheetComments = new XSSFSheetComments();
		XSSFComment comment = new XSSFComment(sheetComments);
		assertNotNull(comment);
		
		CTComment ctComment = CTComment.Factory.newInstance();
		XSSFComment comment2 = new XSSFComment(sheetComments, ctComment);
		assertNotNull(comment2);
	}
	
	public void testGetColumn() {
		XSSFSheetComments sheetComments = new XSSFSheetComments();
		CTComment ctComment = CTComment.Factory.newInstance();
		ctComment.setRef("A1");
		XSSFComment comment = new XSSFComment(sheetComments, ctComment);
		assertNotNull(comment);
		assertEquals(0, comment.getColumn());
		ctComment.setRef("C10");
		assertEquals(2, comment.getColumn());
	}
	
	public void testGetRow() {
		XSSFSheetComments sheetComments = new XSSFSheetComments();
		CTComment ctComment = CTComment.Factory.newInstance();
		ctComment.setRef("A1");
		XSSFComment comment = new XSSFComment(sheetComments, ctComment);
		assertNotNull(comment);
		assertEquals(0, comment.getRow());
		ctComment.setRef("C10");
		assertEquals(9, comment.getRow());
	}
	
	public void testGetAuthor() {
		CTComments ctComments = CTComments.Factory.newInstance();
		CTComment ctComment = ctComments.addNewCommentList().addNewComment();
		CTAuthors ctAuthors = ctComments.addNewAuthors();
		ctAuthors.insertAuthor(0, TEST_AUTHOR);
		ctComment.setAuthorId(0);

		XSSFSheetComments sheetComments = new XSSFSheetComments(ctComments);
		XSSFComment comment = new XSSFComment(sheetComments, ctComment);
		assertNotNull(comment);
		assertEquals(TEST_AUTHOR, comment.getAuthor());
	}
	
	public void testSetColumn() {
		XSSFSheetComments sheetComments = new XSSFSheetComments();
		CTComment ctComment = CTComment.Factory.newInstance();
		XSSFComment comment = new XSSFComment(sheetComments, ctComment);
		comment.setColumn((short)3);
		assertEquals(3, comment.getColumn());
		assertEquals(3, (new CellReference(ctComment.getRef()).getCol()));
		assertEquals("D1", ctComment.getRef());

		comment.setColumn((short)13);
		assertEquals(13, comment.getColumn());
	}
	
	public void testSetRow() {
		XSSFSheetComments sheetComments = new XSSFSheetComments();
		CTComment ctComment = CTComment.Factory.newInstance();
		XSSFComment comment = new XSSFComment(sheetComments, ctComment);
		comment.setRow(20);
		assertEquals(20, comment.getRow());
		assertEquals(20, (new CellReference(ctComment.getRef()).getRow()));
		assertEquals("A21", ctComment.getRef());

		comment.setRow(19);
		assertEquals(19, comment.getRow());
	}
	
	public void testSetAuthor() {
		XSSFSheetComments sheetComments = new XSSFSheetComments();
		CTComment ctComment = CTComment.Factory.newInstance();
		XSSFComment comment = new XSSFComment(sheetComments, ctComment);
		comment.setAuthor(TEST_AUTHOR);
		assertEquals(TEST_AUTHOR, comment.getAuthor());
	}
    
}
