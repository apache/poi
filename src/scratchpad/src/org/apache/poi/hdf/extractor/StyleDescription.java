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

public final class StyleDescription
{

  private static int PARAGRAPH_STYLE = 1;
  private static int CHARACTER_STYLE = 2;

  int _baseStyleIndex;
  int _styleTypeCode;
  int _numUPX;
  byte[] _papx;
  byte[] _chpx;
  PAP _pap;
  CHP _chp;

  public StyleDescription()
  {
      _pap = new PAP();
      _chp = new CHP();
  }
  public StyleDescription(byte[] std, int baseLength, boolean word9)
  {
      int infoShort = Utils.convertBytesToShort(std, 2);
      _styleTypeCode = (infoShort & 0xf);
      _baseStyleIndex = (infoShort & 0xfff0) >> 4;

      infoShort = Utils.convertBytesToShort(std, 4);
      _numUPX = infoShort & 0xf;

      //first byte(s) of variable length section of std is the length of the
      //style name and aliases string
      int nameLength = 0;
      int multiplier = 1;
      if(word9)
      {
          nameLength = Utils.convertBytesToShort(std, baseLength);
          multiplier = 2;
      }
      else
      {
          nameLength = std[baseLength];
      }
      //2 bytes for length, length then null terminator.
      int grupxStart = multiplier + ((nameLength + 1) * multiplier) + baseLength;

      int offset = 0;
      for(int x = 0; x < _numUPX; x++)
      {
          int upxSize = Utils.convertBytesToShort(std, grupxStart + offset);
          if(_styleTypeCode == PARAGRAPH_STYLE)
          {
              if(x == 0)
              {
                  _papx = new byte[upxSize];
                  System.arraycopy(std, grupxStart + offset + 2, _papx, 0, upxSize);
              }
              else if(x == 1)
              {
                  _chpx = new byte[upxSize];
                  System.arraycopy(std, grupxStart + offset + 2, _chpx, 0, upxSize);
              }
          }
          else if(_styleTypeCode == CHARACTER_STYLE && x == 0)
          {
              _chpx = new byte[upxSize];
              System.arraycopy(std, grupxStart + offset + 2, _chpx, 0, upxSize);
          }

          if(upxSize % 2 == 1)
          {
              ++upxSize;
          }
          offset += 2 + upxSize;
      }



  }
  public int getBaseStyle()
  {
      return _baseStyleIndex;
  }
  public byte[] getCHPX()
  {
      return _chpx;
  }
  public byte[] getPAPX()
  {
      return _papx;
  }
  public PAP getPAP()
  {
      return _pap;
  }
  public CHP getCHP()
  {
      return _chp;
  }
  public void setPAP(PAP pap)
  {
      _pap = pap;
  }
  public void setCHP(CHP chp)
  {
      _chp = chp;
  }
}
