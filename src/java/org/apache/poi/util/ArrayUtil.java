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

/**
 * Utility classes for dealing with arrays.
 *
 * @author Glen Stampoultzis
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

    /**
     * Moves a number of entries in an array to another point in the array,
     *  shifting those inbetween as required.
     * @param array The array to alter
     * @param moveFrom The (0 based) index of the first entry to move
     * @param moveTo The (0 based) index of the positition to move to
     * @param numToMove The number of entries to move
     */
    public static void arrayMoveWithin(Object[] array, int moveFrom, int moveTo, int numToMove) {
    	// If we're not asked to do anything, return now
    	if(numToMove <= 0) { return; }
    	if(moveFrom == moveTo) { return; }
    	
    	// Check that the values supplied are valid
    	if(moveFrom < 0 || moveFrom >= array.length) {
    		throw new IllegalArgumentException("The moveFrom must be a valid array index");
    	}
    	if(moveTo < 0 || moveTo >= array.length) {
    		throw new IllegalArgumentException("The moveTo must be a valid array index");
    	}
    	if(moveFrom+numToMove > array.length) {
    		throw new IllegalArgumentException("Asked to move more entries than the array has");
    	}
    	if(moveTo+numToMove > array.length) {
    		throw new IllegalArgumentException("Asked to move to a position that doesn't have enough space");
    	}
    	
    	// Grab the bit to move 
    	Object[] toMove = new Object[numToMove];
    	System.arraycopy(array, moveFrom, toMove, 0, numToMove);
    	
    	// Grab the bit to be shifted
    	Object[] toShift;
    	int shiftTo;
    	if(moveFrom > moveTo) {
    		// Moving to an earlier point in the array
    		// Grab everything between the two points
    		toShift = new Object[(moveFrom-moveTo)];
    		System.arraycopy(array, moveTo, toShift, 0, toShift.length);
    		shiftTo = moveTo + numToMove;
    	} else {
    		// Moving to a later point in the array
    		// Grab everything from after the toMove block, to the new point
    		toShift = new Object[(moveTo-moveFrom)];
    		System.arraycopy(array, moveFrom+numToMove, toShift, 0, toShift.length);
    		shiftTo = moveFrom;
    	}
    	
    	// Copy the moved block to its new location
    	System.arraycopy(toMove, 0, array, moveTo, toMove.length);
    	
    	// And copy the shifted block to the shifted location
    	System.arraycopy(toShift, 0, array, shiftTo, toShift.length);
    	
    	
    	// We're done - array will now have everything moved as required
    }

    /**
     * Copies the specified array, truncating or padding with zeros (if
     * necessary) so the copy has the specified length. This method is temporary
     * replace for Arrays.copyOf() until we start to require JDK 1.6.
     * 
     * @param source
     *            the array to be copied
     * @param newLength
     *            the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with zeros to
     *         obtain the specified length
     * @throws NegativeArraySizeException
     *             if <tt>newLength</tt> is negative
     * @throws NullPointerException
     *             if <tt>original</tt> is null
     */
    public static byte[] copyOf( byte[] source, int newLength )
    {
        byte[] result = new byte[newLength];
        System.arraycopy( source, 0, result, 0,
                Math.min( source.length, newLength ) );
        return result;
    }

    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>(byte)0</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     *
     * This method is temporary
     * replace for Arrays.copyOfRange() until we start to require JDK 1.6.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with zeros to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if <tt>from &lt; 0</tt>
     *     or <tt>from &gt; original.length()</tt>
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static byte[] copyOfRange(byte[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0,
                Math.min(original.length - from, newLength));
        return copy;
    }

}
