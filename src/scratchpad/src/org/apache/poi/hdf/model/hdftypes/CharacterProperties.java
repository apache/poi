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

import org.apache.poi.hdf.model.hdftypes.definitions.CHPAbstractType;
/**
 * Properties for character runs.
 *
 * @author Ryan Ackley
 */

public class CharacterProperties extends CHPAbstractType implements Cloneable
{

  public CharacterProperties()
  {
    setDttmRMark(new short[2]);
    setDttmRMarkDel(new short[2]);
    setXstDispFldRMark(new byte[32]);
    setBrc(new short[2]);;
    setHps(20);
    setFcPic(-1);
    setIstd(10);
    setLidFE(0x0400);
    setLidDefault(0x0400);
    setWCharScale(100);
    //setFUsePgsuSettings(-1);
  }
  /**
   * Used to make a deep copy of this object.
   */
  public Object clone() throws CloneNotSupportedException
  {
    CharacterProperties clone = (CharacterProperties)super.clone();
    clone.setBrc(new short[2]);
    System.arraycopy(getBrc(), 0, clone.getBrc(), 0, 2);
    System.arraycopy(getDttmRMark(), 0, clone.getDttmRMark(), 0, 2);
    System.arraycopy(getDttmRMarkDel(), 0, clone.getDttmRMarkDel(), 0, 2);
    System.arraycopy(getXstDispFldRMark(), 0, clone.getXstDispFldRMark(), 0, 32);
    return clone;
  }
}