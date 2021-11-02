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

package org.apache.poi.xwpf.usermodel;

import java.util.List;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;

/**
 * <p>An IBody represents the different parts of the document which
 * can contain collections of Paragraphs and Tables. It provides a
 * common way to work with these and their contents.</p>
 * <p>Typically, this is something like a XWPFDocument, or one of
 * the parts in it like XWPFHeader, XWPFFooter, XWPFTableCell
 * </p>
 */
public interface IBody {
    /**
     * returns the Part, to which the body belongs, which you need for adding relationship to other parts
     * Actually it is needed of the class XWPFTableCell. Because you have to know to which part the tableCell
     * belongs.
     *
     * @return the Part, to which the body belongs
     */
    POIXMLDocumentPart getPart();

    /**
     * get the PartType of the body, for example
     * DOCUMENT, HEADER, FOOTER, FOOTNOTE,
     *
     * @return the PartType of the body
     */
    BodyType getPartType();

    /**
     * Returns an Iterator with paragraphs and tables,
     * in the order that they occur in the text.
     */
    List<IBodyElement> getBodyElements();

    /**
     * Returns the paragraph(s) that holds
     * the text of the header or footer.
     */
    List<XWPFParagraph> getParagraphs();

    /**
     * Return the table(s) that holds the text
     * of the IBodyPart, for complex cases
     * where a paragraph isn't used.
     */
    List<XWPFTable> getTables();

    /**
     * Returns the paragraph corresponding to the provided {@link CTP}.
     *
     * @param p is instance of CTP and is searching for an XWPFParagraph
     * @return The paragraph corresponding to the {@link CTP}, or {@code null} if there is no corresponding paragraph in
     * this body.
     */
    XWPFParagraph getParagraph(CTP p);

    /**
     * if there is a corresponding {@link XWPFTable} of the parameter ctTable in the tableList of this header
     * the method will return this table
     * if there is no corresponding {@link XWPFTable} the method will return null
     */
    XWPFTable getTable(CTTbl ctTable);

    /**
     * Returns the paragraph that of position pos
     */
    XWPFParagraph getParagraphArray(int pos);

    /**
     * Returns the table at position pos
     */
    XWPFTable getTableArray(int pos);

    /**
     * inserts a new paragraph at position of the cursor
     */
    XWPFParagraph insertNewParagraph(XmlCursor cursor);

    /**
     * inserts a new Table at the cursor position.
     */
    XWPFTable insertNewTbl(XmlCursor cursor);

    /**
     * inserts a new Table at position pos
     */
    void insertTable(int pos, XWPFTable table);

    /**
     * returns the TableCell to which the Table belongs
     */
    XWPFTableCell getTableCell(CTTc cell);

    /**
     * Return XWPFDocument
     */
    XWPFDocument getXWPFDocument();
}
