
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
 * ConditionalFormattingRuleRecord.java
 *
 * Created on January 23, 2008, 9:56 AM
 */
package org.apache.poi.hssf.record;

import java.util.List;
import java.util.Stack;

import org.apache.poi.hssf.record.cf.BorderFormatting;
import org.apache.poi.hssf.record.cf.FontFormatting;
import org.apache.poi.hssf.record.cf.PatternFormatting;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

/**
 * Conditional Formatting Rule Record.
 * @author Dmitriy Kumshayev
 */

public class CFRuleRecord extends Record
{
    
    public static final short sid = 0x01B1;
    
    private byte             field_1_condition_type;
    public static final byte CONDITION_TYPE_NO_CONDITION_TYPE = 0;
    public static final byte CONDITION_TYPE_CELL_VALUE_IS = 1;
    public static final byte CONDITION_TYPE_FORMULA = 2;
    
    private byte             field_2_comparison_operator;
    public static final byte COMPARISON_OPERATOR_NO_COMPARISON = 0;
    public static final byte COMPARISON_OPERATOR_BETWEEN 	   = 1;
    public static final byte COMPARISON_OPERATOR_NOT_BETWEEN   = 2;
    public static final byte COMPARISON_OPERATOR_EQUAL         = 3;
    public static final byte COMPARISON_OPERATOR_NOT_EQUAL     = 4;
    public static final byte COMPARISON_OPERATOR_GT            = 5;
    public static final byte COMPARISON_OPERATOR_LT            = 6;
    public static final byte COMPARISON_OPERATOR_GE            = 7;
    public static final byte COMPARISON_OPERATOR_LE            = 8;
    
    private short            field_3_formula1_len;
    private short            field_4_formula2_len;
    
    private int              field_5_options;

    private static final BitField modificationBits = BitFieldFactory.getInstance(0x83FFFFFF); // Bits: font,align,bord,patt,prot
    private static final BitField alignHor		= BitFieldFactory.getInstance(0x00000001); // 0 = Horizontal alignment modified
 	private static final BitField alignVer		= BitFieldFactory.getInstance(0x00000002); // 0 = Vertical alignment modified
	private static final BitField alignWrap		= BitFieldFactory.getInstance(0x00000004); // 0 = Text wrapped flag modified
	private static final BitField alignRot		= BitFieldFactory.getInstance(0x00000008); // 0 = Text rotation modified
	private static final BitField alignJustLast = BitFieldFactory.getInstance(0x00000010); // 0 = Justify last line flag modified
	private static final BitField alignIndent	= BitFieldFactory.getInstance(0x00000020); // 0 = Indentation modified
	private static final BitField alignShrink	= BitFieldFactory.getInstance(0x00000040); // 0 = Shrink to fit flag modified
	private static final BitField notUsed1 		= BitFieldFactory.getInstance(0x00000080); // Always 1
	private static final BitField protLocked    = BitFieldFactory.getInstance(0x00000100); // 0 = Cell locked flag modified
	private static final BitField protHidden	= BitFieldFactory.getInstance(0x00000200); // 0 = Cell hidden flag modified
	private static final BitField bordLeft 		= BitFieldFactory.getInstance(0x00000400); // 0 = Left border style and colour modified
	private static final BitField bordRight 	= BitFieldFactory.getInstance(0x00000800); // 0 = Right border style and colour modified
	private static final BitField bordTop		= BitFieldFactory.getInstance(0x00001000); // 0 = Top border style and colour modified
	private static final BitField bordBot 		= BitFieldFactory.getInstance(0x00002000); // 0 = Bottom border style and colour modified
	private static final BitField bordTlBr 		= BitFieldFactory.getInstance(0x00004000); // 0 = Top-left to bottom-right border flag modified
	private static final BitField bordBlTr 		= BitFieldFactory.getInstance(0x00008000); // 0 = Bottom-left to top-right border flag modified
	private static final BitField pattStyle 	= BitFieldFactory.getInstance(0x00010000); // 0 = Pattern style modified
	private static final BitField pattCol 		= BitFieldFactory.getInstance(0x00020000); // 0 = Pattern colour modified
	private static final BitField pattBgCol 	= BitFieldFactory.getInstance(0x00040000); // 0 = Pattern background colour modified
	private static final BitField notUsed2 		= BitFieldFactory.getInstance(0x00380000); // Always 111
	private static final BitField undocumented	= BitFieldFactory.getInstance(0x03C00000); // Undocumented bits
    private static final BitField fmtBlockBits  = BitFieldFactory.getInstance(0x7C000000); // Bits: font,align,bord,patt,prot
	private static final BitField font 			= BitFieldFactory.getInstance(0x04000000); // 1 = Record contains font formatting block
	private static final BitField align    		= BitFieldFactory.getInstance(0x08000000); // 1 = Record contains alignment formatting block
	private static final BitField bord     		= BitFieldFactory.getInstance(0x10000000); // 1 = Record contains border formatting block
	private static final BitField patt     		= BitFieldFactory.getInstance(0x20000000); // 1 = Record contains pattern formatting block
	private static final BitField prot     		= BitFieldFactory.getInstance(0x40000000); // 1 = Record contains protection formatting block
	private static final BitField alignTextDir	= BitFieldFactory.getInstance(0x80000000); // 0 = Text direction modified
    
    
    private short            field_6_not_used;
    
