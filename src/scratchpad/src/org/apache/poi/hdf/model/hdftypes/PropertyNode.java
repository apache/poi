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
 * Represents a lightweight node in the Trees used to store formatting
 * properties.
 *
 * @author Ryan Ackley
 */
public abstract class PropertyNode implements Comparable {
  private byte[] _grpprl;
  private int _fcStart;
  private int _fcEnd;

  /**
   * @param fcStart The start of the text for this property.
   * @param fcEnd The end of the text for this property.
   * @param grpprl The property description in compressed form.
   */
  public PropertyNode(int fcStart, int fcEnd, byte[] grpprl)
  {
      _fcStart = fcStart;
      _fcEnd = fcEnd;
      _grpprl = grpprl;
  }
  /**
   * @return The offset of this property's text.
   */
  public int getStart()
  {
      return _fcStart;
  }
  /**
   * @return The offset of the end of this property's text.
   */
  public int getEnd()
  {
    return _fcEnd;
  }
  /**
   * @return This property's property in copmpressed form.
   */
  protected byte[] getGrpprl()
  {
    return _grpprl;
  }
  /**
   * Used for sorting in collections.
   */
  public int compareTo(Object o)
  {
      int fcEnd = ((PropertyNode)o).getEnd();
      if(_fcEnd == fcEnd)
      {
        return 0;
      }
      else if(_fcEnd < fcEnd)
      {
        return -1;
      }
      else
      {
        return 1;
      }
  }
}
