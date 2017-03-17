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

package org.apache.poi.xssf.binary;


import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.util.Internal;
import org.apache.poi.xssf.usermodel.XSSFComment;

/**
 * @since 3.16-beta3
 */
@Internal
class XSSFBComment extends XSSFComment {

    private final CellAddress cellAddress;
    private final String author;
    private final XSSFBRichTextString comment;
    private boolean visible = true;

    XSSFBComment(CellAddress cellAddress, String author, String comment) {
        super(null, null, null);
        this.cellAddress = cellAddress;
        this.author = author;
        this.comment = new XSSFBRichTextString(comment);
    }

    @Override
    public void setVisible(boolean visible) {
        throw new IllegalArgumentException("XSSFBComment is read only.");
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public CellAddress getAddress() {
        return cellAddress;
    }

    @Override
    public void setAddress(CellAddress addr) {
        throw new IllegalArgumentException("XSSFBComment is read only");
    }

    @Override
    public void setAddress(int row, int col) {
        throw new IllegalArgumentException("XSSFBComment is read only");

    }

    @Override
    public int getRow() {
        return cellAddress.getRow();
    }

    @Override
    public void setRow(int row) {
        throw new IllegalArgumentException("XSSFBComment is read only");
    }

    @Override
    public int getColumn() {
        return cellAddress.getColumn();
    }

    @Override
    public void setColumn(int col) {
        throw new IllegalArgumentException("XSSFBComment is read only");
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public void setAuthor(String author) {
        throw new IllegalArgumentException("XSSFBComment is read only");
    }

    @Override
    public XSSFBRichTextString getString() {
        return comment;
    }

    @Override
    public void setString(RichTextString string) {
        throw new IllegalArgumentException("XSSFBComment is read only");
    }

    @Override
    public ClientAnchor getClientAnchor() {
        return null;
    }
}