    private FontFormatting fontFormatting;

    private byte 			 field_8_align_text_break;
    private byte 			 field_9_align_text_rotation_angle;
    private short 			 field_10_align_indentation;
    private short 			 field_11_relative_indentation;
    private short 			 field_12_not_used;
    
    private BorderFormatting borderFormatting;

    private PatternFormatting patternFormatting;
    
    private Stack            field_17_formula1;
    private Stack            field_18_formula2;
    
    /** Creates new CFRuleRecord */
    public CFRuleRecord()
    {
    	field_1_condition_type=CONDITION_TYPE_NO_CONDITION_TYPE;
    	field_2_comparison_operator=COMPARISON_OPERATOR_NO_COMPARISON;
    	setExpression1Length((short)0);
    	setExpression2Length((short)0);
    	
    	// Set modification flags to 1: by default options are not modified
    	setOptions(modificationBits.setValue(field_5_options, -1));
    	// Set formatting block flags to 0 (no formatting blocks)
    	setOptions(fmtBlockBits.setValue(field_5_options, 0));
    	
    	field_6_not_used = 0;
    	fontFormatting=null;
        field_8_align_text_break = 0;
        field_9_align_text_rotation_angle = 0;
        field_10_align_indentation = 0;
        field_11_relative_indentation = 0;
        field_12_not_used = 0;
    	borderFormatting=null;
    	patternFormatting=null;
    	field_17_formula1=null;
    	field_18_formula2=null;
    }

    /**
     * Constructs a Formula record and sets its fields appropriately.
     * Note - id must be 0x06 (NOT 0x406 see MSKB #Q184647 for an 
     * "explanation of this bug in the documentation) or an exception
     *  will be throw upon validation
     *
     * @param in the RecordInputstream to read the record from
     */

    public CFRuleRecord(RecordInputStream in)
    {
        super(in);
    }

    protected void fillFields(RecordInputStream in)
    {
        try {
          field_1_condition_type		= in.readByte();
          field_2_comparison_operator   = in.readByte();
          field_3_formula1_len			= in.readShort();
          field_4_formula2_len			= in.readShort();
          field_5_options				= in.readInt();
          field_6_not_used              = in.readShort();

          if(containsFontFormattingBlock())
          {
        	fontFormatting = new FontFormatting(in);
          }

          if(containsBorderFormattingBlock())
          {
        	  borderFormatting = new BorderFormatting(in);
          }
		   
          if( containsPatternFormattingBlock())
          {
        	  patternFormatting =  new PatternFormatting(in);
          }
          
          if(field_3_formula1_len>0)
          {
              field_17_formula1 = Ptg.createParsedExpressionTokens(field_3_formula1_len, in);
              
              // Now convert any fields as required
              field_17_formula1 = SharedFormulaRecord.convertSharedFormulas(
                  field_17_formula1, 0, 0
              );
          }
          if(field_4_formula2_len>0)
          {
              field_18_formula2 = Ptg.createParsedExpressionTokens(field_4_formula2_len, in);
              
              // Now convert any fields as required
              field_18_formula2 = SharedFormulaRecord.convertSharedFormulas(
                  field_18_formula2, 0, 0
              );
          }
        } catch (java.lang.UnsupportedOperationException uoe)  {
          throw new RecordFormatException(uoe);
        }
        
    }
    
    public void setConditionType(byte conditionType)
    {
    	field_1_condition_type = 
    		conditionType == CONDITION_TYPE_CELL_VALUE_IS ? 
    			CONDITION_TYPE_CELL_VALUE_IS :
				CONDITION_TYPE_FORMULA;
    }

    public byte getConditionType()
    {
    	return field_1_condition_type;
    }

    /**
     * set the option flags
     *
     * @param options  bitmask
     */

    public void setOptions(int options)
    {
        field_5_options = options;
    }

    public boolean containsFontFormattingBlock()
    {
    	return getOptionFlag(font);
    }
    public void setFontFormatting(FontFormatting fontFormatting)
    {
    	this.fontFormatting = fontFormatting;
    	setOptionFlag(true,font);
    }
    public void setFontFormattingUnchanged()
    {
    	setOptionFlag(false,font);
    }
    
