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

package org.apache.poi.util;

import java.util.Iterator;
import java.util.PrimitiveIterator;

@Internal
public class CodepointsUtil {

    /**
     * @param text to iterate over
     * @return iterator with Strings representing the codepoints
     */
  public static Iterator<String> iteratorFor(String text) {
    PrimitiveIterator.OfInt iter = primitiveIterator(text);
    return new Iterator<String>() {
      @Override
      public boolean hasNext() {
        return iter.hasNext();
      }

      @Override
      public String next() {
        return Character.toString(iter.next());
      }
    };
  }

    /**
     * @param text to iterate over
     * @return iterator with ints representing the codepoints
     * @since POI 5.2.4
     */
    public static PrimitiveIterator.OfInt primitiveIterator(String text) {
        return text.codePoints().iterator();
    }
}
