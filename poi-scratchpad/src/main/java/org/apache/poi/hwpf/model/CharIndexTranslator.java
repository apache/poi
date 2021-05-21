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

import org.apache.poi.util.Internal;

@Internal
public interface CharIndexTranslator {
    /**
     * Calculates the byte index of the given char index.
     *
     * @param charPos
     *            The char position
     * @return The byte index
     */
    int getByteIndex( int charPos );

    /**
     * Finds character ranges that includes specified byte range.
     *
     * @param startBytePosInclusive
     *            start byte range
     * @param endBytePosExclusive
     *            end byte range
     */
    int[][] getCharIndexRanges( int startBytePosInclusive,
            int endBytePosExclusive );

    /**
     * Check if index is in table
     *
     * @return true if index in table, false if not
     */
    boolean isIndexInTable(int bytePos);

    /**
     * Return first index &gt;= bytePos that is in table
     *
     * @return  first index greater or equal to bytePos that is in table
     */
    int lookIndexForward(int bytePos);

    /**
     * Return last index &lt;= bytePos that is in table
     *
     * @return last index less of equal to bytePos that is in table
     */
    int lookIndexBackward(int bytePos);

}