    public boolean containsAlignFormattingBlock()
    {
    	return getOptionFlag(align);
    }
    public void setAlignFormattingUnchanged()
    {
    	setOptionFlag(false,align);
    }
    
    public boolean containsBorderFormattingBlock()
    {
    	return getOptionFlag(bord);
    }
    public void setBorderFormatting(BorderFormatting borderFormatting)
    {
    	this.borderFormatting = borderFormatting;
    	setOptionFlag(true,bord);
    }
    public void setBorderFormattingUnchanged()
    {
    	setOptionFlag(false,bord);
    }
    
    public boolean containsPatternFormattingBlock()
    {
    	return getOptionFlag(patt);
    }
    public void setPatternFormatting(PatternFormatting patternFormatting)
    {
    	this.patternFormatting = patternFormatting;
    	setOptionFlag(true,patt);
    }
    public void setPatternFormattingUnchanged()
    {
    	setOptionFlag(false,patt);
    }
    
    public boolean containsProtectionFormattingBlock()
    {
    	return getOptionFlag(prot);
    }
    public void setProtectionFormattingUnchanged()
    {
    	setOptionFlag(false,prot);
    }
    
    public void setComparisonOperation(byte operation)
    {
    	field_2_comparison_operator = operation;
    }

    public byte getComparisonOperation()
    {
    	return field_2_comparison_operator;
    }
    
    /**
     * set the length (in number of tokens) of the expression 1
     * @param len  length
     */

    private void setExpression1Length(short len)
    {
        field_3_formula1_len = len;
    }

    /**
     * get the length (in number of tokens) of the expression 1
     * @return  expression length
     */

    public short getExpression1Length()
    {
        return field_3_formula1_len;
    }
    
    /**
     * set the length (in number of tokens) of the expression 2
     * @param len  length
     */

    private void setExpression2Length(short len)
    {
    	field_4_formula2_len = len;
    }

    /**
     * get the length (in number of tokens) of the expression 2
     * @return  expression length
     */

    public short getExpression2Length()
    {
        return field_4_formula2_len;
    }
    /**
     * get the option flags
     *
     * @return bit mask
     */
    public int getOptions()
    {
        return field_5_options;
    }    

    private boolean isModified(BitField field)
	{
    	return !field.isSet(field_5_options);
	}

	private void setModified(boolean modified, BitField field)
	{
		field_5_options = field.setBoolean(field_5_options, !modified);
	}
	
	public boolean isLeftBorderModified()
	{
		return isModified(bordLeft);
	}

	public void setLeftBorderModified(boolean modified)
	{
		setModified(modified,bordLeft);
	}
    
	public boolean isRightBorderModified()
	{
		return isModified(bordRight);
	}

	public void setRightBorderModified(boolean modified)
	{
		setModified(modified,bordRight);
	}
    
	public boolean isTopBorderModified()
	{
		return isModified(bordTop);
	}

	public void setTopBorderModified(boolean modified)
	{
		setModified(modified,bordTop);
	}
    
	public boolean isBottomBorderModified()
	{
		return isModified(bordBot);
	}

	public void setBottomBorderModified(boolean modified)
	{
		setModified(modified,bordBot);
	}
    
	public boolean isTopLeftBottomRightBorderModified()
	{
		return isModified(bordTlBr);
	}

	public void setTopLeftBottomRightBorderModified(boolean modified)
	{
		setModified(modified,bordTlBr);
	}
    
	public boolean isBottomLeftTopRightBorderModified()
	{
		return isModified(bordBlTr);
	}

	public void setBottomLeftTopRightBorderModified(boolean modified)
	{
		setModified(modified,bordBlTr);
	}
    
	public boolean isPatternStyleModified()
	{
		return isModified(pattStyle);
	}

	public void setPatternStyleModified(boolean modified)
	{
		setModified(modified,pattStyle);
	}
	
	public boolean isPatternColorModified()
	{
		return isModified(pattCol);
	}

	public void setPatternColorModified(boolean modified)
	{
		setModified(modified,pattCol);
	}
	
	public boolean isPatternBackgroundColorModified()
	{
		return isModified(pattBgCol);
	}

	public void setPatternBackgroundColorModified(boolean modified)
	{
		setModified(modified,pattBgCol);
	}
	
    private boolean getOptionFlag(BitField field)
	{
    	return field.isSet(field_5_options);
	}

	private void setOptionFlag(boolean flag, BitField field)
	{
		field_5_options = field.setBoolean(field_5_options, flag);
	}
	
    /**
     * get the stack of the 1st expression as a list
     *
     * @return list of tokens (casts stack to a list and returns it!)
     * this method can return null is we are unable to create Ptgs from 
     *     existing excel file
     * callers should check for null!
     */

