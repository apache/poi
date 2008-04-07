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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.record.cf.BorderFormatting;

/**
 * High level representation for Border Formatting component
 * of Conditional Formatting settings
 * 
 * @author Dmitriy Kumshayev
 *
 */
public final class HSSFBorderFormatting
{
	/** No border */
	public final static short BORDER_NONE =  BorderFormatting.BORDER_NONE;
	/** Thin border */
	public final static short BORDER_THIN =  BorderFormatting.BORDER_THIN;
	/** Medium border */
	public final static short BORDER_MEDIUM =  BorderFormatting.BORDER_MEDIUM;
	/** dash border */
	public final static short BORDER_DASHED =  BorderFormatting.BORDER_DASHED;
	/** dot border */
	public final static short BORDER_HAIR =  BorderFormatting.BORDER_HAIR;
	/** Thick border */
	public final static short BORDER_THICK =  BorderFormatting.BORDER_THICK;
	/** double-line border */
	public final static short BORDER_DOUBLE =  BorderFormatting.BORDER_DOUBLE;
	/** hair-line border */
	public final static short BORDER_DOTTED =  BorderFormatting.BORDER_DOTTED;
	/** Medium dashed border */
	public final static short BORDER_MEDIUM_DASHED =  BorderFormatting.BORDER_MEDIUM_DASHED;
	/** dash-dot border */
	public final static short BORDER_DASH_DOT =  BorderFormatting.BORDER_DASH_DOT;
	/** medium dash-dot border */
	public final static short BORDER_MEDIUM_DASH_DOT =  BorderFormatting.BORDER_MEDIUM_DASH_DOT;
	/** dash-dot-dot border */
	public final static short BORDER_DASH_DOT_DOT =  BorderFormatting.BORDER_DASH_DOT_DOT;
	/** medium dash-dot-dot border */
	public final static short BORDER_MEDIUM_DASH_DOT_DOT =  BorderFormatting.BORDER_MEDIUM_DASH_DOT_DOT;
	/** slanted dash-dot border */
	public final static short BORDER_SLANTED_DASH_DOT =  BorderFormatting.BORDER_SLANTED_DASH_DOT;

	
	private final BorderFormatting borderFormatting;
	
	public HSSFBorderFormatting()
	{
		borderFormatting = new BorderFormatting();
	}

	protected HSSFBorderFormatting(BorderFormatting borderFormatting)
	{
		this.borderFormatting = borderFormatting;
	}
	
	protected BorderFormatting getBorderFormattingBlock()
	{
		return borderFormatting;
	}

	public short getBorderBottom()
	{
		return borderFormatting.getBorderBottom();
	}

	public short getBorderDiagonal()
	{
		return borderFormatting.getBorderDiagonal();
	}

	public short getBorderLeft()
	{
		return borderFormatting.getBorderLeft();
	}

	public short getBorderRight()
	{
		return borderFormatting.getBorderRight();
	}

	public short getBorderTop()
	{
		return borderFormatting.getBorderTop();
	}

	public short getBottomBorderColor()
	{
		return borderFormatting.getBottomBorderColor();
	}

	public short getDiagonalBorderColor()
	{
		return borderFormatting.getDiagonalBorderColor();
	}

	public short getLeftBorderColor()
	{
		return borderFormatting.getLeftBorderColor();
	}

	public short getRightBorderColor()
	{
		return borderFormatting.getRightBorderColor();
	}

	public short getTopBorderColor()
	{
		return borderFormatting.getTopBorderColor();
	}

	public boolean isBackwardDiagonalOn()
	{
		return borderFormatting.isBackwardDiagonalOn();
	}

	public boolean isForwardDiagonalOn()
	{
		return borderFormatting.isForwardDiagonalOn();
	}

	public void setBackwardDiagonalOn(boolean on)
	{
		borderFormatting.setBackwardDiagonalOn(on);
	}

	public void setBorderBottom(short border)
	{
		borderFormatting.setBorderBottom(border);
	}

	public void setBorderDiagonal(short border)
	{
		borderFormatting.setBorderDiagonal(border);
	}

	public void setBorderLeft(short border)
	{
		borderFormatting.setBorderLeft(border);
	}

	public void setBorderRight(short border)
	{
		borderFormatting.setBorderRight(border);
	}

	public void setBorderTop(short border)
	{
		borderFormatting.setBorderTop(border);
	}

	public void setBottomBorderColor(short color)
	{
		borderFormatting.setBottomBorderColor(color);
	}

	public void setDiagonalBorderColor(short color)
	{
		borderFormatting.setDiagonalBorderColor(color);
	}

	public void setForwardDiagonalOn(boolean on)
	{
		borderFormatting.setForwardDiagonalOn(on);
	}

	public void setLeftBorderColor(short color)
	{
		borderFormatting.setLeftBorderColor(color);
	}

	public void setRightBorderColor(short color)
	{
		borderFormatting.setRightBorderColor(color);
	}

	public void setTopBorderColor(short color)
	{
		borderFormatting.setTopBorderColor(color);
	}
}
