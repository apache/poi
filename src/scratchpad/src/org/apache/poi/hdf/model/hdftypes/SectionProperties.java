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


package org.apache.poi.hdf.model.hdftypes;

/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public class SectionProperties implements HDFType
{
  int _index;
  byte _bkc;
  boolean _fTitlePage;
  boolean _fAutoPgn;
  byte _nfcPgn;
  boolean _fUnlocked;
  byte _cnsPgn;
  boolean _fPgnRestart;
  boolean _fEndNote;
  byte _lnc;
  byte _grpfIhdt;
  short _nLnnMod;
  int _dxaLnn;
  short _dxaPgn;
  short _dyaPgn;
  boolean _fLBetween;
  byte _vjc;
  short _dmBinFirst;
  short _dmBinOther;
  short _dmPaperReq;
  short[] _brcTop = new short[2];
  short[] _brcLeft = new short[2];
  short[] _brcBottom = new short[2];
  short[] _brcRight = new short[2];
  boolean _fPropMark;
  int _dxtCharSpace;
  int _dyaLinePitch;
  short _clm;
  byte _dmOrientPage;
  byte _iHeadingPgn;
  short _pgnStart;
  short _lnnMin;
  short _wTextFlow;
  short _pgbProp;
  int _xaPage;
  int _yaPage;
  int _dxaLeft;
  int _dxaRight;
  int _dyaTop;
  int _dyaBottom;
  int _dzaGutter;
  int _dyaHdrTop;
  int _dyaHdrBottom;
  short _ccolM1;
  boolean _fEvenlySpaced;
  int _dxaColumns;
  int[] _rgdxaColumnWidthSpacing;
  byte _dmOrientFirst;
  byte[] _olstAnn;



  public SectionProperties()
  {
      _bkc = 2;
      _dyaPgn = 720;
      _dxaPgn = 720;
      _fEndNote = true;
      _fEvenlySpaced = true;
      _xaPage = 12240;
      _yaPage = 15840;
      _dyaHdrTop = 720;
      _dyaHdrBottom = 720;
      _dmOrientPage = 1;
      _dxaColumns = 720;
      _dyaTop = 1440;
      _dxaLeft = 1800;
      _dyaBottom = 1440;
      _dxaRight = 1800;
      _pgnStart = 1;

  }
}