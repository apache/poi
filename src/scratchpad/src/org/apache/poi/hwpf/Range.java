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
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Section;

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
import org.apache.poi.hwpf.sprm.CharacterSprmCompressor;
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
  boolean _sectionRangeFound;
  private List _sections;
  int _sectionStart;
  int _sectionEnd;
  boolean _parRangeFound;
  private List _paragraphs;
  int _parStart;
  int _parEnd;
  boolean _charRangeFound;
  private List _characters;
  int _charStart;
  int _charEnd;
  boolean _textRangeFound;
  private List _text;
  int _textStart;
  int _textEnd;


  protected Range(int start, int end, HWPFDocument doc)
  {
    _start = start;
    _end = end;
    _doc = doc;
    _sections = _doc.getSectionTable().getSections();
    _paragraphs = _doc.getParagraphTable().getParagraphs();
    _characters = _doc.getCharacterTable().getTextRuns();
    _text = _doc.getTextTable().getTextPieces();
  }

  protected Range(int start, int end, Range parent)
  {
    _start = start;
    _end = end;
    _doc = parent._doc;
    _sections = parent._sections;
    _paragraphs = parent._paragraphs;
    _characters = parent._characters;
    _text = parent._text;
  }

  public String text()
    throws UnsupportedEncodingException
  {
    initText();

    StringBuffer sb = new StringBuffer();
    int size = _text.size();
    for (int x = 0; x < size; x++)
    {
      TextPiece tp = (TextPiece)_text.get(x);
      StringBuffer pieceSb = (StringBuffer)tp.getCacheContents();
      if (pieceSb == null)
      {
        String encoding = "Cp1252";
        if (tp.usesUnicode())
        {
          encoding = "UTF-16LE";
        }
        String str = new String(tp.getBuf(), encoding);
        pieceSb = new StringBuffer(str);
        tp.fillCache(pieceSb);
      }
      int startIndex = Math.max(0, (tp.getStart() - _start));
      int endIndex = Math.min(tp.getEnd() - startIndex, _end - startIndex);
      sb.append(pieceSb.toString().substring(startIndex, endIndex));
    }
    return sb.toString();
  }

  public int numSections()
  {
    initSections();
    return _sections.size();
  }

  public int numParagraphs()
  {
    initParagraphs();
    return _paragraphs.size();
  }

  public int numCharacterRuns()
  {
    initCharacterRuns();
    return _characters.size();
  }

  public CharacterRange insertBefore(String text)
    throws UnsupportedEncodingException
  {
    initAll();

    TextPiece tp = (TextPiece)_text.get(_textStart);
    StringBuffer sb = (StringBuffer)tp.getStringBuffer();

    // Since this is the first item in our list, it is safe to assume that
    // _start >= tp.getStart()
    int insertIndex = _start - tp.getStart();
    sb.insert(insertIndex, text);
    int adjustedLength = _doc.getTextTable().adjustForInsert(_textStart, text.length());
    _doc.getCharacterTable().adjustForInsert(_textStart, adjustedLength);
    _doc.getParagraphTable().adjustForInsert(_textStart, adjustedLength);
    _doc.getSectionTable().adjustForInsert(_textStart, adjustedLength);
    return getCharacterRange(0);
  }

  public CharacterRange insertAfter(String text)
  {
    return null;
  }

  public CharacterRange insertBefore(String text, CharacterRun cr)
    throws UnsupportedEncodingException
  {
    initAll();
    PAPX papx = (PAPX)_paragraphs.get(_parStart);
    short istd = papx.getIstd();

    StyleSheet ss = _doc.getStyleSheet();
    CharacterRun baseStyle = ss.getCharacterStyle(istd);

    byte[] grpprl = CharacterSprmCompressor.compressCharacterProperty(cr, baseStyle);
    _doc.getCharacterTable().insert(_charStart, _start, grpprl);

    return insertBefore(text);
  }

  public CharacterRange insertAfter(String text, CharacterRun cr)
  {
    return null;
  }

  public ParagraphRange insertBefore(Paragraph paragraph)
  {
    return null;
  }

  public ParagraphRange insertAfter(Paragraph paragraph)
  {
    return null;
  }


  public CharacterRun getCharacterRun(int index)
  {
    initCharacterRuns();
    CHPX chpx = (CHPX)_characters.get(index + _charStart);
    CharacterRun chp = (CharacterRun)chpx.getCacheContents();
    if (chp == null)
    {
      int[] point = findRange(_paragraphs, _parStart, chpx.getStart(),
                              chpx.getEnd());
      List paragraphList = _paragraphs.subList(point[0], point[1]);
      PAPX papx = (PAPX)paragraphList.get(0);
      short istd = papx.getIstd();

      StyleSheet sd = _doc.getStyleSheet();
      CharacterRun baseStyle = sd.getCharacterStyle(istd);
      chp = CharacterSprmUncompressor.uncompressCHP(baseStyle, chpx.getBuf(), 0);
      chpx.fillCache(chp);
    }
    return chp;
  }

  public Section getSection(int index)
  {
    initSections();
    SEPX sepx = (SEPX)_sections.get(index + _sectionStart);
    Section sep = (Section)sepx.getCacheContents();
    if (sep == null)
    {
      sep = SectionSprmUncompressor.uncompressSEP(new Section(), sepx.getBuf(), 0);
      sepx.fillCache(sep);
    }
    return sep;
  }

  public Paragraph getParagraph(int index)
  {
    initParagraphs();
    PAPX papx = (PAPX)_paragraphs.get(index + _parStart);
    Paragraph pap = (Paragraph)papx.getCacheContents();
    if (pap == null)
    {
      short istd = LittleEndian.getShort(papx.getBuf());
      StyleSheet sd = _doc.getStyleSheet();
      Paragraph baseStyle = sd.getParagraphStyle(istd);
      pap = ParagraphSprmUncompressor.uncompressPAP(baseStyle, papx.getBuf(), 2);
      papx.fillCache(pap);
    }
    return pap;
  }

  public SectionRange getSectionRange(int index)
  {
    initSections();
    PropertyNode node = (PropertyNode)_sections.get(index + _sectionStart);
    return new SectionRange(Math.max(_start, node.getStart()),
                            Math.min(_end, node.getEnd()), this);
  }

  public ParagraphRange getParagraphRange(int index)
  {
    initParagraphs();
    PropertyNode node = (PropertyNode)_paragraphs.get(index + _parStart);
    return new ParagraphRange(Math.max(_start, node.getStart()),
                            Math.min(_end, node.getEnd()),this);
  }

  public CharacterRange getCharacterRange(int index)
  {
    initCharacterRuns();
    PropertyNode node = (PropertyNode)_characters.get(index + _charStart);
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

  private void initAll()
  {
    initText();
    initCharacterRuns();
    initParagraphs();
    initSections();
  }


  private void initParagraphs()
  {
    if (!_parRangeFound)
    {
      int[] point = findRange(_paragraphs, _parStart, _start, _end);
      _parStart = point[0];
      _parEnd = point[1];
      _parRangeFound = true;
    }
  }

  private void initCharacterRuns()
  {
    if (!_charRangeFound)
    {
      int[] point = findRange(_characters, _charStart, _start, _end);
      _charStart = point[0];
      _charEnd = point[1];
      _charRangeFound = true;
    }
  }

  private void initText()
  {
    if (!_textRangeFound)
    {
      int[] point = findRange(_text, _textStart, _start, _end);
      _textStart = point[0];
      _textEnd = point[1];
      _textRangeFound = true;
    }
  }

  private void initSections()
  {
    if (!_sectionRangeFound)
    {
      int[] point = findRange(_sections, _sectionStart, _start, _end);
      _sectionStart = point[0];
      _sectionEnd = point[1];
      _sectionRangeFound = true;
    }
  }

  private int[] findRange(List rpl, int min, int start, int end)
  {
    int x = min;
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
    return new int[]{x, y + 1};
  }
}
