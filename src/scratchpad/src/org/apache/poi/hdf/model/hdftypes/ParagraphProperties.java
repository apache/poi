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

import org.apache.poi.hdf.model.hdftypes.definitions.PAPAbstractType;
/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public final class ParagraphProperties  extends PAPAbstractType implements Cloneable
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
