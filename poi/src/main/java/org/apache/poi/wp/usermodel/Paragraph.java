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

package org.apache.poi.wp.usermodel;

/**
 * This class represents a paragraph, made up of one or more
 *  runs of text.
 */
public interface Paragraph {
    // Tables work very differently between the formats
//  public boolean isInTable();
//  public boolean isTableRowEnd();
//  public int getTableLevel();

    // TODO Implement justifaction in XWPF
//  public int getJustification();
//  public void setJustification(byte jc);

    // TODO Expose the different page break related things,
    //  XWPF currently doesn't have the full set
/*
    public boolean keepOnPage();
    public void setKeepOnPage(boolean fKeep);

    public boolean keepWithNext();
    public void setKeepWithNext(boolean fKeepFollow);

    public boolean pageBreakBefore();
    public void setPageBreakBefore(boolean fPageBreak);

    public boolean isSideBySide();
    public void setSideBySide(boolean fSideBySide);
*/

    public int getIndentFromRight();
    public void setIndentFromRight(int dxaRight);

    public int getIndentFromLeft();
    public void setIndentFromLeft(int dxaLeft);

    public int getFirstLineIndent();
    public void setFirstLineIndent(int first);

/*
    public boolean isLineNotNumbered();
    public void setLineNotNumbered(boolean fNoLnn);

    public boolean isAutoHyphenated();
    public void setAutoHyphenated(boolean autoHyph);

    public boolean isWidowControlled();
    public void setWidowControl(boolean widowControl);

    public int getSpacingBefore();
    public void setSpacingBefore(int before);

    public int getSpacingAfter();
    public void setSpacingAfter(int after);
*/

    //  public LineSpacingDescriptor getLineSpacing();
    //  public void setLineSpacing(LineSpacingDescriptor lspd);

    public int getFontAlignment();
    public void setFontAlignment(int align);

    public boolean isWordWrapped();
    public void setWordWrapped(boolean wrap);

/*
    public boolean isVertical();
    public void setVertical(boolean vertical);

    public boolean isBackward();
    public void setBackward(boolean bward);
*/

    // TODO Make the HWPF and XWPF interface wrappers compatible for these
/*
    public BorderCode getTopBorder();
    public void setTopBorder(BorderCode top);
    public BorderCode getLeftBorder();
    public void setLeftBorder(BorderCode left);
    public BorderCode getBottomBorder();
    public void setBottomBorder(BorderCode bottom);
    public BorderCode getRightBorder();
    public void setRightBorder(BorderCode right);
    public BorderCode getBarBorder();
    public void setBarBorder(BorderCode bar);

    public ShadingDescriptor getShading();
    public void setShading(ShadingDescriptor shd);
*/

    /**
     * Returns the ilfo, an index to the document's hpllfo, which
     *  describes the automatic number formatting of the paragraph.
     * A value of zero means it isn't numbered.
     */
//    public int getIlfo();

    /**
     * Returns the multi-level indent for the paragraph. Will be
     *  zero for non-list paragraphs, and the first level of any
     *  list. Subsequent levels in hold values 1-8.
     */
//    public int getIlvl();

    /**
     * Returns the heading level (1-8), or 9 if the paragraph
     *  isn't in a heading style.
     */
//    public int getLvl();

    /**
     * Returns number of tabs stops defined for paragraph. Must be >= 0 and <=
     * 64.
     * 
     * @return number of tabs stops defined for paragraph. Must be >= 0 and <=
     *         64
     */
//    public int getTabStopsNumber();

    /**
     * Returns array of positions of itbdMac tab stops
     * 
     * @return array of positions of itbdMac tab stops
     */
//    public int[] getTabStopsPositions();
}
