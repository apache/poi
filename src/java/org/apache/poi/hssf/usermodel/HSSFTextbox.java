/* ====================================================================
   Copyright 2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.usermodel;

/**
 * A textbox is a shape that may hold a rich text string.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class HSSFTextbox
        extends HSSFSimpleShape
{
    public final static short       OBJECT_TYPE_TEXT               = 6;

    int marginLeft, marginRight, marginTop, marginBottom;

    HSSFRichTextString string = new HSSFRichTextString("");

    /**
     * Construct a new textbox with the given parent and anchor.
     * @param parent
     * @param anchor  One of HSSFClientAnchor or HSSFChildAnchor
     */
    public HSSFTextbox( HSSFShape parent, HSSFAnchor anchor )
    {
        super( parent, anchor );
        setShapeType(OBJECT_TYPE_TEXT);
    }

    /**
     * @return  the rich text string for this textbox.
     */
    public HSSFRichTextString getString()
    {
        return string;
    }

    /**
     * @param string    Sets the rich text string used by this object.
     */
    public void setString( HSSFRichTextString string )
    {
        this.string = string;
    }

    /**
     * @return  Returns the left margin within the textbox.
     */
    public int getMarginLeft()
    {
        return marginLeft;
    }

    /**
     * Sets the left margin within the textbox.
     */
    public void setMarginLeft( int marginLeft )
    {
        this.marginLeft = marginLeft;
    }

    /**
     * @return    returns the right margin within the textbox.
     */
    public int getMarginRight()
    {
        return marginRight;
    }

    /**
     * Sets the right margin within the textbox.
     */
    public void setMarginRight( int marginRight )
    {
        this.marginRight = marginRight;
    }

    /**
     * @return  returns the top margin within the textbox.
     */
    public int getMarginTop()
    {
        return marginTop;
    }

    /**
     * Sets the top margin within the textbox.
     */
    public void setMarginTop( int marginTop )
    {
        this.marginTop = marginTop;
    }

    /**
     * Gets the bottom margin within the textbox.
     */
    public int getMarginBottom()
    {
        return marginBottom;
    }

    /**
     * Sets the bottom margin within the textbox.
     */
    public void setMarginBottom( int marginBottom )
    {
        this.marginBottom = marginBottom;
    }
}
