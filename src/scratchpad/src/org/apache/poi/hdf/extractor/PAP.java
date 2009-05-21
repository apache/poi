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

package org.apache.poi.hdf.extractor;

/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public final class PAP implements Cloneable
{
  int _istd;//index to style descriptor.
  byte _jc;//justification code
  byte _fKeep;//keep entire paragraph on one page if possible
  byte _fKeepFollow;//keep paragraph on same page with next paragraph if possible
  byte _fPageBreakBefore;//start this paragraph on new page
  byte _positionByte;//multiple flags see spec;
  byte _brcp;//rectangle border codes for Macword 3.0
  byte _brcl;//border line styles for Macword 3.0
  byte _ilvl;//when non-zero, list level for this paragraph
  byte _fNoLnn;//no line numbering for this paragraph. (makes this an exception to the section property of line numbering)
  int  _ilfo;//when non-zero, (1-based) index into the pllfo identifying the list to which the paragraph belongs
  byte _fSideBySide;//when 1, paragraph is a side by side paragraph
  byte _fNoAutoHyph;//when 0, text in paragraph may be auto hyphenated.
  byte _fWindowControl;//when 1, Word will prevent widowed lines in this paragraph from being placed at the beginning of a page
  int _dxaRight;//indent from right margin (signed).
  int _dxaLeft;//indent from left margin (signed)
  int _dxaLeft1;//first line indent; signed number relative to dxaLeft
  int[] _lspd = new int[2];//line spacing descriptor see spec
  int _dyaBefore;// vertical spacing before paragraph (unsigned)
  int _dyaAfter;//vertical spacing after paragraph (unsigned)
  byte[] _phe = new byte[12];//height of current paragraph
  byte _fCrLf;//undocumented
  byte _fUsePgsuSettings;//undocumented
  byte _fAdjustRight;//undocumented
  byte _fKinsoku;// when 1, apply kinsoku rules when performing line wrapping
  byte _fWordWrap;//when 1, perform word wrap
  byte _fOverflowPunct;//when 1, apply overflow punctuation rules when performing line wrapping
  byte _fTopLinePunct;//when 1, perform top line punctuation processing
  byte _fAutoSpaceDE;//when 1, auto space FE and alphabetic characters
  byte _fAutoSpaceDN;// when 1, auto space FE and numeric characters
  int _wAlignFont;//font alignment 0 Hanging 1 Centered 2 Roman 3 Variable 4 Auto
  short _fontAlign;//multiVal see Spec.
  byte _fInTable;//when 1, paragraph is contained in a table row
  byte _fTtp;//when 1, paragraph consists only of the row mark special character and marks the end of a table row
  byte _wr;//Wrap Code for absolute objects
  byte _fLocked;//when 1, paragraph may not be edited
  int _dxaAbs;//see spec
  int _dyaAbs;//see spec
  int _dxaWidth;//when not == 0, paragraph is constrained to be dxaWidth wide, independent of current margin or column settings
  short[] _brcTop = new short[2];//spec for border above paragraph
  short[] _brcLeft = new short[2];//specification for border to the left of
  short[] _brcBottom = new short[2];//paragraphspecification for border below
  short[] _brcRight = new short[2];//paragraphspecification for border to the
  short[] _brcBetween = new short[2];//right of paragraphsee spec
  short[] _brcBar = new short[2];//specification of border to place on
  short _brcTop1;//outside of text when facing pages are to be displayed.spec
  short _brcLeft1;//for border above paragraphspecification for border to the
  short _brcBottom1;//left ofparagraphspecification for border below
  short _brcRight1;//paragraphspecification for border to the
  short _brcBetween1;//right of paragraphsee spec
  short _brcBar1;//specification of border to place on outside of text when facing pages are to be displayed.
  int _dxaFromText;//horizontal distance to be maintained between an absolutely positioned paragraph and any non-absolute positioned text
  int _dyaFromText;//vertical distance to be maintained between an absolutely positioned paragraph and any non-absolute positioned text
  int _dyaHeight;//see spec
  int _shd;//shading
  int _dcs;//drop cap specifier
  byte[] _anld = new byte[84];//autonumber list descriptor (see ANLD definition)
  short _fPropRMark;//when 1, properties have been changed with revision marking on
  short _ibstPropRMark;//index to author IDs stored in hsttbfRMark. used when properties have been changed when revision marking was enabled
  byte[] _dttmPropRMark = new byte[4];//Date/time at which properties of this were changed for this run of text by the author. (Only recorded when revision marking is on.)
  byte[] _numrm = new byte[8];//paragraph numbering revision mark data (see NUMRM)
  short _itbdMac;//number of tabs stops defined for paragraph. Must be >= 0 and <= 64.



  public PAP()
  {
    _fWindowControl = 1;
    //lspd[0] = 240;
    _lspd[1] = 1;
    _ilvl = 9;
  }
  public Object clone() throws CloneNotSupportedException
  {
      PAP clone =  (PAP)super.clone();

      clone._brcBar = new short[2];
      clone._brcBottom = new short[2];
      clone._brcLeft = new short[2];
      clone._brcBetween = new short[2];
      clone._brcRight = new short[2];
      clone._brcTop = new short[2];
      clone._lspd = new int[2];
      clone._phe = new byte[12];
      clone._anld = new byte[84];
      clone._dttmPropRMark = new byte[4];
      clone._numrm = new byte[8];

      System.arraycopy(_brcBar, 0, clone._brcBar, 0, 2);
      System.arraycopy(_brcBottom, 0, clone._brcBottom, 0, 2);
      System.arraycopy(_brcLeft, 0, clone._brcLeft, 0, 2);
      System.arraycopy(_brcBetween, 0, clone._brcBetween, 0, 2);
      System.arraycopy(_brcRight, 0, clone._brcRight, 0, 2);
      System.arraycopy(_brcTop, 0, clone._brcTop, 0, 2);
      System.arraycopy(_lspd, 0, clone._lspd, 0, 2);
      System.arraycopy(_phe, 0, clone._phe, 0, 12);
      System.arraycopy(_anld, 0, clone._anld, 0, 84);
      System.arraycopy(_dttmPropRMark, 0, clone._dttmPropRMark, 0, 4);
      System.arraycopy(_numrm, 0, clone._numrm, 0, 8);

      return clone;
  }

}
