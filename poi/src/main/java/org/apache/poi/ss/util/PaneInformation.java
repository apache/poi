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

package org.apache.poi.ss.util;

/**
 * Holds information regarding a split plane or freeze plane for a sheet.
 *
 */
public class PaneInformation
{
    /** Constant for active pane being the lower right*/
    public static final byte PANE_LOWER_RIGHT = (byte)0;
    /** Constant for active pane being the upper right*/
    public static final byte PANE_UPPER_RIGHT = (byte)1;
    /** Constant for active pane being the lower left*/
    public static final byte PANE_LOWER_LEFT = (byte)2;
    /** Constant for active pane being the upper left*/
    public static final byte PANE_UPPER_LEFT = (byte)3;
    
	private final short x;
	private final short y;
	private final short topRow;
	private final short leftColumn;
	private final byte activePane;
	private final boolean frozen;
	
	public PaneInformation(short x, short y, short top, short left, byte active, boolean frozen) {
		this.x = x;
		this.y = y;
		this.topRow = top;
		this.leftColumn = left;
		this.activePane = active;
		this.frozen = frozen;
	}


	/**
	 * Returns the vertical position of the split.
	 * @return 0 if there is no vertical spilt,
	 *         or for a freeze pane the number of columns in the TOP pane,
	 *         or for a split plane the position of the split in 1/20th of a point.
	 */
	public short getVerticalSplitPosition() {
	  return x;
	}
	
	/**
	 * Returns the horizontal position of the split.
	 * @return 0 if there is no horizontal spilt,
	 *         or for a freeze pane the number of rows in the LEFT pane,
	 *         or for a split plane the position of the split in 1/20th of a point.
	 */
	public short getHorizontalSplitPosition() {
	  return y;
	}
	
	/**
	 * For a horizontal split returns the top row in the BOTTOM pane.
	 * @return 0 if there is no horizontal split, or the top row of the bottom pane.
	 */
	public short getHorizontalSplitTopRow() {
	  return topRow;
	}
	
	/**
	 * For a vertical split returns the left column in the RIGHT pane.
	 * @return 0 if there is no vertical split, or the left column in the RIGHT pane.
	 */
	public short getVerticalSplitLeftColumn() {
	  return leftColumn;
	}
	
	/**
	 * Returns the active pane
	 * @see #PANE_LOWER_RIGHT
	 * @see #PANE_UPPER_RIGHT
	 * @see #PANE_LOWER_LEFT
	 * @see #PANE_UPPER_LEFT
	 * @return the active pane.
	 */
	public byte getActivePane() {
	  return activePane;
	}
	
	/** Returns true if this is a Freeze pane, false if it is a split pane.
	 */
	public boolean isFreezePane() {
		return frozen;
	}
}