    public List getParsedExpression1()
    {
        return field_17_formula1;
    }

    /**
     * get the stack of the 2nd expression as a list
     *
     * @return list of tokens (casts stack to a list and returns it!)
     * this method can return null is we are unable to create Ptgs from 
     *     existing excel file
     * callers should check for null!
     */

    public List getParsedExpression2()
    {
        return field_18_formula2;
    }
    
    public void setParsedExpression1(Stack ptgs) {
      setExpression1Length(getTotalPtgSize(field_17_formula1 = ptgs));
    }

    public void setParsedExpression2(Stack ptgs) {
        setExpression1Length(getTotalPtgSize(field_18_formula2 = ptgs));
    }
    
    /**
     * called by constructor, should throw runtime exception in the event of a
     * record passed with a differing ID.
     *
     * @param id alleged id for this record
     */

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A CFRULE RECORD");
        }
    }

    public short getSid()
    {
        return sid;
    }

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @param offset to begin writing at
     * @param data byte array containing instance data
     * @return number of bytes written
     */

    public int serialize(int offset, byte [] data)
    {
    	int recordsize = getRecordSize();
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(recordsize-4));
        data[4 + offset] = field_1_condition_type;
        data[5 + offset] = field_2_comparison_operator;
        LittleEndian.putShort(data, 6 + offset, field_3_formula1_len);
        LittleEndian.putShort(data, 8 + offset, field_4_formula2_len);
        LittleEndian.putInt(data,  10 + offset, field_5_options);
        LittleEndian.putShort(data,14 + offset, field_6_not_used);
        
        offset += 16;
        
        if( containsFontFormattingBlock() )
        {
        	byte[] fontFormattingRawRecord  = fontFormatting.getRawRecord();
        	System.arraycopy(fontFormattingRawRecord, 0, data, offset, fontFormattingRawRecord.length);
        	offset += fontFormattingRawRecord.length;
        }
        
        if( containsBorderFormattingBlock())
        {
        	offset += borderFormatting.serialize(offset, data);
        }
        
        if( containsPatternFormattingBlock() )
        {
        	offset += patternFormatting.serialize(offset, data);
        }
        
        if (getExpression1Length()>0)
        {
            Ptg.serializePtgStack(this.field_17_formula1, data, offset);
            offset += getExpression1Length();
        }

        if (getExpression2Length()>0)
        {
            Ptg.serializePtgStack(this.field_18_formula2, data, offset);
            offset += getExpression2Length();
        }
        return recordsize;
    }
    
    
    

    public int getRecordSize()
    {
        int retval =16+
        			(containsFontFormattingBlock()?fontFormatting.getRawRecord().length:0)+
        			(containsBorderFormattingBlock()?8:0)+
        			(containsPatternFormattingBlock()?4:0)+
        			getExpression1Length()+
        			getExpression2Length()
        			;
        return retval;
    }

    private short getTotalPtgSize(List list)
    {
    	short  retval = 0;
    	if( list!=null)
    	{
            for (int k = 0; k < list.size(); k++)
            {
                Ptg ptg = ( Ptg ) list.get(k);

                retval += ptg.getSize();
            }
    	}
        return retval;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[CFRULE]\n");
        if( containsFontFormattingBlock())
        {
            buffer.append(fontFormatting.toString());
        }
        if( containsBorderFormattingBlock())
        {
            buffer.append(borderFormatting.toString());
        }
        if( containsPatternFormattingBlock())
        {
            buffer.append(patternFormatting.toString());
        }
        buffer.append("[/CFRULE]\n");
        return buffer.toString();
    }
    
    public Object clone() {
      CFRuleRecord rec = new CFRuleRecord();
      rec.field_1_condition_type= field_1_condition_type;
      rec.field_2_comparison_operator = field_2_comparison_operator;
      rec.field_3_formula1_len = field_3_formula1_len;
      rec.field_4_formula2_len = field_4_formula2_len;
      rec.field_5_options = field_5_options;
      rec.field_6_not_used = field_6_not_used;
      if( containsFontFormattingBlock())
      {
    	  rec.fontFormatting = (FontFormatting)fontFormatting.clone();
      }
      if( containsBorderFormattingBlock())
      {
    	  rec.borderFormatting = (BorderFormatting)borderFormatting.clone();
      }
      if( containsPatternFormattingBlock())
      {
    	  rec.patternFormatting = (PatternFormatting)patternFormatting.clone();
      }
      if( field_3_formula1_len>0)
      {
          rec.field_17_formula1 = (Stack)field_17_formula1.clone();
      }
      if( field_4_formula2_len>0)
      {
          rec.field_18_formula2 = (Stack)field_18_formula2.clone();
      }
      
      return rec;
    }

	public FontFormatting getFontFormatting()
	{
		return fontFormatting;
	}

}
