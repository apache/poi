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

public class StyleDescription
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