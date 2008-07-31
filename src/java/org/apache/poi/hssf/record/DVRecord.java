/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

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

package org.apache.poi.hssf.record;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.util.HSSFCellRangeAddress;
import org.apache.poi.hssf.util.HSSFCellRangeAddress.AddrStructure;
import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * Title:        DATAVALIDATION Record (0x01BE)<p/>
 * Description:  This record stores data validation settings and a list of cell ranges
 *               which contain these settings. The data validation settings of a sheet
 *               are stored in a sequential list of DV records. This list is followed by
 *               DVAL record(s)
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 * @version 2.0-pre
 */
public final class DVRecord extends Record
{
    public final static short sid = 0x01BE;

    /**
     * Option flags
     */
    private int                field_option_flags;

    /**
     * Title of the prompt box
     */
    private String             field_title_prompt;

    /**
     * Title of the error box
     */
    private String             field_title_error;

    /**
     * Text of the prompt box
     */
    private String             field_text_prompt;

    /**
     * Text of the error box
     */
    private String             field_text_error;

    /**
     * Size of the formula data for first condition
     */
    private short             field_size_first_formula;

    /**
     * Not used
     */
    private short             field_not_used_1 = 0x3FE0;

    /**
     * Formula data for first condition (RPN token array without size field)
     */
    private Stack             field_rpn_token_1 ;

    /**
     * Size of the formula data for second condition
     */
    private short             field_size_sec_formula;

    /**
     * Not used
     */
    private short             field_not_used_2 = 0x0000;

    /**
     * Formula data for second condition (RPN token array without size field)
     */
    private Stack             field_rpn_token_2 ;

    /**
     * Cell range address list with all affected ranges
     */
    private HSSFCellRangeAddress         field_regions;

    public static final Integer STRING_PROMPT_TITLE = new Integer(0);
    public static final Integer STRING_ERROR_TITLE  = new Integer(1);
    public static final Integer STRING_PROMPT_TEXT  = new Integer(2);
    public static final Integer STRING_ERROR_TEXT   = new Integer(3);
    private Hashtable _hash_strings ;

    /**
     * Option flags field
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    private BitField          opt_data_type                    = new BitField(0x0000000F);
    private BitField          opt_error_style                  = new BitField(0x00000070);
    private BitField          opt_string_list_formula          = new BitField(0x00000080);
    private BitField          opt_empty_cell_allowed           = new BitField(0x00000100);
    private BitField          opt_suppress_dropdown_arrow      = new BitField(0x00000200);
    private BitField          opt_show_prompt_on_cell_selected = new BitField(0x00040000);
    private BitField          opt_show_error_on_invalid_value  = new BitField(0x00080000);
    private BitField          opt_condition_operator           = new BitField(0x00F00000);

    public DVRecord()
    {
    }

    /**
     * Constructs a DV record and sets its fields appropriately.
     *
     * @param in the RecordInputstream to read the record from
     */

