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

package org.apache.poi.hwpf.usermodel;


import org.apache.poi.util.LittleEndian;

import org.apache.poi.hwpf.HWPFDocument;

import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.ParagraphProperties;
import org.apache.poi.hwpf.usermodel.Section;

import org.apache.poi.hwpf.model.PropertyNode;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.model.StyleDescription;
import org.apache.poi.hwpf.model.CHPBinTable;
import org.apache.poi.hwpf.model.CHPX;
import org.apache.poi.hwpf.model.PAPX;
import org.apache.poi.hwpf.model.SEPX;
import org.apache.poi.hwpf.model.PAPBinTable;
import org.apache.poi.hwpf.model.SectionTable;
import org.apache.poi.hwpf.model.TextPieceTable;
import org.apache.poi.hwpf.model.TextPiece;
import org.apache.poi.hwpf.model.ListTables;

import org.apache.poi.hwpf.sprm.CharacterSprmUncompressor;
import org.apache.poi.hwpf.sprm.CharacterSprmCompressor;
import org.apache.poi.hwpf.sprm.SectionSprmUncompressor;
import org.apache.poi.hwpf.sprm.ParagraphSprmUncompressor;
import org.apache.poi.hwpf.sprm.ParagraphSprmCompressor;
import org.apache.poi.hwpf.sprm.SprmBuffer;


import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

public class Range
{

  public static final int TYPE_PARAGRAPH = 0;
  public static final int TYPE_CHARACTER= 1;
  public static final int TYPE_SECTION = 2;
  public static final int TYPE_TEXT = 3;
  public static final int TYPE_LISTENTRY = 4;
  public static final int TYPE_TABLE = 5;
  public static final int TYPE_UNDEFINED = 6;

  private WeakReference _parent;
  protected int _start;
  protected int _end;
  protected HWPFDocument _doc;
  boolean _sectionRangeFound;
  protected List _sections;
  int _sectionStart;
  int _sectionEnd;
  boolean _parRangeFound;
  protected List _paragraphs;
  protected int _parStart;
  protected int _parEnd;
  boolean _charRangeFound;
  protected List _characters;
  int _charStart;
  int _charEnd;
  boolean _textRangeFound;
  protected List _text;
  int _textStart;
  int _textEnd;

//  protected Range()
//  {
//
//  }

  public Range(int start, int end, HWPFDocument doc)
  {
    _start = start;
    _end = end;
    _doc = doc;
    _sections = _doc.getSectionTable().getSections();
    _paragraphs = _doc.getParagraphTable().getParagraphs();
    _characters = _doc.getCharacterTable().getTextRuns();
    _text = _doc.getTextTable().getTextPieces();
    _parent = new WeakReference(null);
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
    _parent = new WeakReference(parent);
  }

  protected Range(int startIdx, int endIdx, int idxType, Range parent)
  {
    _doc = parent._doc;
    _sections = parent._sections;
    _paragraphs = parent._paragraphs;
    _characters = parent._characters;
    _text = parent._text;
    _parent = new WeakReference(parent);

    switch (idxType)
    {
      case TYPE_PARAGRAPH:
        _parStart = parent._parStart + startIdx;
        _parEnd = parent._parStart + endIdx;
        _start = ((PropertyNode)_paragraphs.get(_parStart)).getStart();
        _end = ((PropertyNode)_paragraphs.get(_parEnd)).getEnd();
        _parRangeFound = true;
        break;
      case TYPE_CHARACTER:
        _charStart = parent._charStart + startIdx;
        _charEnd = parent._charStart + endIdx;
        _start = ((PropertyNode)_characters.get(_charStart)).getStart();
        _end = ((PropertyNode)_characters.get(_charEnd)).getEnd();
        _charRangeFound = true;
        break;
     case TYPE_SECTION:
        _sectionStart = parent._sectionStart + startIdx;
        _sectionEnd = parent._sectionStart + endIdx;
        _start = ((PropertyNode)_sections.get(_sectionStart)).getStart();
        _end = ((PropertyNode)_sections.get(_sectionEnd)).getEnd();
        _sectionRangeFound = true;
        break;
     case TYPE_TEXT:
        _textStart = parent._textStart + startIdx;
        _textEnd = parent._textStart + endIdx;
        _start = ((PropertyNode)_text.get(_textStart)).getStart();
        _end = ((PropertyNode)_text.get(_textEnd)).getEnd();
        _textRangeFound = true;
        break;
    }
  }

  public String text()
  {
    initText();

    StringBuffer sb = new StringBuffer();

    for (int x = _textStart; x < _textEnd; x++)
    {
      TextPiece piece = (TextPiece)_text.get(x);
      int start = _start > piece.getStart() ? _start - piece.getStart() : 0;
      int end = _end <= piece.getEnd() ? _end - piece.getStart() : piece.getEnd() - piece.getStart();
      sb.append(piece.getStringBuffer().substring(start, end));
    }
    return sb.toString();
  }

