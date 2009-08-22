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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCommentList;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComments;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CommentsDocument;


public class TestCommentsTable extends TestCase {

	private static final String TEST_A2_TEXT = "test A2 text";
	private static final String TEST_A1_TEXT = "test A1 text";
	private static final String TEST_AUTHOR = "test author";

	public void testfindAuthor() throws Exception {
		CommentsDocument doc = CommentsDocument.Factory.newInstance();
		doc.setComments(CTComments.Factory.newInstance());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		doc.save(out, POIXMLDocumentPart.DEFAULT_XML_OPTIONS);
		CommentsTable sheetComments = new CommentsTable();
		sheetComments.readFrom(new ByteArrayInputStream(out.toByteArray()));

		assertEquals(0, sheetComments.findAuthor(TEST_AUTHOR));
		assertEquals(1, sheetComments.findAuthor("another author"));
		assertEquals(0, sheetComments.findAuthor(TEST_AUTHOR));
		assertEquals(2, sheetComments.findAuthor("YAA"));
		assertEquals(1, sheetComments.findAuthor("another author"));
	}

	public void testGetCellComment() throws Exception {
		CommentsDocument doc = CommentsDocument.Factory.newInstance();
		doc.setComments(CTComments.Factory.newInstance());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		doc.save(out, POIXMLDocumentPart.DEFAULT_XML_OPTIONS);
		CommentsTable sheetComments = new CommentsTable();
		sheetComments.readFrom(new ByteArrayInputStream(out.toByteArray()));


		CTComments comments = sheetComments.getCTComments();
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
		assertEquals(TEST_A1_TEXT, sheetComments.findCellComment("A1").getString().getString());
		assertEquals(TEST_A1_TEXT, sheetComments.findCellComment(0, 0).getString().getString());
		assertEquals(TEST_A2_TEXT, sheetComments.findCellComment("A2").getString().getString());
		assertEquals(TEST_A2_TEXT, sheetComments.findCellComment(1, 0).getString().getString());
		assertNull(sheetComments.findCellComment("A3"));
		assertNull(sheetComments.findCellComment(2, 0));
	}

	public void testAddCellComment() throws Exception {
		CommentsDocument doc = CommentsDocument.Factory.newInstance();
		doc.setComments(CTComments.Factory.newInstance());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		doc.save(out, POIXMLDocumentPart.DEFAULT_XML_OPTIONS);
		CommentsTable sheetComments = new CommentsTable();
		sheetComments.readFrom(new ByteArrayInputStream(out.toByteArray()));

		CTCommentList commentList = sheetComments.getCTComments().addNewCommentList();
		assertEquals(0, commentList.sizeOfCommentArray());

		XSSFComment comment = sheetComments.addComment();
		comment.setAuthor("test A1 author");
		comment.setRow(0);
		comment.setColumn((short)0);

		assertEquals(1, commentList.sizeOfCommentArray());
		assertEquals("test A1 author", sheetComments.getAuthor(commentList.getCommentArray(0).getAuthorId()));
		assertEquals("test A1 author", comment.getAuthor());

		// Change the author, check it updates
		comment.setAuthor("Another Author");
		assertEquals(1, commentList.sizeOfCommentArray());
		assertEquals("Another Author", comment.getAuthor());
	}

	public void testDontLoostNewLines() {
		XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("WithVariousData.xlsx");
		List<POIXMLDocumentPart> rels = wb.getSheetAt(0).getRelations();
		CommentsTable ct = null;
		for(POIXMLDocumentPart p : rels) {
			if(p instanceof CommentsTable){
				ct = (CommentsTable)p;
				break;
			}
		}
		if (ct == null) {
			throw new AssertionFailedError("didn't find comments table");
		}
		assertEquals(2, ct.getNumberOfComments());
		assertEquals(1, ct.getNumberOfAuthors());

		XSSFComment comment = ct.findCellComment("C5");

		assertEquals("Nick Burch", comment.getAuthor());
		assertEquals("Nick Burch:\nThis is a comment", comment.getString().getString());

		wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
		rels = wb.getSheetAt(0).getRelations();
		ct = null;
		for(POIXMLDocumentPart p : rels) {
			if(p instanceof CommentsTable){
				ct = (CommentsTable)p;
				break;
			}
		}
		if (ct == null) {
			throw new AssertionFailedError("didn't find comments table");
		}

		assertEquals(2, ct.getNumberOfComments());
		assertEquals(1, ct.getNumberOfAuthors());

		comment = ct.findCellComment("C5");

		assertEquals("Nick Burch", comment.getAuthor());

		assertEquals("Nick Burch:\nThis is a comment", comment.getString().getString());
	}

