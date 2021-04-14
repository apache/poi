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

import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.common.Duplicatable;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.model.LFO;
import org.apache.poi.hwpf.model.ListLevel;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.model.PAPX;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.sprm.ParagraphSprmUncompressor;
import org.apache.poi.hwpf.sprm.SprmBuffer;
import org.apache.poi.hwpf.sprm.TableSprmCompressor;
import org.apache.poi.util.Internal;

import static org.apache.logging.log4j.util.Unbox.box;

public class Paragraph extends Range implements Duplicatable {
    private static final Logger LOGGER = LogManager.getLogger(Paragraph.class);

    public static final short SPRM_JC = 0x2403;
    public static final short SPRM_FSIDEBYSIDE = 0x2404;
    public static final short SPRM_FKEEP = 0x2405;
    public static final short SPRM_FKEEPFOLLOW = 0x2406;
    public static final short SPRM_FPAGEBREAKBEFORE = 0x2407;
    public static final short SPRM_BRCL = 0x2408;
    public static final short SPRM_BRCP = 0x2409;
    public static final short SPRM_ILVL = 0x260A;
    public static final short SPRM_ILFO = 0x460B;
    public static final short SPRM_FNOLINENUMB = 0x240C;
    public static final short SPRM_CHGTABSPAPX = (short)0xC60D;
    public static final short SPRM_DXARIGHT = (short)0x840E;
    public static final short SPRM_DXALEFT = (short)0x840F;
    public static final short SPRM_DXALEFT1 = (short)0x8411;
    public static final short SPRM_DYALINE = 0x6412;
    public static final short SPRM_DYABEFORE = (short)0xA413;
    public static final short SPRM_DYAAFTER = (short)0xA414;
    public static final short SPRM_CHGTABS = (short)0xC615;
    public static final short SPRM_FINTABLE = 0x2416;
    public static final short SPRM_FTTP = 0x2417;
    public static final short SPRM_DXAABS = (short)0x8418;
    public static final short SPRM_DYAABS = (short)0x8419;
    public static final short SPRM_DXAWIDTH = (short)0x841A;
    public static final short SPRM_PC = 0x261B;
    public static final short SPRM_WR = 0x2423;
    public static final short SPRM_BRCTOP = 0x6424;
    public static final short SPRM_BRCLEFT = 0x6425;
    public static final short SPRM_BRCBOTTOM = 0x6426;
    public static final short SPRM_BRCRIGHT = 0x6427;
    public static final short SPRM_BRCBAR = 0x6629;
    public static final short SPRM_FNOAUTOHYPH = 0x242A;
    public static final short SPRM_WHEIGHTABS = 0x442B;
    public static final short SPRM_DCS = 0x442C;
    public static final short SPRM_SHD80 = 0x442D;
    public static final short SPRM_SHD = (short)0xC64D;
    public static final short SPRM_DYAFROMTEXT = (short)0x842E;
    public static final short SPRM_DXAFROMTEXT = (short)0x842F;
    public static final short SPRM_FLOCKED = 0x2430;
    public static final short SPRM_FWIDOWCONTROL = 0x2431;
    public static final short SPRM_RULER = (short)0xC632;
    public static final short SPRM_FKINSOKU = 0x2433;
    public static final short SPRM_FWORDWRAP = 0x2434;
    public static final short SPRM_FOVERFLOWPUNCT = 0x2435;
    public static final short SPRM_FTOPLINEPUNCT = 0x2436;
    public static final short SPRM_AUTOSPACEDE = 0x2437;
    public static final short SPRM_AUTOSPACEDN = 0x2438;
    public static final short SPRM_WALIGNFONT = 0x4439;
    public static final short SPRM_FRAMETEXTFLOW = 0x443A;
    public static final short SPRM_ANLD = (short)0xC63E;
    public static final short SPRM_PROPRMARK = (short)0xC63F;
    public static final short SPRM_OUTLVL = 0x2640;
    public static final short SPRM_FBIDI = 0x2441;
    public static final short SPRM_FNUMRMLNS = 0x2443;
    public static final short SPRM_CRLF = 0x2444;
    public static final short SPRM_NUMRM = (short)0xC645;
    public static final short SPRM_USEPGSUSETTINGS = 0x2447;
    public static final short SPRM_FADJUSTRIGHT = 0x2448;

