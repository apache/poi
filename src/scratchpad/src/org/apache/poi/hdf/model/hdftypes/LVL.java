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

package org.apache.poi.hdf.model.hdftypes;

/**
 * Comment me
 *
 * @author Ryan Ackley
 */


public final class LVL
{
  public int _iStartAt;
  public byte _nfc;
  byte _jc;
  boolean _fLegal;
  boolean _fNoRestart;
  boolean _fPrev;
  boolean _fPrevSpace;
  boolean _fWord6;
  public byte[] _rgbxchNums = new byte[9];
  public byte _ixchFollow;
  public int _dxaSpace;
  public int _dxaIndent;
  public byte[] _chpx;
  public byte[] _papx;
  public char[] _xst;
  public short _istd;

  //byte _cbGrpprlChpx;
  //byte _cbGrpprlPapx;


  public LVL()
  {
  }
  public Object clone()
  {
    LVL obj = null;
    try
    {
      obj = (LVL)super.clone();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return obj;
  }
}
