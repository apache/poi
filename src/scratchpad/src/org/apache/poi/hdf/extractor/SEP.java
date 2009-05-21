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

public final class SEP
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



  public SEP()
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