    @Internal
    public static Paragraph newParagraph( Range parent, PAPX papx )
    {
        HWPFDocumentCore doc = parent._doc;
        ListTables listTables = doc.getListTables();
        StyleSheet styleSheet = doc.getStyleSheet();

        ParagraphProperties properties = new ParagraphProperties();
        properties.setIstd( papx.getIstd() );

        properties = newParagraph_applyStyleProperties( styleSheet, papx,
                properties );
        properties = ParagraphSprmUncompressor.uncompressPAP( properties,
                papx.getGrpprl(), 2 );

        if ( properties.getIlfo() != 0 && listTables != null )
        {
            LFO lfo = null;
            try
            {
                lfo = listTables.getLfo( properties.getIlfo() );
            }
            catch ( NoSuchElementException exc )
            {
                LOGGER.atWarn().log("Paragraph refers to LFO #{} that does not exists", box(properties.getIlfo()));
            }
            if ( lfo != null )
            {
                final ListLevel listLevel = listTables.getLevel( lfo.getLsid(),
                        properties.getIlvl() );

                if ( listLevel != null && listLevel.getGrpprlPapx() != null )
                {
                    properties = ParagraphSprmUncompressor.uncompressPAP(
                            properties, listLevel.getGrpprlPapx(), 0 );
                    // reapply style and local PAPX properties
                    properties = newParagraph_applyStyleProperties( styleSheet,
                            papx, properties );
                    properties = ParagraphSprmUncompressor.uncompressPAP(
                            properties, papx.getGrpprl(), 2 );
                }
            }
        }

        if ( properties.getIlfo() > 0 )
            return new ListEntry( papx, properties, parent );

        return new Paragraph( papx, properties, parent );
    }

    protected static ParagraphProperties newParagraph_applyStyleProperties(
            StyleSheet styleSheet, PAPX papx, ParagraphProperties properties )
    {
        if ( styleSheet == null )
            return properties;

        int style = papx.getIstd();
        byte[] grpprl = styleSheet.getPAPX( style );
        return ParagraphSprmUncompressor.uncompressPAP( properties, grpprl, 2 );
    }

  protected short _istd;
  protected ParagraphProperties _props;
  protected SprmBuffer _papx;

    @Internal
    Paragraph( PAPX papx, ParagraphProperties properties, Range parent )
    {
        super( Math.max( parent._start, papx.getStart() ), Math.min(
                parent._end, papx.getEnd() ), parent );
        _props = properties;
        _papx = papx.getSprmBuf();
        _istd = papx.getIstd();
    }

    Paragraph(Paragraph other) {
        super(other);
        _istd = other._istd;
        _props = (other._props == null) ? null : other._props.copy();
        _papx = (other._papx == null) ? null : other._papx.copy();
    }

  /**
   * Returns the index of the style which applies to this
   *  Paragraph. Details of the style can be looked up
   *  from the {@link StyleSheet}, via
   *  {@link StyleSheet#getStyleDescription(int)}
   */
  public short getStyleIndex()
  {
     return _istd;
  }

  public boolean isInTable()
  {
    return _props.getFInTable();
  }

  /**
   * @return {@code true}, if table trailer paragraph (last in table row),
   *         {@code false} otherwise
   */
  public boolean isTableRowEnd()
  {
    return _props.getFTtp() || _props.getFTtpEmbedded();
  }

  public int getTableLevel()
  {
    return _props.getItap();
  }

    /**
     * @return {@code true}, if the end of paragraph mark is really an end of
     *         cell mark for a nested table cell, {@code false} otherwise
     */
    public boolean isEmbeddedCellMark()
    {
        return _props.getFInnerTableCell();
    }

  public int getJustification()
  {
    return _props.getJc();
  }

  public void setJustification(byte jc)
  {
    _props.setJc(jc);
    _papx.updateSprm(SPRM_JC, jc);
  }

  public boolean keepOnPage()
  {
    return _props.getFKeep();
  }

  public void setKeepOnPage(boolean fKeep)
  {
    _props.setFKeep(fKeep);
    _papx.updateSprm(SPRM_FKEEP, fKeep);
  }

  public boolean keepWithNext()
  {
    return _props.getFKeepFollow();
  }

  public void setKeepWithNext(boolean fKeepFollow)
  {
    _props.setFKeepFollow(fKeepFollow);
    _papx.updateSprm(SPRM_FKEEPFOLLOW, fKeepFollow);
  }

  public boolean pageBreakBefore()
  {
    return _props.getFPageBreakBefore();
  }

  public void setPageBreakBefore(boolean fPageBreak)
  {
    _props.setFPageBreakBefore(fPageBreak);
    _papx.updateSprm(SPRM_FPAGEBREAKBEFORE, fPageBreak);
  }

