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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAuthors;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCommentList;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComments;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CommentsDocument;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;

public class CommentsTable extends POIXMLDocumentPart {
    protected CTComments comments;

    public CommentsTable() {
        super();
        comments = CTComments.Factory.newInstance();
    }

    public CommentsTable(PackagePart part, PackageRelationship rel) throws IOException {
        super(part, rel);
        readFrom(part.getInputStream());
    }

    public void readFrom(InputStream is) throws IOException {
        try {
            CommentsDocument doc = CommentsDocument.Factory.parse(is);
            comments = doc.getComments();
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }
    public void writeTo(OutputStream out) throws IOException {
        CommentsDocument doc = CommentsDocument.Factory.newInstance();
        doc.setComments(comments);
        doc.save(out, DEFAULT_XML_OPTIONS);
    }

    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        writeTo(out);
        out.close();
    }

    public int getNumberOfComments() {
        return comments.getCommentList().sizeOfCommentArray();
    }
    public int getNumberOfAuthors() {
        return getCommentsAuthors().sizeOfAuthorArray();
    }

    public String getAuthor(long authorId) {
        return getCommentsAuthors().getAuthorArray((int)authorId);
    }

    public int findAuthor(String author) {
        for (int i = 0 ; i < getCommentsAuthors().sizeOfAuthorArray() ; i++) {
            if (getCommentsAuthors().getAuthorArray(i).equals(author)) {
                return i;
            }
        }
        return addNewAuthor(author);
    }

    public XSSFComment findCellComment(int row, int column) {
        return findCellComment(
                (new CellReference(row, column)).formatAsString() );
    }

    public XSSFComment findCellComment(String cellRef) {
        for (CTComment comment : getCommentsList().getCommentArray()) {
            if (cellRef.equals(comment.getRef())) {
                return new XSSFComment(this, comment);
            }
        }
        return null;
    }

    /**
     * Generates a new XSSFComment, associated with the
     *  current comments list.
     */
    public XSSFComment addComment() {
        return new XSSFComment(this, getCommentsList().addNewComment());
    }

    private CTCommentList getCommentsList() {
        if (comments.getCommentList() == null) {
            comments.addNewCommentList();
        }
        return comments.getCommentList();
    }

    private CTAuthors getCommentsAuthors() {
        if (comments.getAuthors() == null) {
            comments.addNewAuthors();
        }
        return comments.getAuthors();
    }

    private int addNewAuthor(String author) {
        int index = getCommentsAuthors().sizeOfAuthorArray();
        getCommentsAuthors().insertAuthor(index, author);
        return index;
    }

    public CTComments getCTComments(){
        return comments;
    }
}