	public void testExisting() {
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("WithVariousData.xlsx");
		Sheet sheet1 = workbook.getSheetAt(0);
		Sheet sheet2 = workbook.getSheetAt(1);

		assertTrue( ((XSSFSheet)sheet1).hasComments() );
		assertFalse( ((XSSFSheet)sheet2).hasComments() );

		// Comments should be in C5 and C7
		Row r5 = sheet1.getRow(4);
		Row r7 = sheet1.getRow(6);
		assertNotNull( r5.getCell(2).getCellComment() );
		assertNotNull( r7.getCell(2).getCellComment() );

		// Check they have what we expect
		// TODO: Rich text formatting
		Comment cc5 = r5.getCell(2).getCellComment();
		Comment cc7 = r7.getCell(2).getCellComment();

		assertEquals("Nick Burch", cc5.getAuthor());
		assertEquals("Nick Burch:\nThis is a comment", cc5.getString().getString());
		assertEquals(4, cc5.getRow());
		assertEquals(2, cc5.getColumn());

		assertEquals("Nick Burch", cc7.getAuthor());
		assertEquals("Nick Burch:\nComment #1\n", cc7.getString().getString());
		assertEquals(6, cc7.getRow());
		assertEquals(2, cc7.getColumn());
	}

	public void testWriteRead() {
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("WithVariousData.xlsx");
		XSSFSheet sheet1 = workbook.getSheetAt(0);
		XSSFSheet sheet2 = workbook.getSheetAt(1);

		assertTrue( sheet1.hasComments() );
		assertFalse( sheet2.hasComments() );

		// Change on comment on sheet 1, and add another into
		//  sheet 2
		Row r5 = sheet1.getRow(4);
		Comment cc5 = r5.getCell(2).getCellComment();
		cc5.setAuthor("Apache POI");
		cc5.setString(new XSSFRichTextString("Hello!"));

		Row r2s2 = sheet2.createRow(2);
		Cell c1r2s2 = r2s2.createCell(1);
		assertNull(c1r2s2.getCellComment());

		Comment cc2 = sheet2.createComment();
		cc2.setAuthor("Also POI");
		cc2.setString(new XSSFRichTextString("A new comment"));
		c1r2s2.setCellComment(cc2);


		// Save, and re-load the file
		workbook = XSSFTestDataSamples.writeOutAndReadBack(workbook);

		// Check we still have comments where we should do
		sheet1 = workbook.getSheetAt(0);
		sheet2 = workbook.getSheetAt(1);
		assertNotNull(sheet1.getRow(4).getCell(2).getCellComment());
		assertNotNull(sheet1.getRow(6).getCell(2).getCellComment());
		assertNotNull(sheet2.getRow(2).getCell(1).getCellComment());

		// And check they still have the contents they should do
		assertEquals("Apache POI",
				sheet1.getRow(4).getCell(2).getCellComment().getAuthor());
		assertEquals("Nick Burch",
				sheet1.getRow(6).getCell(2).getCellComment().getAuthor());
		assertEquals("Also POI",
				sheet2.getRow(2).getCell(1).getCellComment().getAuthor());

		assertEquals("Nick Burch:\nThis is a comment",
				sheet1.getRow(4).getCell(2).getCellComment().getString().getString());
	}

	public void testReadWriteMultipleAuthors() {
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("WithMoreVariousData.xlsx");
		XSSFSheet sheet1 = workbook.getSheetAt(0);
		XSSFSheet sheet2 = workbook.getSheetAt(1);

		assertTrue( sheet1.hasComments() );
		assertFalse( sheet2.hasComments() );

		assertEquals("Nick Burch",
				sheet1.getRow(4).getCell(2).getCellComment().getAuthor());
		assertEquals("Nick Burch",
				sheet1.getRow(6).getCell(2).getCellComment().getAuthor());
		assertEquals("Torchbox",
				sheet1.getRow(12).getCell(2).getCellComment().getAuthor());

		// Save, and re-load the file
		workbook = XSSFTestDataSamples.writeOutAndReadBack(workbook);

		// Check we still have comments where we should do
		sheet1 = workbook.getSheetAt(0);
		assertNotNull(sheet1.getRow(4).getCell(2).getCellComment());
		assertNotNull(sheet1.getRow(6).getCell(2).getCellComment());
		assertNotNull(sheet1.getRow(12).getCell(2).getCellComment());

		// And check they still have the contents they should do
		assertEquals("Nick Burch",
				sheet1.getRow(4).getCell(2).getCellComment().getAuthor());
		assertEquals("Nick Burch",
				sheet1.getRow(6).getCell(2).getCellComment().getAuthor());
		assertEquals("Torchbox",
				sheet1.getRow(12).getCell(2).getCellComment().getAuthor());

		// Todo - check text too, once bug fixed
	}
}
