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
 * An abstract shape.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public abstract class HSSFShape
{
    public static final int LINEWIDTH_ONE_PT = 12700;
    public static final int LINEWIDTH_DEFAULT = 9525;

    public static final int LINESTYLE_SOLID = 0;              // Solid (continuous) pen
    public static final int LINESTYLE_DASHSYS = 1;            // PS_DASH system   dash style
    public static final int LINESTYLE_DOTSYS = 2;             // PS_DOT system   dash style
    public static final int LINESTYLE_DASHDOTSYS = 3;         // PS_DASHDOT system dash style
    public static final int LINESTYLE_DASHDOTDOTSYS = 4;      // PS_DASHDOTDOT system dash style
    public static final int LINESTYLE_DOTGEL = 5;             // square dot style
    public static final int LINESTYLE_DASHGEL = 6;            // dash style
    public static final int LINESTYLE_LONGDASHGEL = 7;        // long dash style
    public static final int LINESTYLE_DASHDOTGEL = 8;         // dash short dash
    public static final int LINESTYLE_LONGDASHDOTGEL = 9;     // long dash short dash
    public static final int LINESTYLE_LONGDASHDOTDOTGEL = 10; // long dash short dash short dash
    public static final int LINESTYLE_NONE = -1;

    HSSFShape parent;
    HSSFAnchor anchor;
    int lineStyleColor = 0x08000040;
    int fillColor = 0x08000009;
    int lineWidth = LINEWIDTH_DEFAULT;    // 12700 = 1pt
    int lineStyle = LINESTYLE_SOLID;
    boolean noFill = false;

    /**
     * Create a new shape with the specified parent and anchor.
     */
    HSSFShape( HSSFShape parent, HSSFAnchor anchor )
    {
        this.parent = parent;
        this.anchor = anchor;
    }

    /**
     * Gets the parent shape.
     */
    public HSSFShape getParent()
    {
        return parent;
    }

    /**
     * @return  the anchor that is used by this shape.
     */
    public HSSFAnchor getAnchor()
    {
        return anchor;
    }

    /**
     * Sets a particular anchor.  A top-level shape must have an anchor of
     * HSSFClientAnchor.  A child anchor must have an anchor of HSSFChildAnchor
     *
     * @param anchor    the anchor to use.
     * @throws IllegalArgumentException     when the wrong anchor is used for
     *                                      this particular shape.
     *
     * @see HSSFChildAnchor
     * @see HSSFClientAnchor
     */
    public void setAnchor( HSSFAnchor anchor )
    {
        if ( parent == null )
        {
            if ( anchor instanceof HSSFChildAnchor )
                throw new IllegalArgumentException( "Must use client anchors for shapes directly attached to sheet." );
        }
        else
        {
            if ( anchor instanceof HSSFClientAnchor )
                throw new IllegalArgumentException( "Must use child anchors for shapes attached to groups." );
        }

        this.anchor = anchor;
    }

    /**
     * The color applied to the lines of this shape.
     */
    public int getLineStyleColor()
    {
        return lineStyleColor;
    }

    /**
     * The color applied to the lines of this shape.
     */
    public void setLineStyleColor( int lineStyleColor )
    {
        this.lineStyleColor = lineStyleColor;
    }

    /**
     * The color applied to the lines of this shape.
     */
    public void setLineStyleColor( int red, int green, int blue )
    {
        this.lineStyleColor = ((blue) << 16) | ((green) << 8) | red;
    }

    /**
     * The color used to fill this shape.
     */
    public int getFillColor()
    {
        return fillColor;
    }

    /**
     * The color used to fill this shape.
     */
    public void setFillColor( int fillColor )
    {
        this.fillColor = fillColor;
    }

    /**
     * The color used to fill this shape.
     */
    public void setFillColor( int red, int green, int blue )
    {
        this.fillColor = ((blue) << 16) | ((green) << 8) | red;
    }

    /**
     * @return  returns with width of the line in EMUs.  12700 = 1 pt.
     */
    public int getLineWidth()
    {
        return lineWidth;
    }

    /**
     * Sets the width of the line.  12700 = 1 pt.
     *
     * @param lineWidth width in EMU's.  12700EMU's = 1 pt
     *
     * @see HSSFShape#LINEWIDTH_ONE_PT
     */
    public void setLineWidth( int lineWidth )
    {
        this.lineWidth = lineWidth;
    }

    /**
     * @return One of the constants in LINESTYLE_*
     */
    public int getLineStyle()
    {
        return lineStyle;
    }

    /**
     * Sets the line style.
     *
     * @param lineStyle     One of the constants in LINESTYLE_*
     */
    public void setLineStyle( int lineStyle )
    {
        this.lineStyle = lineStyle;
    }

    /**
     * @return  true if this shape is not filled with a color.
     */
    public boolean isNoFill()
    {
        return noFill;
    }

    /**
     * Sets whether this shape is filled or transparent.
     */
    public void setNoFill( boolean noFill )
    {
        this.noFill = noFill;
    }

    /**
     * Count of all children and their childrens children.
     */
    public int countOfAllChildren()
    {
        return 1;
    }
}
