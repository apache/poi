/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hwpf;


import org.apache.poi.util.LittleEndian;

import org.apache.poi.hwpf.usermodel.SectionRange;
import org.apache.poi.hwpf.usermodel.CharacterRange;
import org.apache.poi.hwpf.usermodel.ParagraphRange;
import org.apache.poi.hwpf.usermodel.CharacterProperties;
import org.apache.poi.hwpf.usermodel.ParagraphProperties;
import org.apache.poi.hwpf.usermodel.SectionProperties;

import org.apache.poi.hwpf.model.hdftypes.PropertyNode;
import org.apache.poi.hwpf.model.hdftypes.StyleSheet;
import org.apache.poi.hwpf.model.hdftypes.StyleDescription;
import org.apache.poi.hwpf.model.hdftypes.CHPBinTable;
import org.apache.poi.hwpf.model.hdftypes.CHPX;
import org.apache.poi.hwpf.model.hdftypes.PAPX;
import org.apache.poi.hwpf.model.hdftypes.SEPX;
import org.apache.poi.hwpf.model.hdftypes.PAPBinTable;
import org.apache.poi.hwpf.model.hdftypes.SectionTable;
import org.apache.poi.hwpf.model.hdftypes.TextPieceTable;
import org.apache.poi.hwpf.model.hdftypes.TextPiece;

import org.apache.poi.hwpf.sprm.CharacterSprmUncompressor;
import org.apache.poi.hwpf.sprm.SectionSprmUncompressor;
import org.apache.poi.hwpf.sprm.ParagraphSprmUncompressor;


import java.util.List;
import java.util.ArrayList;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;

public class Range
{
  private int _start;
  private int _end;
  private HWPFDocument _doc;
  private List _parentSections;
  private List _parentParagraphs;
  private List _parentCharacters;
  private List _parentText;
  private List _sections;
  private List _paragraphs;
  private List _characters;
  private List _text;


  protected Range(int start, int end, HWPFDocument doc)
  {
    _start = start;
    _end = end;
    _doc = doc;
    _parentSections = _doc.getSectionTable().getSections();
    _parentParagraphs = _doc.getParagraphTable().getParagraphs();
    _parentCharacters = _doc.getCharacterTable().getTextRuns();
    _parentText = _doc.getTextTable().getTextPieces();
  }

  protected Range(int start, int end, Range parent)
  {
    _start = start;
    _end = end;
    _doc = parent._doc;
    _parentSections = parent._parentSections;
    _parentParagraphs = parent._parentParagraphs;
    _parentCharacters = parent._parentCharacters;
    _parentText = parent._parentText;
  }

  public String text()
    throws UnsupportedEncodingException
  {
    if (_text == null)
    {
      _text = initRangedList(_parentText, _start, _end);
    }

    StringBuffer sb = new StringBuffer();
    int size = _text.size();
    for (int x = 0; x < size; x++)
    {
      TextPiece tp = (TextPiece)_text.get(x);
      String encoding = "Cp1252";
      if (tp.usesUnicode())
      {
        encoding = "UTF-16LE";
      }
      String str = new String (tp.getBuf(), Math.max(_start, tp.getStart()), Math.min(_end, tp.getEnd()), encoding);
      sb.append(str);
    }
    return sb.toString();
  }

  public int numSections()
  {
    if (_sections == null)
    {
      _sections = initRangedList(_parentSections, _start, _end);
    }
    return _sections.size();
  }

  public int numParagraphs()
  {
    if (_paragraphs == null)
    {
      _paragraphs = initRangedList(_parentParagraphs, _start, _end);
    }
    return _paragraphs.size();
  }

  public int numCharacterRuns()
  {
    if (_characters == null)
    {
      _characters = initRangedList(_parentCharacters, _start, _end);
    }
    return _characters.size();
  }

  public CharacterProperties getCharacterRun(int index)
  {
    CHPX chpx = (CHPX)_characters.get(index);
    CharacterProperties chp = (CharacterProperties)chpx.getCacheContents();
    if (chp == null)
    {
      List paragraphList = initRangedList(_paragraphs, chpx.getStart(),
                                          chpx.getEnd());
      PAPX papx = (PAPX)paragraphList.get(0);
      short istd = LittleEndian.getShort(papx.getBuf());

      StyleSheet sd = _doc.getStyleSheet();
      CharacterProperties baseStyle = sd.getCharacterStyle(istd);
      chp = CharacterSprmUncompressor.uncompressCHP(baseStyle, chpx.getBuf(), 0);
      chpx.fillCache(chp);
    }
    return chp;
  }

  public SectionProperties getSection(int index)
  {
    SEPX sepx = (SEPX)_sections.get(index);
    SectionProperties sep = (SectionProperties)sepx.getCacheContents();
    if (sep == null)
    {
      sep = SectionSprmUncompressor.uncompressSEP(new SectionProperties(), sepx.getBuf(), 0);
      sepx.fillCache(sep);
    }
    return sep;
  }

  public ParagraphProperties getParagraph(int index)
  {
    PAPX papx = (PAPX)_sections.get(index);
    ParagraphProperties pap = (ParagraphProperties)papx.getCacheContents();
    if (pap == null)
    {
      short istd = LittleEndian.getShort(papx.getBuf());
      StyleSheet sd = _doc.getStyleSheet();
      ParagraphProperties baseStyle = sd.getParagraphStyle(istd);
      pap = ParagraphSprmUncompressor.uncompressPAP(baseStyle, papx.getBuf(), 2);
      papx.fillCache(pap);
    }
    return pap;
  }

  public SectionRange getSectionRange(int index)
  {
    if (_sections == null)
    {
      _sections = initRangedList(_doc.getSectionTable().getSections(), _start, _end);
    }
    PropertyNode node = (PropertyNode)_sections.get(index);
    return new SectionRange(Math.max(_start, node.getStart()),
                            Math.min(_end, node.getEnd()), this);
  }

  public ParagraphRange getParagraphRange(int index)
  {
    if (_paragraphs == null)
    {
      _paragraphs = initRangedList(_doc.getParagraphTable().getParagraphs(), _start, _end);
    }
    PropertyNode node = (PropertyNode)_paragraphs.get(index);
    return new ParagraphRange(Math.max(_start, node.getStart()),
                            Math.min(_end, node.getEnd()),this);

  }

  public CharacterRange getCharacterRange(int index)
  {
    if (_characters == null)
    {
      _characters = initRangedList(_doc.getCharacterTable().getTextRuns(), _start, _end);
    }
    PropertyNode node = (PropertyNode)_characters.get(index);
    return new CharacterRange(Math.max(_start, node.getStart()),
                            Math.min(_end, node.getEnd()), this);

  }

  public SectionRange sections()
  {
    return new SectionRange(_start, _end, _doc);
  }
  public ParagraphRange paragraphs()
  {
    return new ParagraphRange(_start, _end, _doc);
  }

  public CharacterRange characterRuns()
  {
    return new CharacterRange(_start, _end, _doc);
  }

  private List initRangedList(List rpl, int start, int end)
  {
    int x = 0;
    PropertyNode node = (PropertyNode)rpl.get(x);
    while(node.getStart() < start)
    {
      x++;
      node = (PropertyNode)rpl.get(x);
    }

    int y = x;
    node = (PropertyNode)rpl.get(y);
    while(node.getEnd() > end)
    {
      y++;
      node = (PropertyNode)rpl.get(y);
    }
    return rpl.subList(x, y + 1);
  }
}
