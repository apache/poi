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

import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.BorderCode;

public class SectionSprmUncompressor extends SprmUncompressor
{
  public SectionSprmUncompressor()
  {
  }
  public static Section uncompressSEP(Section parent,
                                                  byte[] grpprl,
                                                  int offset)
  {
    Section newProperties = null;
    try
    {
      newProperties = (Section) parent.clone();
    }
    catch (CloneNotSupportedException cnse)
    {
      throw new RuntimeException("There is no way this exception should happen!!");
    }
    SprmIterator sprmIt = new SprmIterator(grpprl, offset);

    while (sprmIt.hasNext())
    {
      SprmOperation sprm = (SprmOperation)sprmIt.next();
      unCompressSEPOperation(newProperties, sprm);
    }

    return newProperties;
  }

  /**
   * Used in decompression of a sepx. This performs an operation defined by
   * a single sprm.
   *
   * @param newSEP The SectionProperty to perfrom the operation on.
   * @param operand The operation to perform.
   * @param param The operation's parameter.
   * @param varParam The operation variable length parameter.
   */
  static void unCompressSEPOperation (Section newSEP, SprmOperation sprm)
  {
    switch (sprm.getOperation())
    {
      case 0:
        newSEP.setCnsPgn ((byte) sprm.getOperand());
        break;
      case 0x1:
        newSEP.setIHeadingPgn ((byte) sprm.getOperand());
        break;
      case 0x2:
        byte[] buf = new byte[sprm.size() - 3];
        System.arraycopy(sprm.getGrpprl(), sprm.getGrpprlOffset(), buf, 0, buf.length);
        newSEP.setOlstAnm (buf);
        break;
      case 0x3:
        //not quite sure
        break;
      case 0x4:
        //not quite sure
        break;
      case 0x5:
        newSEP.setFEvenlySpaced (getFlag (sprm.getOperand()));
        break;
      case 0x6:
        newSEP.setFUnlocked (getFlag (sprm.getOperand()));
        break;
      case 0x7:
        newSEP.setDmBinFirst ((short) sprm.getOperand());
        break;
      case 0x8:
        newSEP.setDmBinOther ((short) sprm.getOperand());
        break;
      case 0x9:
        newSEP.setBkc ((byte) sprm.getOperand());
        break;
      case 0xa:
        newSEP.setFTitlePage (getFlag (sprm.getOperand()));
        break;
      case 0xb:
        newSEP.setCcolM1 ((short) sprm.getOperand());
        break;
      case 0xc:
        newSEP.setDxaColumns (sprm.getOperand());
        break;
      case 0xd:
        newSEP.setFAutoPgn (getFlag (sprm.getOperand()));
        break;
      case 0xe:
        newSEP.setNfcPgn ((byte) sprm.getOperand());
        break;
      case 0xf:
        newSEP.setDyaPgn ((short) sprm.getOperand());
        break;
      case 0x10:
        newSEP.setDxaPgn ((short) sprm.getOperand());
        break;
      case 0x11:
        newSEP.setFPgnRestart (getFlag (sprm.getOperand()));
        break;
      case 0x12:
        newSEP.setFEndNote (getFlag (sprm.getOperand()));
        break;
      case 0x13:
        newSEP.setLnc ((byte) sprm.getOperand());
        break;
      case 0x14:
        newSEP.setGrpfIhdt ((byte) sprm.getOperand());
        break;
      case 0x15:
        newSEP.setNLnnMod ((short) sprm.getOperand());
        break;
      case 0x16:
        newSEP.setDxaLnn (sprm.getOperand());
        break;
      case 0x17:
        newSEP.setDyaHdrTop (sprm.getOperand());
        break;
      case 0x18:
        newSEP.setDyaHdrBottom (sprm.getOperand());
        break;
      case 0x19:
        newSEP.setFLBetween (getFlag (sprm.getOperand()));
        break;
      case 0x1a:
        newSEP.setVjc ((byte) sprm.getOperand());
        break;
      case 0x1b:
        newSEP.setLnnMin ((short) sprm.getOperand());
        break;
      case 0x1c:
        newSEP.setPgnStart ((short) sprm.getOperand());
        break;
      case 0x1d:
        newSEP.setDmOrientPage ((byte) sprm.getOperand());
        break;
      case 0x1e:

        //nothing
        break;
      case 0x1f:
        newSEP.setXaPage (sprm.getOperand());
        break;
      case 0x20:
        newSEP.setYaPage (sprm.getOperand());
        break;
      case 0x21:
        newSEP.setDxaLeft (sprm.getOperand());
        break;
      case 0x22:
        newSEP.setDxaRight (sprm.getOperand());
        break;
      case 0x23:
        newSEP.setDyaTop (sprm.getOperand());
        break;
      case 0x24:
        newSEP.setDyaBottom (sprm.getOperand());
        break;
      case 0x25:
        newSEP.setDzaGutter (sprm.getOperand());
        break;
      case 0x26:
        newSEP.setDmPaperReq ((short) sprm.getOperand());
        break;
      case 0x27:
        newSEP.setFPropMark (getFlag (sprm.getOperand()));
        break;
      case 0x28:
        break;
      case 0x29:
        break;
      case 0x2a:
        break;
      case 0x2b:
        newSEP.setBrcTop(new BorderCode(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x2c:
        newSEP.setBrcLeft(new BorderCode(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x2d:
        newSEP.setBrcBottom(new BorderCode(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x2e:
        newSEP.setBrcRight(new BorderCode(sprm.getGrpprl(), sprm.getGrpprlOffset()));
        break;
      case 0x2f:
        newSEP.setPgbProp (sprm.getOperand());
        break;
      case 0x30:
        newSEP.setDxtCharSpace (sprm.getOperand());
        break;
      case 0x31:
        newSEP.setDyaLinePitch (sprm.getOperand());
        break;
      case 0x33:
        newSEP.setWTextFlow ((short) sprm.getOperand());
        break;
      default:
        break;
    }

  }


}
