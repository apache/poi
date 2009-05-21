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

import org.apache.poi.hdf.extractor.util.*;

/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public final class TextPiece extends PropertyNode implements Comparable
{
  private boolean _usesUnicode;
  private int _length;

  public TextPiece(int start, int length, boolean unicode)
  {
    super(start, start + length, null);
      _usesUnicode = unicode;
      _length = length;
      //_fcStart = start;
      //_fcEnd = start + length;

  }
   public boolean usesUnicode()
  {
      return _usesUnicode;
  }

   public int compareTo(Object obj) {
       return 0;
   }

}
