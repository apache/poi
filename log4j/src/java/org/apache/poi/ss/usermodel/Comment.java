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

package org.apache.poi.ss.usermodel;

import org.apache.poi.ss.util.CellAddress;

public interface Comment {

    /**
     * Sets whether this comment is visible.
     *
     * @param visible <code>true</code> if the comment is visible, <code>false</code> otherwise
     */
    void setVisible(boolean visible);

    /**
     * Returns whether this comment is visible.
     *
     * @return <code>true</code> if the comment is visible, <code>false</code> otherwise
     */
    boolean isVisible();
    
    /**
     * Get the address of the cell that this comment is attached to
     *
     * @return comment cell address
     * @since 3.15-beta1
     */
    CellAddress getAddress();
    
    /**
     * Set the address of the cell that this comment is attached to
     *
     * @param addr
     * @since 3.15-beta1
     */
    void setAddress(CellAddress addr);
    
    /**
     * Set the address of the cell that this comment is attached to
     *
     * @param row
     * @param col
     * @since 3.15-beta1
     */
    void setAddress(int row, int col);

    /**
     * Return the row of the cell that contains the comment
     *
     * @return the 0-based row of the cell that contains the comment
     */
    int getRow();

    /**
     * Set the row of the cell that contains the comment
     *
     * @param row the 0-based row of the cell that contains the comment
     */
    void setRow(int row);

    /**
     * Return the column of the cell that contains the comment
     *
     * @return the 0-based column of the cell that contains the comment
     */
    int getColumn();

    /**
     * Set the column of the cell that contains the comment
     *
     * @param col the 0-based column of the cell that contains the comment
     */
    void setColumn(int col);

    /**
     * Name of the original comment author
     *
     * @return the name of the original author of the comment
     */
    String getAuthor();

    /**
     * Name of the original comment author
     *
     * @param author the name of the original author of the comment
     */
    void setAuthor(String author);
    
    /**
     * Fetches the rich text string of the comment
     */
    public RichTextString getString();

    /**
     * Sets the rich text string used by this comment.
     *
     * @param string    Sets the rich text string used by this object.
     */
    void setString(RichTextString string);

    /**
     * Return defines position of this anchor in the sheet.
     * The anchor is the yellow box/balloon that is rendered on top of the sheets
     * when the comment is visible.
     * 
     * To associate a comment with a different cell, use {@link #setAddress}.
     *
     * @return defines position of this anchor in the sheet
     */
    public ClientAnchor getClientAnchor();
}