  public boolean isLineNotNumbered()
  {
    return _props.getFNoLnn();
  }

  public void setLineNotNumbered(boolean fNoLnn)
  {
    _props.setFNoLnn(fNoLnn);
    _papx.updateSprm(SPRM_FNOLINENUMB, fNoLnn);
  }

  public boolean isSideBySide()
  {
    return _props.getFSideBySide();
  }

  public void setSideBySide(boolean fSideBySide)
  {
    _props.setFSideBySide(fSideBySide);
    _papx.updateSprm(SPRM_FSIDEBYSIDE, fSideBySide);
  }

  public boolean isAutoHyphenated()
  {
    return !_props.getFNoAutoHyph();
  }

  public void setAutoHyphenated(boolean autoHyph)
  {
    _props.setFNoAutoHyph(!autoHyph);
    _papx.updateSprm(SPRM_FNOAUTOHYPH, !autoHyph);
  }

  public boolean isWidowControlled()
  {
    return _props.getFWidowControl();
  }

  public void setWidowControl(boolean widowControl)
  {
    _props.setFWidowControl(widowControl);
    _papx.updateSprm(SPRM_FWIDOWCONTROL, widowControl);
  }

  public int getIndentFromRight()
  {
    return _props.getDxaRight();
  }

  public void setIndentFromRight(int dxaRight)
  {
    _props.setDxaRight(dxaRight);
    _papx.updateSprm(SPRM_DXARIGHT, (short)dxaRight);
  }

  public int getIndentFromLeft()
  {
    return _props.getDxaLeft();
  }

  public void setIndentFromLeft(int dxaLeft)
  {
    _props.setDxaLeft(dxaLeft);
    _papx.updateSprm(SPRM_DXALEFT, (short)dxaLeft);
  }

  public int getFirstLineIndent()
  {
    return _props.getDxaLeft1();
  }

  public void setFirstLineIndent(int first)
  {
    _props.setDxaLeft1(first);
    _papx.updateSprm(SPRM_DXALEFT1, (short)first);
  }

  public LineSpacingDescriptor getLineSpacing()
  {
    return _props.getLspd();
  }

  public void setLineSpacing(LineSpacingDescriptor lspd)
  {
    _props.setLspd(lspd);
    _papx.updateSprm(SPRM_DYALINE, lspd.toInt());
  }

  public int getSpacingBefore()
  {
    return _props.getDyaBefore();
  }

  public void setSpacingBefore(int before)
  {
    _props.setDyaBefore(before);
    _papx.updateSprm(SPRM_DYABEFORE, (short)before);
  }

  public int getSpacingAfter()
  {
    return _props.getDyaAfter();
  }

  public void setSpacingAfter(int after)
  {
    _props.setDyaAfter(after);
    _papx.updateSprm(SPRM_DYAAFTER, (short)after);
  }

  public boolean isKinsoku()
  {
    return _props.getFKinsoku();
  }

  public void setKinsoku(boolean kinsoku)
  {
    _props.setFKinsoku(kinsoku);
    _papx.updateSprm(SPRM_FKINSOKU, kinsoku);
  }

  public boolean isWordWrapped()
  {
    return _props.getFWordWrap();
  }

  public void setWordWrapped(boolean wrap)
  {
    _props.setFWordWrap(wrap);
    _papx.updateSprm(SPRM_FWORDWRAP, wrap);
  }

  public int getFontAlignment()
  {
    return _props.getWAlignFont();
  }

  public void setFontAlignment(int align)
  {
    _props.setWAlignFont(align);
    _papx.updateSprm(SPRM_WALIGNFONT, (short)align);
  }

  public boolean isVertical()
  {
    return _props.isFVertical();
  }

  public void setVertical(boolean vertical)
  {
    _props.setFVertical(vertical);
    _papx.updateSprm(SPRM_FRAMETEXTFLOW, getFrameTextFlow());
  }

  public boolean isBackward()
  {
    return _props.isFBackward();
  }

  public void setBackward(boolean bward)
  {
    _props.setFBackward(bward);
    _papx.updateSprm(SPRM_FRAMETEXTFLOW, getFrameTextFlow());
  }

  public BorderCode getTopBorder()
  {
    return _props.getBrcTop();
  }

  public void setTopBorder(BorderCode top)
  {
    _props.setBrcTop(top);
    _papx.updateSprm(SPRM_BRCTOP, top.toInt());
  }

