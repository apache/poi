/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.util;

/**
 * Utility classes for dealing with arrays.
 *
 * @author Glen Stampoultzis
 * @version $Id$
 */
public class ArrayUtil
{
    /**
     * This is really a debugging version of <code>System.arraycopy()</code>.
     * Use it to provide better exception messages when copying arrays around.
     * For production use it's better to use the original for speed.
     */
    public static void arraycopy(byte[] src, int src_position, byte[] dst, int dst_position, int length)
    {
        if (src_position < 0)
            throw new IllegalArgumentException("src_position was less than 0.  Actual value " + src_position);
        if (src_position >= src.length)
            throw new IllegalArgumentException( "src_position was greater than src array size.  Tried to write starting at position " + src_position + " but the array length is " + src.length );
        if (src_position + length > src.length)
            throw new IllegalArgumentException("src_position + length would overrun the src array.  Expected end at " + (src_position + length) + " actual end at " + src.length);
        if (dst_position < 0)
            throw new IllegalArgumentException("dst_position was less than 0.  Actual value " + dst_position);
        if (dst_position >= dst.length)
            throw new IllegalArgumentException( "dst_position was greater than dst array size.  Tried to write starting at position " + dst_position + " but the array length is " + dst.length );
        if (dst_position + length > dst.length)
            throw new IllegalArgumentException("dst_position + length would overrun the dst array.  Expected end at " + (dst_position + length) + " actual end at " + dst.length);

        System.arraycopy( src, src_position, dst, dst_position, length);
    }


}
