
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
        

/*
 * FontFormatting.java
 *
 * Created on January 22, 2008, 10:05 PM
 */
package org.apache.poi.hssf.record.cf;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

/**
 * Pattern Formatting Block of the Conditional Formatting Rule Record.
 * 
 * @author Dmitriy Kumshayev
 */

public class PatternFormatting implements Cloneable
{
    /**  No background */
    public final static short     NO_FILL             = 0  ;
    /**  Solidly filled */
    public final static short     SOLID_FOREGROUND    = 1  ;
    /**  Small fine dots */
    public final static short     FINE_DOTS           = 2  ;
    /**  Wide dots */
    public final static short     ALT_BARS            = 3  ;
    /**  Sparse dots */
    public final static short     SPARSE_DOTS         = 4  ;
    /**  Thick horizontal bands */
    public final static short     THICK_HORZ_BANDS    = 5  ;
    /**  Thick vertical bands */
    public final static short     THICK_VERT_BANDS    = 6  ;
    /**  Thick backward facing diagonals */
    public final static short     THICK_BACKWARD_DIAG = 7  ;
    /**  Thick forward facing diagonals */
    public final static short     THICK_FORWARD_DIAG  = 8  ;
    /**  Large spots */
    public final static short     BIG_SPOTS           = 9  ;
    /**  Brick-like layout */
    public final static short     BRICKS              = 10 ;
    /**  Thin horizontal bands */
    public final static short     THIN_HORZ_BANDS     = 11 ;
    /**  Thin vertical bands */
    public final static short     THIN_VERT_BANDS     = 12 ;
    /**  Thin backward diagonal */
    public final static short     THIN_BACKWARD_DIAG  = 13 ;
    /**  Thin forward diagonal */
    public final static short     THIN_FORWARD_DIAG   = 14 ;
    /**  Squares */
    public final static short     SQUARES             = 15 ;
    /**  Diamonds */
    public final static short     DIAMONDS            = 16 ;
    /**  Less Dots */
    public final static short     LESS_DOTS           = 17 ;
    /**  Least Dots */
    public final static short     LEAST_DOTS          = 18 ;
	
    public PatternFormatting()
    {
        field_15_pattern_style	= (short)0;
        field_16_pattern_color_indexes	= (short)0;
    }
    
    /** Creates new FontFormatting */
    public PatternFormatting(RecordInputStream in)
	{
    	field_15_pattern_style	= in.readShort();
        field_16_pattern_color_indexes	= in.readShort();
	}
    
    // PATTERN FORMATING BLOCK
    // For Pattern Styles see constants at HSSFCellStyle (from NO_FILL to LEAST_DOTS)
    private short 			 field_15_pattern_style;
    private static final BitField  fillPatternStyle = BitFieldFactory.getInstance(0xFC00);
    
    private short 			 field_16_pattern_color_indexes;
    private static final BitField  patternColorIndex = BitFieldFactory.getInstance(0x007F);		 
    private static final BitField  patternBackgroundColorIndex = BitFieldFactory.getInstance(0x3F80);		 

    /**
     * setting fill pattern
     *
     * @see #NO_FILL
     * @see #SOLID_FOREGROUND
     * @see #FINE_DOTS
     * @see #ALT_BARS
     * @see #SPARSE_DOTS
     * @see #THICK_HORZ_BANDS
     * @see #THICK_VERT_BANDS
     * @see #THICK_BACKWARD_DIAG
     * @see #THICK_FORWARD_DIAG
     * @see #BIG_SPOTS
     * @see #BRICKS
     * @see #THIN_HORZ_BANDS
     * @see #THIN_VERT_BANDS
     * @see #THIN_BACKWARD_DIAG
     * @see #THIN_FORWARD_DIAG
     * @see #SQUARES
     * @see #DIAMONDS
     *
     * @param fp  fill pattern 
     */
    public void setFillPattern(short fp)
    {
    	field_15_pattern_style = fillPatternStyle.setShortValue(field_15_pattern_style, fp);
    }

    /**
     * get the fill pattern 
     * @return fill pattern
     */

    public short getFillPattern()
    {
        return fillPatternStyle.getShortValue(field_15_pattern_style);
    }
    
    /**
     * set the background fill color.
     *
     * @param bg  color
     */

    public void setFillBackgroundColor(short bg)
    {    	
    	field_16_pattern_color_indexes = patternBackgroundColorIndex.setShortValue(field_16_pattern_color_indexes,bg);
    }

    /**
     * get the background fill color
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return fill color
     */
    public short getFillBackgroundColor()
    {
    	return patternBackgroundColorIndex.getShortValue(field_16_pattern_color_indexes);
    }

    /**
     * set the foreground fill color
     * @param bg  color
     */
    public void setFillForegroundColor(short fg)
    {
    	field_16_pattern_color_indexes = patternColorIndex.setShortValue(field_16_pattern_color_indexes,fg);
    }

    /**
     * get the foreground fill color
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return fill color
     */
    public short getFillForegroundColor()
    {
    	return patternColorIndex.getShortValue(field_16_pattern_color_indexes);
    }
    
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("    [Pattern Formatting]\n");
        buffer.append("          .fillpattern= ").append(Integer.toHexString(getFillPattern())).append("\n");
        buffer.append("          .fgcoloridx= ").append(Integer.toHexString(getFillForegroundColor())).append("\n");
        buffer.append("          .bgcoloridx= ").append(Integer.toHexString(getFillBackgroundColor())).append("\n");
        buffer.append("    [/Pattern Formatting]\n");
        return buffer.toString();
    }
    
    public Object clone() 
    {
      PatternFormatting rec = new PatternFormatting();
      rec.field_15_pattern_style = field_15_pattern_style;
      rec.field_16_pattern_color_indexes = field_16_pattern_color_indexes; 
      return rec;
    }
    
    public int serialize(int offset, byte [] data)
    {
    	LittleEndian.putShort(data, offset, field_15_pattern_style);
    	offset += 2;
    	LittleEndian.putShort(data, offset, field_16_pattern_color_indexes);
    	offset += 2;
    	return 4;
    }
}
