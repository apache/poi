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

package org.apache.poi.hwpf.model;

import java.util.Arrays;

import org.apache.poi.util.Internal;

/**
 * A data structure used to hold some data we don't
 *  understand / can't handle, so we have it available
 *  for when we come to write back out again 
 */
@Internal
public final class UnhandledDataStructure
{
  private final byte[] _buf;

  public UnhandledDataStructure(byte[] buf, int offset, int length)
  {
    // Sanity check the size they've asked for
    int offsetEnd = offset + length;
    if (offsetEnd > buf.length || offsetEnd < 0)
    {
      throw new IndexOutOfBoundsException("Buffer Length is " + buf.length + " " +
                                          "but code is tried to read " + length + " " + 
                                          "from offset " + offset + " to " + offsetEnd);
    }
    if (offset < 0 || length < 0)
    {
       throw new IndexOutOfBoundsException("Offset and Length must both be >= 0, negative " +
            "indicies are not permitted - code is tried to read " + length + " from offset " + offset);
    }
    
    // Save that requested portion of the data 
    _buf = Arrays.copyOfRange(buf, offset, offsetEnd);

  }

  /*package*/ byte[] getBuf()
  {
    return _buf;
  }
}
