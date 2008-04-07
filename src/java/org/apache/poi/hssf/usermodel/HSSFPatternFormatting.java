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

import org.apache.poi.hssf.record.cf.PatternFormatting;

/**
 * High level representation for Conditional Formatting settings
 * 
 * @author Dmitriy Kumshayev
 *
 */
public class HSSFPatternFormatting
{
	/**  No background */
	public final static short NO_FILL 				=  PatternFormatting.NO_FILL;
	/**  Solidly filled */
	public final static short SOLID_FOREGROUND 		=  PatternFormatting.SOLID_FOREGROUND;
	/**  Small fine dots */
	public final static short FINE_DOTS 			=  PatternFormatting.FINE_DOTS;
	/**  Wide dots */
	public final static short ALT_BARS 				=  PatternFormatting.ALT_BARS;
	/**  Sparse dots */
	public final static short SPARSE_DOTS 			=  PatternFormatting.SPARSE_DOTS;
	/**  Thick horizontal bands */
	public final static short THICK_HORZ_BANDS 		=  PatternFormatting.THICK_HORZ_BANDS;
	/**  Thick vertical bands */
	public final static short THICK_VERT_BANDS 		=  PatternFormatting.THICK_VERT_BANDS;
	/**  Thick backward facing diagonals */
	public final static short THICK_BACKWARD_DIAG 	=  PatternFormatting.THICK_BACKWARD_DIAG;
	/**  Thick forward facing diagonals */
	public final static short THICK_FORWARD_DIAG 	=  PatternFormatting.THICK_FORWARD_DIAG;
	/**  Large spots */
	public final static short BIG_SPOTS 			=  PatternFormatting.BIG_SPOTS;
	/**  Brick-like layout */
	public final static short BRICKS 				=  PatternFormatting.BRICKS;
	/**  Thin horizontal bands */
	public final static short THIN_HORZ_BANDS 		=  PatternFormatting.THIN_HORZ_BANDS;
	/**  Thin vertical bands */
	public final static short THIN_VERT_BANDS 		=  PatternFormatting.THIN_VERT_BANDS;
	/**  Thin backward diagonal */
	public final static short THIN_BACKWARD_DIAG 	=  PatternFormatting.THIN_BACKWARD_DIAG;
	/**  Thin forward diagonal */
	public final static short THIN_FORWARD_DIAG 	=  PatternFormatting.THIN_FORWARD_DIAG;
	/**  Squares */
	public final static short SQUARES 				=  PatternFormatting.SQUARES;
	/**  Diamonds */
	public final static short DIAMONDS 				=  PatternFormatting.DIAMONDS;
	/**  Less Dots */
	public final static short LESS_DOTS 			=  PatternFormatting.LESS_DOTS;
	/**  Least Dots */
	public final static short LEAST_DOTS 			=  PatternFormatting.LEAST_DOTS;

	private PatternFormatting patternFormatting;
	
	public HSSFPatternFormatting()
	{
		patternFormatting = new PatternFormatting();
	}
	protected HSSFPatternFormatting(PatternFormatting patternFormatting)
	{
		this.patternFormatting = patternFormatting;
	}

	protected PatternFormatting getPatternFormattingBlock()
	{
		return patternFormatting;
	}

	/**
	 * @return
	 * @see org.apache.poi.hssf.record.cf.PatternFormatting#getFillBackgroundColor()
	 */
	public short getFillBackgroundColor()
	{
		return patternFormatting.getFillBackgroundColor();
	}

	/**
	 * @return
	 * @see org.apache.poi.hssf.record.cf.PatternFormatting#getFillForegroundColor()
	 */
	public short getFillForegroundColor()
	{
		return patternFormatting.getFillForegroundColor();
	}

	/**
	 * @return
	 * @see org.apache.poi.hssf.record.cf.PatternFormatting#getFillPattern()
	 */
	public short getFillPattern()
	{
		return patternFormatting.getFillPattern();
	}

	/**
	 * @param bg
	 * @see org.apache.poi.hssf.record.cf.PatternFormatting#setFillBackgroundColor(short)
	 */
	public void setFillBackgroundColor(short bg)
	{
		patternFormatting.setFillBackgroundColor(bg);
	}

	/**
	 * @param fg
	 * @see org.apache.poi.hssf.record.cf.PatternFormatting#setFillForegroundColor(short)
	 */
	public void setFillForegroundColor(short fg)
	{
		patternFormatting.setFillForegroundColor(fg);
	}

	/**
	 * @param fp
	 * @see org.apache.poi.hssf.record.cf.PatternFormatting#setFillPattern(short)
	 */
	public void setFillPattern(short fp)
	{
		patternFormatting.setFillPattern(fp);
	}
}
