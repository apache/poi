/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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


package org.apache.poi.hdf.extractor;


/**
 * Comment me
 *
 * @author Ryan Ackley 
 */

public class CHP implements Cloneable
{
  boolean _bold;
  boolean _italic;
  boolean _fRMarkDel;
  boolean _fOutline;
  boolean _fSmallCaps;
  boolean _fCaps;
  boolean _fVanish;
  boolean _fRMark;
  boolean _fSpec;
  boolean _fStrike;
  boolean _fObj;
  boolean _fShadow;
  boolean _fLowerCase;
  boolean _fData;
  boolean _fOle2;
  boolean _fEmboss;
  boolean _fImprint;
  boolean _fDStrike;

  short _ftcAscii;
  short _ftcFE;
  short _ftcOther;
  short _ftc;
  int _hps;//font size in half points
  int _dxaSpace;//space following each character in the run expressed in twip units
  byte _iss;//superscript/subscript indices 0 means no super/subscripting 1 means text in run is superscripted 2 means text in run is subscripted
  byte _kul;//underline code see spec
  byte _ico;//color of text see spec
  short _hpsPos;//super/subscript position in half points; positive means text is raised; negative means text is lowered
  short _lidDefault;//language for non-Far East text
  short _lidFE;//language for Far East text
  byte _idctHint;
  int _wCharScale;
  short _chse;

  int _specialFC;//varies depending on whether this is a special char
  short _ibstRMark;//index to author IDs stored in hsttbfRMark. used when text in run was newly typed when revision marking was enabled
  short _ibstRMarkDel;//index to author IDs stored in hsttbfRMark. used when text in run was newly typed when revision marking was enabled
  int[] _dttmRMark = new int[2];//Date/time at which this run of text was
  int[] _dttmRMarkDel = new int[2];//entered/modified by the author. (Only
                                     //recorded when revision marking is on.)Date/time at which this run of text was deleted by the author. (Only recorded when revision marking is on.)
  int _istd;
  int _baseIstd = -1;
  int _fcPic;
  short _ftcSym;// see spec
  short _xchSym;//see spec
  byte _ysr;//hyphenation rules
  byte _chYsr;//used for hyphenation see spec
  int _hpsKern;//kerning distance for characters in run recorded in half points
  int _fcObj;
  byte _icoHighlight;//highlight color
  boolean _fChsDiff;
  boolean _highlighted;//when true characters are highlighted with color specified by chp.icoHighlight
  boolean _fPropMark;//when true, properties have been changed with revision marking on
  short _ibstPropRMark;//index to author IDs stored in hsttbfRMark. used when properties have been changed when revision marking was enabled
  int _dttmPropRMark;//Date/time at which properties of this were changed for this run of text by the author
  byte _sfxtText;//text animation see spec
  boolean _fDispFldRMark;//see spec
  short _ibstDispFldRMark;//Index to author IDs stored in hsttbfRMark. used when ListNum field numbering has been changed when revision marking was enabled
  int _dttmDispFldRMark;//The date for the ListNum field number change
  byte[] _xstDispFldRMark = new byte[32];//The string value of the ListNum field when revision mark tracking began
  short _shd;//shading
  short[] _brc = new short[2];//border
  short _paddingStart = 0;
  short _paddingEnd = 0;

  public CHP()
  {
      _istd = 10;
      _hps = 20;
      _lidDefault = 0x0400;
      _lidFE = 0x0400;

  }
  public void copy(CHP toCopy)
  {
      _bold = toCopy._bold;
      _italic = toCopy._italic;
       _fRMarkDel = toCopy._fRMarkDel;
       _fOutline = toCopy._fOutline;
       _fSmallCaps = toCopy._fSmallCaps;
       _fCaps = toCopy._fCaps;
       _fVanish = toCopy._fVanish;
       _fRMark = toCopy._fRMark;
       _fSpec = toCopy._fSpec;
       _fStrike = toCopy._fStrike;
       _fObj = toCopy._fObj;
       _fShadow = toCopy._fShadow;
       _fLowerCase = toCopy._fLowerCase;
       _fData = toCopy._fData;
       _fOle2 = toCopy._fOle2;
       _fEmboss = toCopy._fEmboss;
       _fImprint = toCopy._fImprint;
       _fDStrike = toCopy._fDStrike;

       _ftcAscii = toCopy._ftcAscii;
       _ftcFE = toCopy._ftcFE;
       _ftcOther = toCopy._ftcOther;
       _ftc = toCopy._ftc;
       _hps = toCopy._hps;
       _dxaSpace = toCopy._dxaSpace;
       _iss = toCopy._iss;
       _kul = toCopy._kul;
       _ico = toCopy._ico;
       _hpsPos = toCopy._hpsPos;
       _lidDefault = toCopy._lidDefault;
       _lidFE = toCopy._lidFE;
       _idctHint = toCopy._idctHint;
       _wCharScale = toCopy._wCharScale;
       _chse = toCopy._chse;

       _specialFC = toCopy._specialFC;
       _ibstRMark = toCopy._ibstRMark;
       _ibstRMarkDel = toCopy._ibstRMarkDel;
       _dttmRMark = toCopy._dttmRMark;
       _dttmRMarkDel  = toCopy._dttmRMarkDel;

       _istd = toCopy._istd;
       _baseIstd = toCopy._baseIstd;
       _fcPic = toCopy._fcPic;
       _ftcSym = toCopy._ftcSym;
       _xchSym = toCopy._xchSym;
       _ysr = toCopy._ysr;
       _chYsr = toCopy._chYsr;
       _hpsKern = toCopy._hpsKern;
       _fcObj = toCopy._fcObj;
       _icoHighlight = toCopy._icoHighlight;
       _fChsDiff = toCopy._fChsDiff;
       _highlighted = toCopy._highlighted;
       _fPropMark = toCopy._fPropMark;
       _ibstPropRMark = toCopy._ibstPropRMark;
       _dttmPropRMark = toCopy._dttmPropRMark;
       _sfxtText = toCopy._sfxtText;
       _fDispFldRMark = toCopy._fDispFldRMark;
       _ibstDispFldRMark = toCopy._ibstDispFldRMark;
       _dttmDispFldRMark = toCopy._dttmDispFldRMark;
       _xstDispFldRMark = toCopy._xstDispFldRMark;
       _shd = toCopy._shd;
       _brc = toCopy._brc;

  }

  public Object clone() throws CloneNotSupportedException
  {
    CHP clone = (CHP)super.clone();
    clone._brc = new short[2];
    System.arraycopy(_brc, 0, clone._brc, 0, 2);
    return clone;
  }
}