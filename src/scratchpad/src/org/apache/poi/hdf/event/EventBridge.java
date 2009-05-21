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

package org.apache.poi.hdf.event;


import org.apache.poi.hdf.model.util.BTreeSet;
import org.apache.poi.hdf.model.util.NumberFormatter;
import org.apache.poi.hdf.model.hdftypes.*;

import org.apache.poi.util.LittleEndian;

import java.util.ArrayList;

public final class EventBridge implements HDFLowLevelParsingListener
{

  private static int HEADER_EVEN_INDEX = 0;
  private static int HEADER_ODD_INDEX = 1;
  private static int FOOTER_EVEN_INDEX = 2;
  private static int FOOTER_ODD_INDEX = 3;
  private static int HEADER_FIRST_INDEX = 4;
  private static int FOOTER_FIRST_INDEX = 5;

  /** This class translates low level events into high level events for this
  *   listener */
  HDFParsingListener _listener;
  /** stylesheet for this document */
  StyleSheet _stsh;
  /** name says it all */
  DocumentProperties _dop;
  /** StyleDescription for the current paragraph. */
  StyleDescription _currentStd;
  /** List info for this doc */
  ListTables _listTables;


  /** "WordDocument" from the POIFS */
  byte[] _mainDocument;
  /** Table0 or Table1 from POIFS */
  byte[] _tableStream;

  /** text offset in main stream */
  int _fcMin;
  int _ccpText;
  int _ccpFtn;
  int _hdrSize;
  int _hdrOffset;

  /** text pieces */
  BTreeSet _text = new BTreeSet();

  private boolean _beginHeaders;
  BTreeSet _hdrSections = new BTreeSet();
  BTreeSet _hdrParagraphs = new BTreeSet();
  BTreeSet _hdrCharacterRuns = new BTreeSet();

  int _sectionCounter = 1;
  ArrayList _hdrs = new ArrayList();

  private boolean _holdParagraph = false;
  private int _endHoldIndex = -1;
  private ArrayList _onHold;

  public EventBridge(HDFParsingListener listener)
  {
    _listener = listener;
  }
  public void mainDocument(byte[] mainDocument)
  {
    _mainDocument = mainDocument;
  }
  public void tableStream(byte[] tableStream)
  {
    _tableStream = tableStream;
  }
  public void miscellaneous(int fcMin, int ccpText, int ccpFtn, int fcPlcfhdd, int lcbPlcfhdd)
  {
    _fcMin = fcMin;
    _ccpText = ccpText;
    _ccpFtn = ccpFtn;
    _hdrOffset = fcPlcfhdd;
    _hdrSize = lcbPlcfhdd;
  }
  public void document(DocumentProperties dop)
  {
    _dop = dop;
  }
  public void bodySection(SepxNode sepx)
  {
    SectionProperties sep = (SectionProperties)StyleSheet.uncompressProperty(sepx.getSepx(), new SectionProperties(), _stsh);
    HeaderFooter[] hdrArray = findSectionHdrFtrs(_sectionCounter);
    _hdrs.add(hdrArray);
    _listener.section(sep, sepx.getStart() - _fcMin, sepx.getEnd() - _fcMin);
    _sectionCounter++;
  }

  public void hdrSection(SepxNode sepx)
  {
    _beginHeaders = true;
    _hdrSections.add(sepx);
  }
  public void endSections()
  {
    for (int x = 1; x < _sectionCounter; x++)
    {
      HeaderFooter[] hdrArray = (HeaderFooter[])_hdrs.get(x-1);
      HeaderFooter hf = null;

      if (!hdrArray[HeaderFooter.HEADER_EVEN - 1].isEmpty())
      {
        hf = hdrArray[HeaderFooter.HEADER_EVEN - 1];
        _listener.header(x - 1, HeaderFooter.HEADER_EVEN);
        flushHeaderProps(hf.getStart(), hf.getEnd());
      }
      if (!hdrArray[HeaderFooter.HEADER_ODD - 1].isEmpty())
      {
        hf = hdrArray[HeaderFooter.HEADER_ODD - 1];
        _listener.header(x - 1, HeaderFooter.HEADER_ODD);
        flushHeaderProps(hf.getStart(), hf.getEnd());
      }
      if (!hdrArray[HeaderFooter.FOOTER_EVEN - 1].isEmpty())
      {
        hf = hdrArray[HeaderFooter.FOOTER_EVEN - 1];
        _listener.footer(x - 1, HeaderFooter.FOOTER_EVEN);
        flushHeaderProps(hf.getStart(), hf.getEnd());
      }
      if (!hdrArray[HeaderFooter.FOOTER_ODD - 1].isEmpty())
      {
        hf = hdrArray[HeaderFooter.FOOTER_EVEN - 1];
        _listener.footer(x - 1, HeaderFooter.FOOTER_EVEN);
        flushHeaderProps(hf.getStart(), hf.getEnd());
      }
      if (!hdrArray[HeaderFooter.HEADER_FIRST - 1].isEmpty())
      {
        hf = hdrArray[HeaderFooter.HEADER_FIRST - 1];
        _listener.header(x - 1, HeaderFooter.HEADER_FIRST);
        flushHeaderProps(hf.getStart(), hf.getEnd());
      }
      if (!hdrArray[HeaderFooter.FOOTER_FIRST - 1].isEmpty())
      {
        hf = hdrArray[HeaderFooter.FOOTER_FIRST - 1];
        _listener.footer(x - 1, HeaderFooter.FOOTER_FIRST);
        flushHeaderProps(hf.getStart(), hf.getEnd());
      }
    }
  }



