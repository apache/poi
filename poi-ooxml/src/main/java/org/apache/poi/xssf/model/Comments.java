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

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFComment;

import java.util.Iterator;

/**
 * An interface exposing useful functions for dealing with Excel Workbook Comments.
 * It is intended that this interface should support low level access and not expose
 * all the comments in memory
 */
public interface Comments {

    int getNumberOfComments();

    int getNumberOfAuthors();

    String getAuthor(long authorId);

    int findAuthor(String author);

    /**
     * Finds the cell comment at cellAddress, if one exists
     *
     * @param cellAddress the address of the cell to find a comment
     * @return cell comment if one exists, otherwise returns null
     * @see #findCellComment(Sheet, CellAddress)
     */
    XSSFComment findCellComment(CellAddress cellAddress);

    /**
     * Finds the cell comment at cellAddress, if one exists
     *
     * @param sheet the sheet to check for comments (used to find drawing/shape data for comments) - set to null
     *              if you don't need the drawing/shape data
     * @param cellAddress the address of the cell to find a comment
     * @return cell comment if one exists, otherwise returns null
     * @see #findCellComment(CellAddress)
     * @since POI 5.2.0
     */
    public XSSFComment findCellComment(Sheet sheet, CellAddress cellAddress);

    /**
     * Remove the comment at cellRef location, if one exists
     *
     * @param cellRef the location of the comment to remove
     * @return returns true if a comment was removed
     */
    boolean removeComment(CellAddress cellRef);

    /**
     * Returns all cell addresses that have comments.
     * @return An iterator to traverse all cell addresses that have comments.
     * @since 4.0.0
     */
    Iterator<CellAddress> getCellAddresses();

    /**
     * @param sheet the sheet to check for comments (used to find drawing/shape data for comments) - set to null
     *              if you don't need the drawing/shape data
     * @return iterator of comments
     * @since POI 5.2.0
     */
    Iterator<XSSFComment> commentIterator(Sheet sheet);

    /**
     * Create a new comment and add to the CommentTable.
     * @param sheet sheet to add comment to
     * @param clientAnchor the anchor for this comment
     * @return new XSSFComment
     * @since POI 5.2.0
     */
    XSSFComment createNewComment(Sheet sheet, ClientAnchor clientAnchor);

    /**
     * Called after the reference is updated, so that
     *  we can reflect that in our cache
     * @param oldReference the comment to remove from the commentRefs map
     * @param comment the comment to replace in the commentRefs map
     * @see #commentUpdated(XSSFComment)                
     * @since POI 5.2.0
     */
    void referenceUpdated(CellAddress oldReference, XSSFComment comment);

    /**
     * Called after the comment is updated, so that
     *  we can reflect that in our cache
     * @param comment the comment to replace in the commentRefs map
     * @since POI 5.2.0
     * @see #referenceUpdated(CellAddress, XSSFComment)
     */
    void commentUpdated(XSSFComment comment);
}
