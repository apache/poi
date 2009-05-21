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

import org.apache.poi.hdf.model.hdftypes.definitions.CHPAbstractType;
/**
 * Properties for character runs.
 *
 * @author Ryan Ackley
 */

public final class CharacterProperties extends CHPAbstractType implements Cloneable
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
