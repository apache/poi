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


package org.apache.poi.hdf.model.hdftypes;

import org.apache.poi.hdf.model.hdftypes.definitions.PAPAbstractType;
/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public class ParagraphProperties  extends PAPAbstractType implements Cloneable
{


  public ParagraphProperties()
  {
    short[] lspd = new short[2];
    setFWidowControl((byte)1);
    //lspd[0] = 240;
    lspd[1] = 1;
    setIlvl((byte)9);

    setLspd(lspd);
    setBrcBar(new short[2]);
    setBrcBottom(new short[2]);
    setBrcLeft(new short[2]);
    setBrcBetween(new short[2]);
    setBrcRight(new short[2]);
    setBrcTop(new short[2]);
    setPhe(new byte[12]);
    setAnld(new byte[84]);
    setDttmPropRMark(new byte[4]);
    setNumrm(new byte[8]);


  }
  public Object clone() throws CloneNotSupportedException
  {
      ParagraphProperties clone =  (ParagraphProperties)super.clone();

      short[] brcBar = new short[2];
      short[] brcBottom = new short[2];
      short[] brcLeft = new short[2];
      short[] brcBetween = new short[2];
      short[] brcRight = new short[2];
      short[] brcTop = new short[2];
      short[] lspd = new short[2];
      byte[] phe = new byte[12];
      byte[] anld = new byte[84];
      byte[] dttmPropRMark = new byte[4];
      byte[] numrm = new byte[8];

      System.arraycopy(getBrcBar(), 0, brcBar, 0, 2);
      System.arraycopy(getBrcBottom(), 0, brcBottom, 0, 2);
      System.arraycopy(getBrcLeft(), 0, brcLeft, 0, 2);
      System.arraycopy(getBrcBetween(), 0, brcBetween, 0, 2);
      System.arraycopy(getBrcRight(), 0, brcRight, 0, 2);
      System.arraycopy(getBrcTop(), 0, brcTop, 0, 2);
      System.arraycopy(getLspd(), 0, lspd, 0, 2);
      System.arraycopy(getPhe(), 0, phe, 0, 12);
      System.arraycopy(getAnld(), 0, anld, 0, 84);
      System.arraycopy(getDttmPropRMark(), 0, dttmPropRMark, 0, 4);
      System.arraycopy(getNumrm(), 0, numrm, 0, 8);


      clone.setBrcBar(brcBar);
      clone.setBrcBottom(brcBottom);
      clone.setBrcLeft(brcLeft);
      clone.setBrcBetween(brcBetween);
      clone.setBrcRight(brcRight);
      clone.setBrcTop(brcTop);
      clone.setLspd(lspd);
      clone.setPhe(phe);
      clone.setAnld(anld);
      clone.setDttmPropRMark(dttmPropRMark);
      clone.setNumrm(numrm);
      return clone;
  }

}