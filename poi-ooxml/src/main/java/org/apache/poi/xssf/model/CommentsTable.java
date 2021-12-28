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

import com.microsoft.schemas.vml.CTShape;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.OoxmlSheetExtensions;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFVMLDrawing;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCommentList;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComments;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CommentsDocument;

@Internal
public class CommentsTable extends POIXMLDocumentPart implements Comments {

    public static final String DEFAULT_AUTHOR = "";
    public static final int DEFAULT_AUTHOR_ID = 0;

    private Sheet sheet;
    private XSSFVMLDrawing vmlDrawing;

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
        try (InputStream stream = part.getInputStream()) {
            readFrom(stream);
        }
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
    @Internal
    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        try (OutputStream out = part.getOutputStream()) {
            writeTo(out);
        }
    }

    /**
     * Called after the reference is updated, so that
     *  we can reflect that in our cache
     * @param oldReference the comment to remove from the commentRefs map
     * @param comment the comment to replace in the commentRefs map
     * @deprecated use {@link #referenceUpdated(CellAddress, XSSFComment)}
     */
    @Deprecated
    @Removal(version = "6.0.0")
    public void referenceUpdated(CellAddress oldReference, CTComment comment) {
       if(commentRefs != null) {
          commentRefs.remove(oldReference);
          commentRefs.put(new CellAddress(comment.getRef()), comment);
       }
    }

    /**
     * Called after the reference is updated, so that
     *  we can reflect that in our cache
     * @param oldReference the comment to remove from the commentRefs map
     * @param comment the comment to replace in the commentRefs map
     * @see #commentUpdated(XSSFComment)
     * @since POI 5.2.0
     */
    @Override
    public void referenceUpdated(CellAddress oldReference, XSSFComment comment) {
        if(commentRefs != null) {
            commentRefs.remove(oldReference);
            commentRefs.put(comment.getAddress(), comment.getCTComment());
        }
    }

    /**
     * Called after the comment is updated, so that
     *  we can reflect that in our cache
     * @param comment the comment to replace in the commentRefs map
     * @since POI 5.2.0
     * @see #referenceUpdated(CellAddress, XSSFComment)
     */
    @Override
    public void commentUpdated(XSSFComment comment) {
        //no-op in this implementation
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
        CTComment ctComment = getCTComment(cellAddress);
        if(ctComment == null) {
            return null;
        }

        XSSFVMLDrawing vml = getVMLDrawing(sheet, false);
        return new XSSFComment(this, ctComment,
                vml == null ? null : vml.findCommentShape(cellAddress.getRow(), cellAddress.getColumn()));
    }

    /**
     * Get the underlying CTComment xmlbean for a comment located at cellRef, if it exists
     *
     * @param cellRef the location of the cell comment
     * @return CTComment xmlbean if comment exists, otherwise return null.
     */
    @Internal
    CTComment getCTComment(CellAddress cellRef) {
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
     * Create a new comment and add to the CommentTable.
     * @param clientAnchor the anchor for this comment
     * @return new XSSFComment
     * @since POI 5.2.0
     */
    @Override
    public XSSFComment createNewComment(ClientAnchor clientAnchor) {
        XSSFVMLDrawing vml = getVMLDrawing(sheet, true);
        CTShape vmlShape = vml == null ? null : vml.newCommentShape();
        if (vmlShape != null && clientAnchor instanceof XSSFClientAnchor && ((XSSFClientAnchor)clientAnchor).isSet()) {
            // convert offsets from emus to pixels since we get a
            // DrawingML-anchor
            // but create a VML Drawing
            int dx1Pixels = clientAnchor.getDx1() / Units.EMU_PER_PIXEL;
            int dy1Pixels = clientAnchor.getDy1() / Units.EMU_PER_PIXEL;
            int dx2Pixels = clientAnchor.getDx2() / Units.EMU_PER_PIXEL;
            int dy2Pixels = clientAnchor.getDy2() / Units.EMU_PER_PIXEL;
            String position = clientAnchor.getCol1() + ", " + dx1Pixels + ", " + clientAnchor.getRow1() + ", " + dy1Pixels + ", " +
                    clientAnchor.getCol2() + ", " + dx2Pixels + ", " + clientAnchor.getRow2() + ", " + dy2Pixels;
            vmlShape.getClientDataArray(0).setAnchorArray(0, position);
        }
        CellAddress ref = new CellAddress(clientAnchor.getRow1(), clientAnchor.getCol1());

        if (findCellComment(ref) != null) {
            throw new IllegalArgumentException("Multiple cell comments in one cell are not allowed, cell: " + ref);
        }

        return new XSSFComment(this, newComment(ref), vmlShape);
    }
    
    /**
     * Create a new comment located at cell address
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
     * Returns the underlying CTComments list xmlbean
     *
     * @return underlying comments list xmlbean
     */
    @Internal
    public CTComments getCTComments(){
        return comments;
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

    private XSSFVMLDrawing getVMLDrawing(Sheet sheet, boolean autocreate) {
        if (vmlDrawing == null) {
            if (sheet instanceof OoxmlSheetExtensions) {
                vmlDrawing = ((OoxmlSheetExtensions)sheet).getVMLDrawing(autocreate);
            }
        }
        return vmlDrawing;
    }
}