  public BorderCode getLeftBorder()
  {
    return _props.getBrcLeft();
  }

  public void setLeftBorder(BorderCode left)
  {
    _props.setBrcLeft(left);
    _papx.updateSprm(SPRM_BRCLEFT, left.toInt());
  }

  public BorderCode getBottomBorder()
  {
    return _props.getBrcBottom();
  }

  public void setBottomBorder(BorderCode bottom)
  {
    _props.setBrcBottom(bottom);
    _papx.updateSprm(SPRM_BRCBOTTOM, bottom.toInt());
  }

  public BorderCode getRightBorder()
  {
    return _props.getBrcRight();
  }

  public void setRightBorder(BorderCode right)
  {
    _props.setBrcRight(right);
    _papx.updateSprm(SPRM_BRCRIGHT, right.toInt());
  }

  public BorderCode getBarBorder()
  {
    return _props.getBrcBar();
  }

  public void setBarBorder(BorderCode bar)
  {
    _props.setBrcBar(bar);
    _papx.updateSprm(SPRM_BRCBAR, bar.toInt());
  }

  public ShadingDescriptor getShading()
  {
    return _props.getShd();
  }

  public void setShading(ShadingDescriptor shd)
  {
    _props.setShd(shd);
    //TODO: remove old one
    _papx.addSprm( SPRM_SHD, shd.serialize() );
  }

  public DropCapSpecifier getDropCap()
  {
    return _props.getDcs();
  }

  public void setDropCap(DropCapSpecifier dcs)
  {
    _props.setDcs(dcs);
    _papx.updateSprm(SPRM_DCS, dcs.toShort());
  }

  /**
   * Returns the ilfo, an index to the document's hpllfo, which
   *  describes the automatic number formatting of the paragraph.
   * A value of zero means it isn't numbered.
   */
  public int getIlfo()
  {
     return _props.getIlfo();
  }

  /**
   * Returns the multi-level indent for the paragraph. Will be
   *  zero for non-list paragraphs, and the first level of any
   *  list. Subsequent levels in hold values 1-8.
   */
  public int getIlvl()
  {
     return _props.getIlvl();
  }

  /**
   * Returns the heading level (1-8), or 9 if the paragraph
   *  isn't in a heading style.
   */
  public int getLvl()
  {
     return _props.getLvl();
  }

  void setTableRowEnd(TableProperties props)
  {
    setTableRowEnd(true);
    byte[] grpprl = TableSprmCompressor.compressTableProperty(props);
    _papx.append(grpprl);
  }

  private void setTableRowEnd(boolean val)
  {
    _props.setFTtp(val);
    _papx.updateSprm(SPRM_FTTP, val);
  }

    /**
     * Returns number of tabs stops defined for paragraph. Must be &gt;= 0 and &lt;= 64.
     *
     * @return number of tabs stops defined for paragraph. Must be &gt;= 0 and &lt;= 64
     */
    public int getTabStopsNumber()
    {
        return _props.getItbdMac();
    }

    /**
     * Returns array of positions of itbdMac tab stops
     *
     * @return array of positions of itbdMac tab stops
     */
    public int[] getTabStopsPositions()
    {
        return _props.getRgdxaTab();
    }

    public HWPFList getList()
    {
        if ( getIlfo() == 0x000 || getIlfo() == 0xF801 )
        {
            throw new IllegalStateException( "Paragraph not in list" );
        }
        return new HWPFList( getDocument().getStyleSheet(),
                getDocument().getListTables(), getIlfo() );
    }

    public boolean isInList()
    {
        return getIlfo() != 0x000 && getIlfo() != 0xF801;
    }

    /**
     * Clone the ParagraphProperties object associated with this
     *  Paragraph, so that you can apply the same properties to
     *  another Paragraph.
     */
    public ParagraphProperties cloneProperties() {
        return _props.copy();
    }

    @Override
    public Paragraph copy() {
        return new Paragraph(this);
    }

    private short getFrameTextFlow()
    {
        short retVal = 0;
        if (_props.isFVertical())
        {
            retVal |= 1;
        }
        if (_props.isFBackward())
        {
            retVal |= 2;
        }
        if (_props.isFRotateFont())
        {
            retVal |= 4;
        }
        return retVal;
    }

    @Override
    public String toString()
    {
        return "Paragraph [" + getStartOffset() + "; " + getEndOffset() + ")";
    }

    @Internal
    public ParagraphProperties getProps() {
        return _props;
    }
}
