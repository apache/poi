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

package org.apache.poi.hwpf.sprm;

import java.util.List;

import org.apache.poi.util.LittleEndian;


public class SprmUtils
{
  public SprmUtils()
  {
  }

  public static byte[] shortArrayToByteArray(short[] convert)
  {
    byte[] buf = new byte[convert.length * LittleEndian.SHORT_SIZE];

    for (int x = 0; x < convert.length; x++)
    {
      LittleEndian.putShort(buf, x * LittleEndian.SHORT_SIZE, convert[x]);
    }

    return buf;
  }

  public static int addSprm(short instruction, int param, byte[] varParam, List list)
  {
    int type = instruction & 0xe000;

    byte[] sprm = null;
    switch(type)
    {
      case 0:
      case 1:
        sprm = new byte[3];
        sprm[2] = (byte)param;
        break;
      case 2:
        sprm = new byte[4];
        LittleEndian.putShort(sprm, 2, (short)param);
        break;
      case 3:
        sprm = new byte[6];
        LittleEndian.putInt(sprm, 2, param);
        break;
      case 4:
      case 5:
        sprm = new byte[4];
        LittleEndian.putShort(sprm, 2, (short)param);
        break;
      case 6:
        sprm = new byte[3 + varParam.length];
        sprm[2] = (byte)varParam.length;
        System.arraycopy(varParam, 0, sprm, 3, varParam.length);
        break;
      case 7:
        sprm = new byte[5];
        // this is a three byte int so it has to be handled special
        byte[] temp = new byte[4];
        LittleEndian.putInt(temp, 0, param);
        System.arraycopy(temp, 0, sprm, 2, 3);
        break;
      default:
        //should never happen
        break;
    }
    LittleEndian.putShort(sprm, 0, instruction);
    list.add(sprm);
    return sprm.length;
  }

  public static int convertBrcToInt(short[] brc)
  {
    byte[] buf = new byte[4];
    LittleEndian.putShort(buf, brc[0]);
    LittleEndian.putShort(buf, LittleEndian.SHORT_SIZE, brc[1]);
    return LittleEndian.getInt(buf);
  }
}