  public void paragraph(PapxNode papx)
  {
    if (_beginHeaders)
    {
      _hdrParagraphs.add(papx);
    }
    byte[] bytePapx = papx.getPapx();
    int istd = LittleEndian.getShort(bytePapx, 0);
    _currentStd = _stsh.getStyleDescription(istd);

    ParagraphProperties pap = (ParagraphProperties)StyleSheet.uncompressProperty(bytePapx, _currentStd.getPAP(), _stsh);

    if (pap.getFTtp() > 0)
    {
      TableProperties tap = (TableProperties)StyleSheet.uncompressProperty(bytePapx, new TableProperties(), _stsh);
      _listener.tableRowEnd(tap, papx.getStart() - _fcMin, papx.getEnd() - _fcMin);
    }
    else if (pap.getIlfo() > 0)
    {
      _holdParagraph = true;
      _endHoldIndex = papx.getEnd();
      _onHold.add(papx);
    }
    else
    {
      _listener.paragraph(pap, papx.getStart() - _fcMin, papx.getEnd() - _fcMin);
    }
  }

  public void characterRun(ChpxNode chpx)
  {
    if (_beginHeaders)
    {
      _hdrCharacterRuns.add(chpx);
    }

    int start = chpx.getStart();
    int end = chpx.getEnd();
    //check to see if we should hold this characterRun
    if (_holdParagraph)
    {
      _onHold.add(chpx);
      if (end >= _endHoldIndex)
      {
        _holdParagraph = false;
        _endHoldIndex = -1;
        flushHeldParagraph();
        _onHold = new ArrayList();
      }
    }

    byte[] byteChpx = chpx.getChpx();


    CharacterProperties chp = (CharacterProperties)StyleSheet.uncompressProperty(byteChpx, _currentStd.getCHP(), _stsh);

    ArrayList textList = BTreeSet.findProperties(start, end, _text.root);
    String text = getTextFromNodes(textList, start, end);

    _listener.characterRun(chp, text, start - _fcMin, end - _fcMin);
  }
  public void text(TextPiece t)
  {
    _text.add(t);
  }
  public void fonts(FontTable fontTbl)
  {
  }
  public void lists(ListTables listTbl)
  {
    _listTables = listTbl;
  }
  public void styleSheet(StyleSheet stsh)
  {
    _stsh = stsh;
  }
  private void flushHeaderProps(int start, int end)
  {
    ArrayList list = BTreeSet.findProperties(start, end, _hdrSections.root);
    int size = list.size();

    for (int x = 0; x < size; x++)
    {
      SepxNode oldNode = (SepxNode)list.get(x);
      int secStart = Math.max(oldNode.getStart(), start);
      int secEnd = Math.min(oldNode.getEnd(), end);

      //SepxNode node = new SepxNode(-1, secStart, secEnd, oldNode.getSepx());
      //bodySection(node);

      ArrayList parList = BTreeSet.findProperties(secStart, secEnd, _hdrParagraphs.root);
      int parSize = parList.size();

      for (int y = 0; y < parSize; y++)
      {
        PapxNode oldParNode = (PapxNode)parList.get(y);
        int parStart = Math.max(oldParNode.getStart(), secStart);
        int parEnd = Math.min(oldParNode.getEnd(), secEnd);

        PapxNode parNode = new PapxNode(parStart, parEnd, oldParNode.getPapx());
        paragraph(parNode);

        ArrayList charList = BTreeSet.findProperties(parStart, parEnd, _hdrCharacterRuns.root);
        int charSize = charList.size();

        for (int z = 0; z < charSize; z++)
        {
          ChpxNode oldCharNode = (ChpxNode)charList.get(z);
          int charStart = Math.max(oldCharNode.getStart(), parStart);
          int charEnd = Math.min(oldCharNode.getEnd(), parEnd);

          ChpxNode charNode = new ChpxNode(charStart, charEnd, oldCharNode.getChpx());
          characterRun(charNode);
        }
      }

    }

  }
  private String getTextFromNodes(ArrayList list, int start, int end)
  {
    int size = list.size();

    StringBuffer sb = new StringBuffer();

    for (int x = 0; x < size; x++)
    {
      TextPiece piece = (TextPiece)list.get(x);
      int charStart = Math.max(start, piece.getStart());
      int charEnd = Math.min(end, piece.getEnd());

      if(piece.usesUnicode())
      {
        for (int y = charStart; y < charEnd; y += 2)
        {
          sb.append((char)LittleEndian.getShort(_mainDocument, y));
        }
      }
      else
      {
        for (int y = charStart; y < charEnd; y++)
        {
          sb.append(_mainDocument[y]);
        }
      }
    }
    return sb.toString();
  }