    public DVRecord(RecordInputStream in)
    {
        super(in);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT a valid DV RECORD");
        }
    }

    protected void fillFields(RecordInputStream in)
    {
       field_rpn_token_1 = new Stack();
       field_rpn_token_2 = new Stack();
        
       this.field_option_flags = in.readInt();
       this._hash_strings = new Hashtable(4);
       
       StringHandler strHandler_prompt_title = new StringHandler( in );
       this.field_title_prompt = strHandler_prompt_title.getStringData();
       this._hash_strings.put(DVRecord.STRING_PROMPT_TITLE, strHandler_prompt_title);

       StringHandler strHandler_error_title = new StringHandler( in );
       this.field_title_error = strHandler_error_title.getStringData();
       this._hash_strings.put(DVRecord.STRING_ERROR_TITLE, strHandler_error_title);

       StringHandler strHandler_prompt_text = new StringHandler( in );
       this.field_text_prompt = strHandler_prompt_text.getStringData();
       this._hash_strings.put(DVRecord.STRING_PROMPT_TEXT, strHandler_prompt_text);

       StringHandler strHandler_error_text = new StringHandler( in );
       this.field_text_error = strHandler_error_text.getStringData();
       this._hash_strings.put(DVRecord.STRING_ERROR_TEXT, strHandler_error_text);

       this.field_size_first_formula = in.readShort(); 
       this.field_not_used_1 = in.readShort();

       //read first formula data condition
       int token_pos = 0;
       while (token_pos < this.field_size_first_formula)
       {
           Ptg ptg = Ptg.createPtg(in);
           token_pos += ptg.getSize();
           field_rpn_token_1.push(ptg);
       }

       this.field_size_sec_formula = in.readShort(); 
       this.field_not_used_2 = in.readShort();

       //read sec formula data condition
       if (false) { // TODO - prior to bug 44710 this 'skip' was being executed. write a junit to confirm this fix
           try {
               in.skip(this.field_size_sec_formula);
           } catch(IOException e) {
               e.printStackTrace();
               throw new IllegalStateException(e.getMessage());
           }
       }
       token_pos = 0;
       while (token_pos < this.field_size_sec_formula)
       {
           Ptg ptg = Ptg.createPtg(in);
           token_pos += ptg.getSize();
           field_rpn_token_2.push(ptg);
       }

       //read cell range address list with all affected ranges
       this.field_regions = new HSSFCellRangeAddress(in);
    }


    // --> start option flags
    /**
     * set the condition data type
     * @param type - condition data type
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public void setDataType(int type)
    {
        this.field_option_flags =  this.opt_data_type.setValue(this.field_option_flags, type);
    }

    /**
     * get the condition data type
     * @return the condition data type
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public int getDataType()
    {
       return this.opt_data_type.getValue(this.field_option_flags);
    }

    /**
     * set the condition error style
     * @param type - condition error style
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public void setErrorStyle(int style)
    {
        this.field_option_flags =  this.opt_error_style.setValue(this.field_option_flags, style);
    }

    /**
     * get the condition error style
     * @return the condition error style
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public int getErrorStyle()
    {
       return this.opt_error_style.getValue(this.field_option_flags);
    }

    /**
     * set if in list validations the string list is explicitly given in the formula
     * @param type - true if in list validations the string list is explicitly given in the formula; false otherwise
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public void setListExplicitFormula(boolean explicit)
    {
        this.field_option_flags = this.opt_string_list_formula.setBoolean(this.field_option_flags, explicit);
    }

    /**
     * return true if in list validations the string list is explicitly given in the formula, false otherwise
     * @return true if in list validations the string list is explicitly given in the formula, false otherwise
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public boolean getListExplicitFormula()
    {
       return (this.opt_string_list_formula.isSet(this.field_option_flags));
    }

    /**
     * set if empty values are allowed in cells
     * @param type - true if empty values are allowed in cells, false otherwise
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public void setEmptyCellAllowed(boolean allowed)
    {
        this.field_option_flags =  this.opt_empty_cell_allowed.setBoolean(this.field_option_flags, allowed);
    }

    /**
     * return true if empty values are allowed in cells, false otherwise
     * @return if empty values are allowed in cells, false otherwise
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public boolean getEmptyCellAllowed()
    {
       return (this.opt_empty_cell_allowed.isSet(this.field_option_flags));
    }
    /**
     * @deprecated - (Jul-2008) use setSuppressDropDownArrow
      */
    public void setSurppresDropdownArrow(boolean suppress) {
        setSuppressDropdownArrow(suppress);
    }
    /**
     * @deprecated - (Jul-2008) use getSuppressDropDownArrow
      */
    public boolean getSurppresDropdownArrow() {
        return getSuppressDropdownArrow();
    }

    /**
     * set if drop down arrow should be suppressed when list validation is used
     * @param type - true if drop down arrow should be suppressed when list validation is used, false otherwise
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public void setSuppressDropdownArrow(boolean suppress)
    {
        this.field_option_flags =  this.opt_suppress_dropdown_arrow.setBoolean(this.field_option_flags, suppress);
    }

    /**
     * return true if drop down arrow should be suppressed when list validation is used, false otherwise
     * @return if drop down arrow should be suppressed when list validation is used, false otherwise
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public boolean getSuppressDropdownArrow()
    {
       return (this.opt_suppress_dropdown_arrow.isSet(this.field_option_flags));
    }

    /**
     * set if a prompt window should appear when cell is selected
     * @param type - true if a prompt window should appear when cell is selected, false otherwise
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public void setShowPromptOnCellSelected(boolean show)
    {
        this.field_option_flags =  this.opt_show_prompt_on_cell_selected.setBoolean(this.field_option_flags, show);
    }

    /**
     * return true if a prompt window should appear when cell is selected, false otherwise
     * @return if a prompt window should appear when cell is selected, false otherwise
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public boolean getShowPromptOnCellSelected()
    {
       return (this.opt_show_prompt_on_cell_selected.isSet(this.field_option_flags));
    }

    /**
     * set if an error window should appear when an invalid value is entered in the cell
     * @param type - true if an error window should appear when an invalid value is entered in the cell, false otherwise
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public void setShowErrorOnInvalidValue(boolean show)
    {
        this.field_option_flags =  this.opt_show_error_on_invalid_value.setBoolean(this.field_option_flags, show);
    }

    /**
     * return true if an error window should appear when an invalid value is entered in the cell, false otherwise
     * @return if an error window should appear when an invalid value is entered in the cell, false otherwise
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public boolean getShowErrorOnInvalidValue()
    {
       return (this.opt_show_error_on_invalid_value.isSet(this.field_option_flags));
    }

    /**
     * set the condition operator
     * @param type - condition operator
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public void setConditionOperator(int operator)
    {
        this.field_option_flags =  this.opt_condition_operator.setValue(this.field_option_flags, operator);
    }

    /**
     * get the condition operator
     * @return the condition operator
     * @see org.apache.poi.hssf.util.HSSFDataValidation utility class
     */
    public int getConditionOperator()
    {
       return this.opt_condition_operator.getValue(this.field_option_flags);
    }
    // <-- end option flags

    public void setFirstFormulaRPN( Stack rpn )
    {
        this.field_rpn_token_1 = rpn;
    }

    public void setFirstFormulaSize( short size )
    {
        this.field_size_first_formula = size;
    }

    public void setSecFormulaRPN( Stack rpn )
    {
        this.field_rpn_token_2 = rpn;
    }

    public void setSecFormulaSize( short size )
    {
        this.field_size_sec_formula = size;
    }

    public void setStringField( Integer type, String str_data )
    {
       if ( this._hash_strings == null )
       {
          this._hash_strings = new Hashtable();
       }
       StringHandler strHandler = new StringHandler();
       if ( str_data == null )
       {
          str_data = "";
       }
       else
       {
          strHandler.setStringLength(str_data.length());
       }
       strHandler.setStringData(str_data);

       strHandler.setUnicodeFlag((byte)0x00);
       this._hash_strings.put( type, strHandler);
    }

    public String getStringField( Integer type )
    {
        return ((StringHandler)this._hash_strings.get(type)).getStringData();
    }

    public void setCellRangeAddress( HSSFCellRangeAddress range )
    {
        this.field_regions = range;
    }

    public HSSFCellRangeAddress getCellRangeAddress( )
    {
        return this.field_regions;
    }

    /**
     * gets the option flags field.
     * @return options - the option flags field
     */
    public int getOptionFlags()
    {
       return this.field_option_flags;
    }

    public String toString()
    {
      /** @todo DVRecord string representation */
        StringBuffer sb = new StringBuffer();
        sb.append("[DV]\n");
        sb.append(" options=").append(Integer.toHexString(field_option_flags));
        sb.append(" title-prompt=").append(field_title_prompt);
        sb.append(" title-error=").append(field_title_error);
        sb.append(" text-prompt=").append(field_text_prompt);
        sb.append(" text-error=").append(field_text_error);
        sb.append("\n");
        appendFormula(sb, "Formula 1:",  field_rpn_token_1);
        appendFormula(sb, "Formula 2:",  field_rpn_token_2);
        int nRegions = field_regions.getADDRStructureNumber();
        for(int i=0; i<nRegions; i++) {
            AddrStructure addr = field_regions.getADDRStructureAt(i);
            sb.append('(').append(addr.getFirstRow()).append(',').append(addr.getLastRow());
            sb.append(',').append(addr.getFirstColumn()).append(',').append(addr.getLastColumn()).append(')');
        }
        sb.append("\n");
        sb.append("[/DV]");

        return sb.toString();
    }

    private void appendFormula(StringBuffer sb, String label, Stack stack) {
        sb.append(label);
        if (stack.isEmpty()) {
            sb.append("<empty>\n");
            return;
        }
        sb.append("\n");
        Ptg[] ptgs = new Ptg[stack.size()];
        stack.toArray(ptgs);
        for (int i = 0; i < ptgs.length; i++) {
            sb.append('\t').append(ptgs[i].toString()).append('\n');
        }
    }

    public int serialize(int offset, byte [] data)
    {
        int size = this.getRecordSize();
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) (size-4));

        int pos = 4;
        LittleEndian.putInt(data, pos + offset, this.getOptionFlags());
        pos += 4;
        pos += ((StringHandler)this._hash_strings.get( DVRecord.STRING_PROMPT_TITLE )).serialize(pos+offset, data);
        pos += ((StringHandler)this._hash_strings.get( DVRecord.STRING_ERROR_TITLE )).serialize(pos+offset, data);
        pos += ((StringHandler)this._hash_strings.get( DVRecord.STRING_PROMPT_TEXT )).serialize(pos+offset, data);
        pos += ((StringHandler)this._hash_strings.get( DVRecord.STRING_ERROR_TEXT )).serialize(pos+offset, data);
        LittleEndian.putShort(data, offset+pos, this.field_size_first_formula);
        pos += 2;
        LittleEndian.putShort(data, offset+pos, this.field_not_used_1);
        pos += 2;

        for (int k = 0; k < this.field_rpn_token_1.size(); k++)
        {
            Ptg ptg = ( Ptg ) this.field_rpn_token_1.get(k);
            ptg.writeBytes(data, pos+offset);
            pos += ptg.getSize();
        }

        LittleEndian.putShort(data, offset+pos, this.field_size_sec_formula);
        pos += 2;
        LittleEndian.putShort(data, offset+pos, this.field_not_used_2);
        pos += 2;
        if ( this.field_size_sec_formula > 0 )
        {
          for (int k = 0; k < this.field_rpn_token_2.size(); k++)
          {
              Ptg ptg = ( Ptg ) this.field_rpn_token_2.get(k);
              ptg.writeBytes(data, pos+offset);
              pos += ptg.getSize();
          }
        }
        this.field_regions.serialize(pos+offset, data);
        return size;
    }

    public int getRecordSize()
    {
        int size = 4+4+2+2+2+2;//header+options_field+first_formula_size+first_unused+sec_formula_size+sec+unused;
        if ( this._hash_strings != null )
        {
            Enumeration enum_keys = this._hash_strings.keys();
            while ( enum_keys.hasMoreElements() )
            {
                size += ((StringHandler)this._hash_strings.get( (Integer)enum_keys.nextElement() )).getSize();
            }
        }
        size += this.field_size_first_formula+ this.field_size_sec_formula;
        size += this.field_regions.getSize();
        return size;
    }

    public short getSid()
    {
        return this.sid;
    }
    
    /**
     * Clones the object. Uses serialisation, as the
     *  contents are somewhat complex
     */
    public Object clone() {
        return cloneViaReserialise();
    }

    /**@todo DVRecord = Serializare */

    private static final class StringHandler
    {
        private int     _string_length       = 0x0001;
        private byte    _string_unicode_flag = 0x00;
        private String  _string_data         = "0x00";
        private int     _start_offset;
        private int     _end_offset;

        StringHandler()
        {

        }

        StringHandler(RecordInputStream in)
        {
            this.fillFields(in);
        }

        protected void fillFields(RecordInputStream in) 
        {
            this._string_length       = in.readUShort(); 
            this._string_unicode_flag = in.readByte(); 
            if (this._string_unicode_flag == 1)
            {
                this._string_data = in.readUnicodeLEString(this._string_length);
            }
            else
            {
                this._string_data = in.readCompressedUnicode(this._string_length);
            }
        }

        private void setStringData( String string_data )
        {
          this._string_data = string_data;
        }

        private String getStringData()
        {
            return this._string_data;
        }

        private int getEndOffset()
        {
            return this._end_offset;
        }

        public int serialize( int offset, byte[] data )
        {
            LittleEndian.putUShort(data, offset, this._string_length );
            data[2 + offset] = this._string_unicode_flag;
            if (this._string_unicode_flag == 1)
            {
                StringUtil.putUnicodeLE(this._string_data, data, 3 + offset);
            }
            else
            {
                StringUtil.putCompressedUnicode(this._string_data, data, 3 + offset);
            }
            return getSize();
        }

        private void setUnicodeFlag( byte flag )
        {
            this._string_unicode_flag = flag;
        }

        private void setStringLength( int len )
        {
           this._string_length = len;
        }

        private int getStringByteLength()
        {
            return (this._string_unicode_flag == 1) ? this._string_length * 2 : this._string_length;
        }

        public int getSize()
        {
            return 2 + 1 + getStringByteLength();
        }
    }
}
