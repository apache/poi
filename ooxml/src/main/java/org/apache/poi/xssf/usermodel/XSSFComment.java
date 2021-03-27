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

import static org.apache.poi.util.Units.EMU_PER_PIXEL;

import java.math.BigInteger;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.model.CommentsTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;

import com.microsoft.schemas.office.excel.CTClientData;
import com.microsoft.schemas.vml.CTShape;

public class XSSFComment implements Comment {
    
    private final CTComment _comment;
    private final CommentsTable _comments;
    private final CTShape _vmlShape;

    /**
     * cached reference to the string with the comment text
     */
    private XSSFRichTextString _str;

    /**
     * Creates a new XSSFComment, associated with a given
     *  low level comment object.
     */
    public XSSFComment(CommentsTable comments, CTComment comment, CTShape vmlShape) {
        _comment = comment;
        _comments = comments;
        _vmlShape = vmlShape;

        // we potentially need to adjust the column/row information in the shape
        // the same way as we do in setRow()/setColumn()
        if(comment != null && vmlShape != null && vmlShape.sizeOfClientDataArray() > 0) {
            CellReference ref = new CellReference(comment.getRef());
            CTClientData clientData = vmlShape.getClientDataArray(0);
            clientData.setRowArray(0, new BigInteger(String.valueOf(ref.getRow())));
            clientData.setColumnArray(0, new BigInteger(String.valueOf(ref.getCol())));
            
            avoidXmlbeansCorruptPointer(vmlShape);
        }
    }

    /**
     *
     * @return Name of the original comment author. Default value is blank.
     */
    @Override
    public String getAuthor() {
        return _comments.getAuthor(_comment.getAuthorId());
    }

    /**
     * Name of the original comment author. Default value is blank.
     *
     * @param author the name of the original author of the comment
     */
    @Override
    public void setAuthor(String author) {
        _comment.setAuthorId(_comments.findAuthor(author));
    }

    /**
     * @return the 0-based column of the cell that the comment is associated with.
     */
    @Override
    public int getColumn() {
        return getAddress().getColumn();
    }

    /**
     * @return the 0-based row index of the cell that the comment is associated with.
     */
    @Override
    public int getRow() {
        return getAddress().getRow();
    }

    /**
     * Returns whether this comment is visible.
     *
     * @return <code>true</code> if the comment is visible, <code>false</code> otherwise
     */
    @Override
    public boolean isVisible() {
        boolean visible = false;
        if(_vmlShape != null) {
            String style = _vmlShape.getStyle();
            visible = style != null && style.contains("visibility:visible");
        }
        return visible;
    }

    /**
     * Sets whether this comment is visible.
     *
     * @param visible <code>true</code> if the comment is visible, <code>false</code> otherwise
     */
    @Override
    public void setVisible(boolean visible) {
        if(_vmlShape != null){
            String style;
            if(visible) style = "position:absolute;visibility:visible";
            else style = "position:absolute;visibility:hidden";
            _vmlShape.setStyle(style);
        }
    }
    
    @Override
    public CellAddress getAddress() {
        return new CellAddress(_comment.getRef());
    }
    
    @Override
    public void setAddress(int row, int col) {
        setAddress(new CellAddress(row, col));
    }
    
    @Override
    public void setAddress(CellAddress address) {
        CellAddress oldRef = new CellAddress(_comment.getRef());
        if (address.equals(oldRef)) {
            // nothing to do
            return;
        }
        
        _comment.setRef(address.formatAsString());
        _comments.referenceUpdated(oldRef, _comment);
        
        if (_vmlShape != null) {
            CTClientData clientData = _vmlShape.getClientDataArray(0);
            clientData.setRowArray(0, new BigInteger(String.valueOf(address.getRow())));
            clientData.setColumnArray(0, new BigInteger(String.valueOf(address.getColumn())));
           
            avoidXmlbeansCorruptPointer(_vmlShape);
        }
    }

    /**
     * Set the column of the cell that contains the comment
     * 
     * If changing both row and column, use {@link #setAddress}.
     *
     * @param col the 0-based column of the cell that contains the comment
     */
    @Override
    public void setColumn(int col) {
        setAddress(getRow(), col);
    }

    /**
     * Set the row of the cell that contains the comment
     * 
     * If changing both row and column, use {@link #setAddress}.
     *
     * @param row the 0-based row of the cell that contains the comment
     */
    @Override
    public void setRow(int row) {
        setAddress(row, getColumn());
    }
    
    /**
     * @return the rich text string of the comment
     */
    @Override
    public XSSFRichTextString getString() {
        if(_str == null) {
            CTRst rst = _comment.getText();
            if(rst != null) _str = new XSSFRichTextString(_comment.getText());
        }
        return _str;
    }

    /**
     * Sets the rich text string used by this comment.
     *
     * @param string  the XSSFRichTextString used by this object.
     */
    @Override
    public void setString(RichTextString string) {
        if(!(string instanceof XSSFRichTextString)){
            throw new IllegalArgumentException("Only XSSFRichTextString argument is supported");
        }
        _str = (XSSFRichTextString)string;
        _comment.setText(_str.getCTRst());
    }
    
    public void setString(String string) {
        setString(new XSSFRichTextString(string));
    }

    @Override
    public ClientAnchor getClientAnchor() {
        if(_vmlShape == null) {
            return null;
        }
        String position = _vmlShape.getClientDataArray(0).getAnchorArray(0);
        int[] pos = new int[8];
        int i = 0;
        for (String s : position.split(",")) {
            pos[i++] = Integer.parseInt(s.trim());
        }
        return new XSSFClientAnchor(pos[1]*EMU_PER_PIXEL, pos[3]*EMU_PER_PIXEL, pos[5]*EMU_PER_PIXEL, pos[7]*EMU_PER_PIXEL, pos[0], pos[2], pos[4], pos[6]);
    }

    /**
     * @return the xml bean holding this comment's properties
     */
    protected CTComment getCTComment(){
        return _comment;
    }

    protected CTShape getCTShape(){
        return _vmlShape;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof XSSFComment)) {
            return false;
        }
        XSSFComment other = (XSSFComment) obj;
        return ((getCTComment() == other.getCTComment()) &&
                (getCTShape() == other.getCTShape())); 
    }

    @Override
    public int hashCode() {
        return ((getRow()*17) + getColumn())*31;
    }

    private static void avoidXmlbeansCorruptPointer(CTShape vmlShape) {
        // There is a very odd xmlbeans bug when changing the row
        //  arrays which can lead to corrupt pointer
        // This call seems to fix them again... See bug #50795
        //noinspection ResultOfMethodCallIgnored
        vmlShape.getClientDataList().toString();
    }
}
