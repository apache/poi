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

package org.apache.poi.hwpf.usermodel;

import org.apache.poi.common.Duplicatable;
import org.apache.poi.hwpf.HWPFOldDocument;
import org.apache.poi.hwpf.model.SEPX;

public final class Section extends Range implements Duplicatable {
    private final SectionProperties _props;

    public Section(Section other) {
        super(other);
        _props  = other._props.copy();
    }

    public Section( SEPX sepx, Range parent ) {
        super( Math.max( parent._start, sepx.getStart() ), Math.min(parent._end, sepx.getEnd() ), parent );

        // XXX: temporary workaround for old Word95 document
        if ( parent.getDocument() instanceof HWPFOldDocument )
            _props = new SectionProperties();
        else
            _props = sepx.getSectionProperties();
    }

    @Override
    public Section copy() {
        return new Section(this);
    }

    /**
     * @return distance to be maintained between columns, in twips. Used when
     *         {@link #isColumnsEvenlySpaced()} == true
     */
    public int getDistanceBetweenColumns()
    {
        return _props.getDxaColumns();
    }

    public int getMarginBottom()
    {
        return _props.getDyaBottom();
    }

    public int getMarginLeft()
    {
        return _props.getDxaLeft();
    }

    public int getMarginRight()
    {
        return _props.getDxaRight();
    }

    public int getMarginTop()
    {
        return _props.getDyaTop();
    }

    public int getNumColumns()
    {
        return _props.getCcolM1() + 1;
    }

    /**
     * @return page height (in twips) in current section. Default value is 15840
     *         twips
     */
    public int getPageHeight()
    {
        return _props.getYaPage();
    }

    /**
     * @return page width (in twips) in current section. Default value is 12240
     *         twips
     */
    public int getPageWidth()
    {
        return _props.getXaPage();
    }

    /**
     * Set the height of the bottom margin in twips. In the AbstractWordUtils class, a constant
     * is defined that indicates how many twips there are per inch and it can be used in setting
     * the margins width a little like this;
     *
     * section.setMarginBottom( (int) 1.5 * AbstractWordUtils.TWIPS_PER_INCH );
     *
     * @param marginWidth A primitive int whose value will indciate how high the margin should
     *                be - in twips.
     */
    public void setMarginBottom(int marginWidth)
    {
        this._props.setDyaBottom(marginWidth);
    }

    /**
     * Set the width of the left hand margin in twips. In the AbstractWordUtils class, a constant
     * is defined that indicates how many twips there are per inch and it can be used in setting
     * the margins width a little like this;
     *
     * section.setMarginLeft( (int) 1.5 * AbstractWordUtils.TWIPS_PER_INCH );
     *
     * @param marginWidth A primitive int whose value will indciate how high the margin should
     *                be - in twips.
     */
    public void setMarginLeft(int marginWidth)
    {
        this._props.setDxaLeft(marginWidth);
    }

    /**
     * Set the width of the right hand margin in twips. In the AbstractWordUtils class, a constant
     * is defined that indicates how many twips there are per inch and it can be used in setting
     * the margins width a little like this;
     *
     * section.setMarginRight( (int) 1.5 * AbstractWordUtils.TWIPS_PER_INCH );
     *
     * @param marginWidth A primitive int whose value will indciate how high the margin should
     *                be - in twips.
     */
    public void setMarginRight(int marginWidth)
    {
        this._props.setDxaRight(marginWidth);
    }

    /**
     * Set the height of the top margin in twips. In the AbstractWordUtils class, a constant
     * is defined that indicates how many twips there are per inch and it can be used in setting
     * the margins width a little like this;
     *
     * section.setMarginTop( (int) 1.5 * AbstractWordUtils.TWIPS_PER_INCH );
     *
     * @param marginWidth A primitive int whose value will indciate how high the margin should
     *                be - in twips.
     */
    public void setMarginTop(int marginWidth)
    {
        this._props.setDyaTop(marginWidth);
    }

    public boolean isColumnsEvenlySpaced()
    {
        return _props.getFEvenlySpaced();
    }

    /**
     * Get the footnote restart qualifier
     *
     * <dl>
     * <dt>{@code 0x00}</dt><dd>If the numbering is continuous throughout the entire document</dd>
     * <dt>{@code 0x01}</dt><dd>If the numbering restarts at the beginning of this section</dd>
     * <dt>{@code 0x02}</dt><dd>If the numbering restarts on every page</dd>
     * </dl>
     *
     * @return an Rnc, as decribed above, specifying when and where footnote numbering restarts
     */
    public short getFootnoteRestartQualifier() {
        return _props.getRncFtn();
    }

    /**
     * @return an offset to be added to footnote numbers
     */
    public int getFootnoteNumberingOffset() {
        return _props.getNFtn();
    }

    /**
     * Get the numbering format of embedded footnotes
     *
     * <p>The full list of possible return values is given in [MS-OSHARED], v20140428, 2.2.1.3</p>
     *
     * @return an Nfc specifying the numbering format for footnotes
     */
    public int getFootnoteNumberingFormat() {
        return _props.getNfcFtnRef();
    }

    /**
     * Get the endnote restart qualifier
     *
     * <dl>
     * <dt>{@code 0x00}</dt><dd>If the numbering is continuous throughout the entire document</dd>
     * <dt>{@code 0x01}</dt><dd>If the numbering restarts at the beginning of this section</dd>
     * <dt>{@code 0x02}</dt><dd>If the numbering restarts on every page</dd>
     * </dl>
     *
     * @return an Rnc, as decribed above, specifying when and where endnote numbering restarts
     */
    public short getEndnoteRestartQualifier() {
        return _props.getRncEdn();
    }

    /**
     * @return an offset to be added to endnote numbers
     */
    public int getEndnoteNumberingOffset() {
        return _props.getNEdn();
    }

    /**
     * Get the numbering format of embedded endnotes
     *
     * <p>The full list of possible return values is given in [MS-OSHARED], v20140428, 2.2.1.3</p>
     *
     * @return an Nfc specifying the numbering format for endnotes
     */
    public int getEndnoteNumberingFormat() {
        return _props.getNfcEdnRef();
    }

    @Override
    public String toString()
    {
        return "Section [" + getStartOffset() + "; " + getEndOffset() + ")";
    }

    public int type()
    {
        return TYPE_SECTION;
    }
}