  private void flushHeldParagraph()
  {
    PapxNode papx = (PapxNode)_onHold.get(0);
    byte[] bytePapx = papx.getPapx();
    int istd = LittleEndian.getShort(bytePapx, 0);
    StyleDescription std = _stsh.getStyleDescription(istd);

    ParagraphProperties pap = (ParagraphProperties)StyleSheet.uncompressProperty(bytePapx, _currentStd.getPAP(), _stsh);
    LVL lvl = _listTables.getLevel(pap.getIlfo(), pap.getIlvl());
    pap = (ParagraphProperties)StyleSheet.uncompressProperty(lvl._papx, pap, _stsh, false);

    int size = _onHold.size() - 1;

    CharacterProperties numChp = (CharacterProperties)StyleSheet.uncompressProperty(((ChpxNode)_onHold.get(size)).getChpx(), std.getCHP(), _stsh);

    numChp = (CharacterProperties)StyleSheet.uncompressProperty(lvl._chpx, numChp, _stsh);
    String bulletText = getBulletText(lvl, pap);

    _listener.listEntry(bulletText, numChp, pap, papx.getStart() - _fcMin, papx.getEnd() - _fcMin);
    for (int x = 1; x <= size; x++)
    {
      characterRun((ChpxNode)_onHold.get(x));
    }

  }

  private String getBulletText(LVL lvl, ParagraphProperties pap)
  {
    StringBuffer bulletBuffer = new StringBuffer();
    for(int x = 0; x < lvl._xst.length; x++)
    {
      if(lvl._xst[x] < 9)
      {
        LVL numLevel = _listTables.getLevel(pap.getIlfo(), lvl._xst[x]);
        int num = numLevel._iStartAt;
        if(lvl == numLevel)
        {
          numLevel._iStartAt++;
        }
        else if(num > 1)
        {
          num--;
        }
        bulletBuffer.append(NumberFormatter.getNumber(num, lvl._nfc));

      }
      else
      {
        bulletBuffer.append(lvl._xst[x]);
      }

    }

    switch (lvl._ixchFollow)
    {
      case 0:
        bulletBuffer.append('\u0009');
        break;
      case 1:
        bulletBuffer.append(' ');
        break;
    }
    return bulletBuffer.toString();
  }

  private HeaderFooter[] findSectionHdrFtrs(int index)
  {
    HeaderFooter[] hdrArray = new HeaderFooter[6];

    for (int x = 1; x < 7; x++)
    {
      hdrArray[x-1] = createSectionHdrFtr(index, x);
    }

    return hdrArray;
  }

  private HeaderFooter createSectionHdrFtr(int index, int type)
  {
    if(_hdrSize < 50)
    {
      return new HeaderFooter(0,0,0);
    }

    int start = _fcMin + _ccpText + _ccpFtn;
    int end = start;
    int arrayIndex = 0;

    switch(type)
    {
      case HeaderFooter.HEADER_EVEN:
           arrayIndex = (HEADER_EVEN_INDEX + (index * 6));
           break;
      case HeaderFooter.FOOTER_EVEN:
           arrayIndex = (FOOTER_EVEN_INDEX + (index * 6));
           break;
      case HeaderFooter.HEADER_ODD:
           arrayIndex = (HEADER_ODD_INDEX + (index * 6));
           break;
      case HeaderFooter.FOOTER_ODD:
           arrayIndex = (FOOTER_ODD_INDEX + (index * 6));
           break;
      case HeaderFooter.HEADER_FIRST:
           arrayIndex = (HEADER_FIRST_INDEX + (index * 6));
           break;
      case HeaderFooter.FOOTER_FIRST:
           arrayIndex = (FOOTER_FIRST_INDEX + (index * 6));
           break;
    }
    start += LittleEndian.getInt(_tableStream, _hdrOffset + (arrayIndex * 4));
    end += LittleEndian.getInt(_tableStream, _hdrOffset + (arrayIndex + 1) * 4);

    HeaderFooter retValue = new HeaderFooter(type, start, end);

    if((end - start) == 0 && index > 1)
    {
      retValue = createSectionHdrFtr(type, index - 1);
    }
    return retValue;
  }
}
