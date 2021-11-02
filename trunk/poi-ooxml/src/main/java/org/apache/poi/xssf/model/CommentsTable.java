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

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.util.Internal;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCommentList;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComments;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CommentsDocument;

@Internal
public class CommentsTable extends POIXMLDocumentPart implements Comments {

    public static final String DEFAULT_AUTHOR = "";
    public static final int DEFAULT_AUTHOR_ID = 0;

    /**
     * Underlying XML Beans CTComment list.
     */
    private CTComments comments;
    /**
     * XML Beans uses a list, which is very slow
     *  to search, so we wrap things with our own
     *  map for fast lookup.
     */
    private Map<CellAddress, CTComment> commentRefs;

    public CommentsTable() {
        super();
        comments = CTComments.Factory.newInstance();
        comments.addNewCommentList();
        comments.addNewAuthors().addAuthor(DEFAULT_AUTHOR);
    }

    /**
     * @since POI 3.14-Beta1
     */
    public CommentsTable(PackagePart part) throws IOException {
        super(part);
        readFrom(part.getInputStream());
    }
    
    public void readFrom(InputStream is) throws IOException {
        try {
            CommentsDocument doc = CommentsDocument.Factory.parse(is, DEFAULT_XML_OPTIONS);
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
     *  @param oldReference the comment to remove from the commentRefs map
     *  @param comment the comment to replace in the commentRefs map
     */
    public void referenceUpdated(CellAddress oldReference, CTComment comment) {
       if(commentRefs != null) {
          commentRefs.remove(oldReference);
          commentRefs.put(new CellAddress(comment.getRef()), comment);
       }
    }

    @Override
    public int getNumberOfComments() {
        return comments.getCommentList().sizeOfCommentArray();
    }

    @Override
    public int getNumberOfAuthors() {
        return comments.getAuthors().sizeOfAuthorArray();
    }

    @Override
    public String getAuthor(long authorId) {
        return comments.getAuthors().getAuthorArray(Math.toIntExact(authorId));
    }

    @Override
    public int findAuthor(String author) {
        String[] authorArray = comments.getAuthors().getAuthorArray();
        for (int i = 0 ; i < authorArray.length; i++) {
            if (authorArray[i].equals(author)) {
                return i;
            }
        }
        return addNewAuthor(author);
    }

    /**
     * Finds the cell comment at cellAddress, if one exists
     *
     * @param cellAddress the address of the cell to find a comment
     * @return cell comment if one exists, otherwise returns null
     */
    @Override
    public XSSFComment findCellComment(CellAddress cellAddress) {
        CTComment ct = getCTComment(cellAddress);
        return ct == null ? null : new XSSFComment(this, ct, null);
    }
    
    /**
     * Get the underlying CTComment xmlbean for a comment located at cellRef, if it exists
     *
     * @param cellRef the location of the cell comment
     * @return CTComment xmlbean if comment exists, otherwise return null.
     */
    @Internal
    public CTComment getCTComment(CellAddress cellRef) {
        // Create the cache if needed
        prepareCTCommentCache();

        // Return the comment, or null if not known
        return commentRefs.get(cellRef);
    }

    /**
     * Returns all cell addresses that have comments.
     * @return An iterator to traverse all cell addresses that have comments.
     * @since 4.0.0
     */
    @Override
    public Iterator<CellAddress> getCellAddresses() {
        prepareCTCommentCache();
        return commentRefs.keySet().iterator();
    }

    /**
     * Refresh Map<CellAddress, CTComment> commentRefs cache,
     * Calls that use the commentRefs cache will perform in O(1)
     * time rather than O(n) lookup time for List<CTComment> comments.
     */
    private void prepareCTCommentCache() {
        // Create the cache if needed
        if(commentRefs == null) {
           commentRefs = new HashMap<>();
           for (CTComment comment : comments.getCommentList().getCommentArray()) {
              commentRefs.put(new CellAddress(comment.getRef()), comment);
           }
        }
    }
    
    /**
     * Create a new comment located` at cell address
     *
     * @param ref the location to add the comment
     * @return a new CTComment located at ref with default author
     */
    @Internal
    public CTComment newComment(CellAddress ref) {
        CTComment ct = comments.getCommentList().addNewComment();
        ct.setRef(ref.formatAsString());
        ct.setAuthorId(DEFAULT_AUTHOR_ID);
        
        if(commentRefs != null) {
           commentRefs.put(ref, ct);
        }
        return ct;
    }

    /**
     * Remove the comment at cellRef location, if one exists
     *
     * @param cellRef the location of the comment to remove
     * @return returns true if a comment was removed
     */
    @Override
    public boolean removeComment(CellAddress cellRef) {
        final String stringRef = cellRef.formatAsString();
        CTCommentList lst = comments.getCommentList();
        if(lst != null) {
            CTComment[] commentArray = lst.getCommentArray();
            for (int i = 0; i < commentArray.length; i++) {
                CTComment comment = commentArray[i];
                if (stringRef.equals(comment.getRef())) {
                    lst.removeComment(i);

                    if(commentRefs != null) {
                       commentRefs.remove(cellRef);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Add a new author to the CommentsTable.
     * This does not check if the author already exists.
     *
     * @param author the name of the comment author
     * @return the index of the new author
     */
    private int addNewAuthor(String author) {
        int index = comments.getAuthors().sizeOfAuthorArray();
        comments.getAuthors().insertAuthor(index, author);
        return index;
    }

    /**
     * Returns the underlying CTComments list xmlbean
     *
     * @return underlying comments list xmlbean
     */
    @Internal
    public CTComments getCTComments(){
        return comments;
    }
}