  public int numSections()
  {
    initSections();
    return _sectionEnd - _sectionStart;
  }

  public int numParagraphs()
  {
    initParagraphs();
    return _parEnd - _parStart;
  }

  public int numCharacterRuns()
  {
    initCharacterRuns();
    return _charEnd - _charStart;
  }

  public CharacterRun insertBefore(String text)
    //throws UnsupportedEncodingException
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
    adjustForInsert(text.length());

    return getCharacterRun(0);
  }

  public CharacterRun insertAfter(String text)
  {
    initAll();
    int listIndex = _textEnd - 1;
    TextPiece tp = (TextPiece)_text.get(listIndex);
    StringBuffer sb = (StringBuffer)tp.getStringBuffer();

    int insertIndex = _end - tp.getStart();
    sb.insert(insertIndex, text);
    int adjustedLength = _doc.getTextTable().adjustForInsert(listIndex, text.length());
    _doc.getCharacterTable().adjustForInsert(_charEnd - 1, adjustedLength);
    _doc.getParagraphTable().adjustForInsert(_parEnd - 1, adjustedLength);
    _doc.getSectionTable().adjustForInsert(_sectionEnd - 1, adjustedLength);
    adjustForInsert(text.length());

    return getCharacterRun(numCharacterRuns() - 1);

  }

  public CharacterRun insertBefore(String text, CharacterProperties props)
    //throws UnsupportedEncodingException
  {
    initAll();
    PAPX papx = (PAPX)_paragraphs.get(_parStart);
    short istd = papx.getIstd();

    StyleSheet ss = _doc.getStyleSheet();
    CharacterProperties baseStyle = ss.getCharacterStyle(istd);
    byte[] grpprl = CharacterSprmCompressor.compressCharacterProperty(props, baseStyle);
    SprmBuffer buf = new SprmBuffer(grpprl);
    _doc.getCharacterTable().insert(_charStart, _start, buf);

    return insertBefore(text);
  }

  public CharacterRun insertAfter(String text, CharacterProperties props)
    //throws UnsupportedEncodingException
  {
    initAll();
    PAPX papx = (PAPX)_paragraphs.get(_parEnd - 1);
    short istd = papx.getIstd();

    StyleSheet ss = _doc.getStyleSheet();
    CharacterProperties baseStyle = ss.getCharacterStyle(istd);
    byte[] grpprl = CharacterSprmCompressor.compressCharacterProperty(props, baseStyle);
    SprmBuffer buf = new SprmBuffer(grpprl);
    _doc.getCharacterTable().insert(_charEnd, _end, buf);
    _charEnd++;
    return insertAfter(text);
  }


  public Paragraph insertBefore(ParagraphProperties props, int styleIndex)
    //throws UnsupportedEncodingException
  {
   return this.insertBefore(props, styleIndex, "\r");
  }

  protected Paragraph insertBefore(ParagraphProperties props, int styleIndex, String text)
    //throws UnsupportedEncodingException
  {
    initAll();
    StyleSheet ss = _doc.getStyleSheet();
    ParagraphProperties baseStyle = ss.getParagraphStyle(styleIndex);
    CharacterProperties baseChp = ss.getCharacterStyle(styleIndex);

    byte[] grpprl = ParagraphSprmCompressor.compressParagraphProperty(props, baseStyle);
    byte[] withIndex = new byte[grpprl.length + LittleEndian.SHORT_SIZE];
    LittleEndian.putShort(withIndex, (short)styleIndex);
    System.arraycopy(grpprl, 0, withIndex, LittleEndian.SHORT_SIZE, grpprl.length);
    SprmBuffer buf = new SprmBuffer(withIndex);

    _doc.getParagraphTable().insert(_parStart, _start, buf);
    insertBefore(text, baseChp);
    return getParagraph(0);
  }


  public Paragraph insertAfter(ParagraphProperties props, int styleIndex)
    //throws UnsupportedEncodingException
  {
    return this.insertAfter(props, styleIndex, "\r");
  }

  protected Paragraph insertAfter(ParagraphProperties props, int styleIndex, String text)
    //throws UnsupportedEncodingException
  {
    initAll();
    StyleSheet ss = _doc.getStyleSheet();
    ParagraphProperties baseStyle = ss.getParagraphStyle(styleIndex);
    CharacterProperties baseChp = ss.getCharacterStyle(styleIndex);

    byte[] grpprl = ParagraphSprmCompressor.compressParagraphProperty(props, baseStyle);
    byte[] withIndex = new byte[grpprl.length + LittleEndian.SHORT_SIZE];
    LittleEndian.putShort(withIndex, (short)styleIndex);
    System.arraycopy(grpprl, 0, withIndex, LittleEndian.SHORT_SIZE, grpprl.length);
    SprmBuffer buf = new SprmBuffer(withIndex);

    _doc.getParagraphTable().insert(_parEnd, _end, buf);
    _parEnd++;
    insertAfter(text, baseChp);
    return getParagraph(numParagraphs() - 1);
  }



  public Table insertBefore(TableProperties props, int rows)
  {
    ParagraphProperties parProps = new ParagraphProperties();
    parProps.setFInTable((byte)1);
    parProps.setTableLevel((byte)1);

    int columns = props.getItcMac();
    for (int x = 0; x < rows; x++)
    {
      Paragraph cell = this.insertBefore(parProps, StyleSheet.NIL_STYLE);
      cell.insertAfter(String.valueOf('\u0007'));
      for(int y = 1; y < columns; y++)
      {
        cell = cell.insertAfter(parProps, StyleSheet.NIL_STYLE);
        cell.insertAfter(String.valueOf('\u0007'));
      }
      cell = cell.insertAfter(parProps, StyleSheet.NIL_STYLE, String.valueOf('\u0007'));
      cell.setTableRowEnd(props);
    }
    return new Table(_start, _start + (rows * (columns + 1)), this, 0);
  }

  public ListEntry insertBefore(ParagraphProperties props, int listID, int level, int styleIndex)
    //throws UnsupportedEncodingException
  {
    ListTables lt = _doc.getListTables();
    if (lt.getLevel(listID, level) == null)
    {
      throw new NoSuchElementException("The specified list and level do not exist");
    }

    int ilfo = lt.getOverrideIndexFromListID(listID);
    props.setIlfo(ilfo);
    props.setIlvl((byte)level);

    return (ListEntry)insertBefore(props, styleIndex);
  }

  public CharacterRun getCharacterRun(int index)
  {
    initCharacterRuns();
    CHPX chpx = (CHPX)_characters.get(index + _charStart);

    int[] point = findRange(_paragraphs, _parStart, chpx.getStart(),
                              chpx.getEnd());
    PAPX papx = (PAPX)_paragraphs.get(point[0]);
    short istd = papx.getIstd();

    CharacterRun chp = new CharacterRun(chpx, _doc.getStyleSheet(), istd, this);

    return chp;
  }

  public Section getSection(int index)
  {
    initSections();
    SEPX sepx = (SEPX)_sections.get(index + _sectionStart);
    Section sep = new Section(sepx, this);
    return sep;
  }

  public Paragraph getParagraph(int index)
  {
    initParagraphs();
    PAPX papx = (PAPX)_paragraphs.get(index + _parStart);

    ParagraphProperties props = papx.getParagraphProperties(_doc.getStyleSheet());
    Paragraph pap = null;
    if (props.getIlfo() > 0)
    {
      pap = new ListEntry(papx, this, _doc.getListTables());
    }
    else
    {
      pap = new Paragraph(papx, this);
    }

    return pap;
  }

  public int type()
  {
    return TYPE_UNDEFINED;
  }

  public Table getTable(Paragraph paragraph)
  {
    if (!paragraph.isInTable())
    {
      throw new IllegalArgumentException("This paragraph doesn't belong to a table");
    }

    Range r = (Range)paragraph;
    if (r._parent.get() != this)
    {
      throw new IllegalArgumentException("This paragraph is not a child of this range");
    }

    r.initAll();
    int tableEnd = r._parEnd;

    if (r._parStart != 0 && ((Paragraph)r._paragraphs.get(r._parStart - 1)).isInTable())
    {
      throw new IllegalArgumentException("This paragraph is not the first one in the table");
    }

    int limit = r._paragraphs.size();
    for (; tableEnd < limit; tableEnd++)
    {
      if (!((Paragraph)r._paragraphs.get(tableEnd)).isInTable())
      {
        break;
      }
    }

    initAll();
    if (tableEnd > _parEnd)
    {
      throw new ArrayIndexOutOfBoundsException("The table's bounds fall outside of this Range");
    }

    return new Table(r._parStart, tableEnd, r._doc.getRange(), 1);
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
    while(node.getEnd() <= start)
    {
      x++;
      node = (PropertyNode)rpl.get(x);
    }

    int y = x;
    node = (PropertyNode)rpl.get(y);
    while(node.getEnd() < end)
    {
      y++;
      node = (PropertyNode)rpl.get(y);
    }
    return new int[]{x, y + 1};
  }

  private void reset()
  {
    _textRangeFound = false;
    _charRangeFound = false;
    _parRangeFound = false;
    _sectionRangeFound = false;
  }

  private void adjustForInsert(int length)
  {
    _end += length;
    reset();
    Range parent = (Range)_parent.get();
    if (parent != null)
    {
      parent.adjustForInsert(length);
    }
  }

}
