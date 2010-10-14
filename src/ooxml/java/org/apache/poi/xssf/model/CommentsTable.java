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
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCommentList;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComments;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CommentsDocument;

public class CommentsTable extends POIXMLDocumentPart {
    private CTComments comments;
    /**
     * XML Beans uses a list, which is very slow
     *  to search, so we wrap things with our own
     *  map for fast lookup.
     */
    private Map<String, CTComment> commentRefs;

    public CommentsTable() {
        super();
        comments = CTComments.Factory.newInstance();
        comments.addNewCommentList();
        comments.addNewAuthors().addAuthor("");
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
    
    /**
     * Called after the reference is updated, so that
     *  we can reflect that in our cache
     */
    public void referenceUpdated(String oldReference, CTComment comment) {
       if(commentRefs != null) {
          commentRefs.remove(oldReference);
          commentRefs.put(comment.getRef(), comment);
       }
    }

    public int getNumberOfComments() {
        return comments.getCommentList().sizeOfCommentArray();
    }

    public int getNumberOfAuthors() {
        return comments.getAuthors().sizeOfAuthorArray();
    }

    public String getAuthor(long authorId) {
        return comments.getAuthors().getAuthorArray((int)authorId);
    }

    public int findAuthor(String author) {
        for (int i = 0 ; i < comments.getAuthors().sizeOfAuthorArray() ; i++) {
            if (comments.getAuthors().getAuthorArray(i).equals(author)) {
                return i;
            }
        }
        return addNewAuthor(author);
    }

    public XSSFComment findCellComment(String cellRef) {
        CTComment ct = getCTComment(cellRef);
        return ct == null ? null : new XSSFComment(this, ct, null);
    }

    @SuppressWarnings("deprecation") //YK: getXYZArray() array accessors are deprecated in xmlbeans with JDK 1.5 support
    public CTComment getCTComment(String cellRef) {
        // Create the cache if needed
        if(commentRefs == null) {
           commentRefs = new HashMap<String, CTComment>();
           for (CTComment comment : comments.getCommentList().getCommentArray()) {
              commentRefs.put(comment.getRef(), comment);
           }
        }

        // Return the comment, or null if not known
        return commentRefs.get(cellRef);
    }

    public CTComment newComment() {
        CTComment ct = comments.getCommentList().addNewComment();
        ct.setRef("A1");
        ct.setAuthorId(0);
        
        if(commentRefs != null) {
           commentRefs.put(ct.getRef(), ct);
        }
        return ct;
    }

    public boolean removeComment(String cellRef) {
        CTCommentList lst = comments.getCommentList();
        if(lst != null) for(int i=0; i < lst.sizeOfCommentArray(); i++) {
            CTComment comment = lst.getCommentArray(i);
            if (cellRef.equals(comment.getRef())) {
                lst.removeComment(i);
                
                if(commentRefs != null) {
                   commentRefs.remove(cellRef);
                }
                return true;
            }
        }
        return false;
    }

    private int addNewAuthor(String author) {
        int index = comments.getAuthors().sizeOfAuthorArray();
        comments.getAuthors().insertAuthor(index, author);
        return index;
    }

    public CTComments getCTComments(){
        return comments;
    }
}