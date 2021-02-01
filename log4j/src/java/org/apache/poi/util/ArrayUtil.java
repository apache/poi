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


import java.util.Arrays;

/**
 * Utility classes for dealing with arrays.
 */
@Internal
public final class ArrayUtil {

	private ArrayUtil() {}

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
    	Object[] toMove = Arrays.copyOfRange(array, moveFrom, moveFrom+numToMove);

    	// Grab the bit to be shifted
    	Object[] toShift;
    	int shiftTo;
    	if(moveFrom > moveTo) {
    		// Moving to an earlier point in the array
    		// Grab everything between the two points
    		toShift = Arrays.copyOfRange(array, moveTo, moveFrom);
    		shiftTo = moveTo + numToMove;
    	} else {
    		// Moving to a later point in the array
    		// Grab everything from after the toMove block, to the new point
    		toShift = Arrays.copyOfRange(array, moveFrom+numToMove, moveTo+numToMove);
    		shiftTo = moveFrom;
    	}

    	// Copy the moved block to its new location
    	System.arraycopy(toMove, 0, array, moveTo, toMove.length);

    	// And copy the shifted block to the shifted location
    	System.arraycopy(toShift, 0, array, shiftTo, toShift.length);


    	// We're done - array will now have everything moved as required
    }

}
