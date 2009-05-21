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

package org.apache.poi.hdf.extractor.util;

/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public abstract class PropertyNode implements Comparable {
  private byte[] _grpprl;
  private int _fcStart;
  private int _fcEnd;

  public PropertyNode(int fcStart, int fcEnd, byte[] grpprl)
  {
      _fcStart = fcStart;
      _fcEnd = fcEnd;
      _grpprl = grpprl;
  }
  public int getStart()
  {
      return _fcStart;
  }
  public int getEnd()
  {
    return _fcEnd;
  }
  protected byte[] getGrpprl()
  {
    return _grpprl;
  }
  public int compareTo(Object o)
  {
      int fcStart = ((PropertyNode)o).getStart();
      if(_fcStart == fcStart)
      {
        return 0;
      }
      else if(_fcStart < fcStart)
      {
        return -1;
      }
      else
      {
        return 1;
      }
  }
}
