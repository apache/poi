
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

import java.util.List;
import java.util.Stack;

import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import org.apache.poi.hssf.util.RangeAddress;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * Title:        Name Record (aka Named Range) <P>
 * Description:  Defines a named range within a workbook. <P>
 * REFERENCE:  <P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @author  Sergei Kozello (sergeikozello at mail.ru)
 * @author Glen Stampoultzis (glens at apache.org)
 * @version 1.0-pre
 */

public class NameRecord extends Record {
    /**
     */
    public final static short sid = 0x18; //Docs says that it is 0x218
    
	/**Included for completeness sake, not implemented
	   */
	public final static byte  BUILTIN_CONSOLIDATE_AREA      = (byte)1;
	
	/**Included for completeness sake, not implemented
	 */
	public final static byte  BUILTIN_AUTO_OPEN             = (byte)2;

	/**Included for completeness sake, not implemented
	 */
	public final static byte  BUILTIN_AUTO_CLOSE            = (byte)3;

	/**Included for completeness sake, not implemented
	 */
	public final static byte  BUILTIN_DATABASE              = (byte)4;

	/**Included for completeness sake, not implemented
	 */
	public final static byte  BUILTIN_CRITERIA              = (byte)5;
	
	public final static byte  BUILTIN_PRINT_AREA            = (byte)6;
	public final static byte  BUILTIN_PRINT_TITLE           = (byte)7;
	
	/**Included for completeness sake, not implemented
	 */
	public final static byte  BUILTIN_RECORDER              = (byte)8;
	
	/**Included for completeness sake, not implemented
	 */
	public final static byte  BUILTIN_DATA_FORM             = (byte)9;
	
	/**Included for completeness sake, not implemented
	 */

	public final static byte  BUILTIN_AUTO_ACTIVATE         = (byte)10;
	
	/**Included for completeness sake, not implemented
	 */

	public final static byte  BUILTIN_AUTO_DEACTIVATE       = (byte)11;
	
	/**Included for completeness sake, not implemented
	 */
	public final static byte  BUILTIN_SHEET_TITLE           = (byte)12;
    
    public static final short OPT_HIDDEN_NAME =   (short) 0x0001;
    public static final short OPT_FUNCTION_NAME = (short) 0x0002;
    public static final short OPT_COMMAND_NAME =  (short) 0x0004;
    public static final short OPT_MACRO =         (short) 0x0008;
    public static final short OPT_COMPLEX =       (short) 0x0010;
    public static final short OPT_BUILTIN =       (short) 0x0020;
    public static final short OPT_BINDATA =       (short) 0x1000;

    
    private short             field_1_option_flag;
    private byte              field_2_keyboard_shortcut;
    private byte              field_3_length_name_text;
    private short             field_4_length_name_definition;
    private short             field_5_index_to_sheet;     // unused: see field_6
    private short             field_6_equals_to_index_to_sheet;
    private byte              field_7_length_custom_menu;
    private byte              field_8_length_description_text;
    private byte              field_9_length_help_topic_text;
    private byte              field_10_length_status_bar_text;
    private byte              field_11_compressed_unicode_flag;   // not documented
    private byte              field_12_builtIn_name;
    private String            field_12_name_text;
    private Stack             field_13_name_definition;
    private byte[]            field_13_raw_name_definition;       // raw data
    private String            field_14_custom_menu_text;
    private String            field_15_description_text;
    private String            field_16_help_topic_text;
    private String            field_17_status_bar_text;


    /** Creates new NameRecord */
    public NameRecord() {
        field_13_name_definition = new Stack();

        field_12_name_text = new String();
        field_14_custom_menu_text = new String();
        field_15_description_text = new String();
        field_16_help_topic_text = new String();
        field_17_status_bar_text = new String();
    }

    /**
     * Constructs a Name record and sets its fields appropriately.
     *
     * @param id     id must be 0x18 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */
    public NameRecord(short id, short size, byte [] data) {
        super(id, size, data);
    }

    /**
     * Constructs a Name record and sets its fields appropriately.
     *
     * @param id     id must be 0x18 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */
    public NameRecord(short id, short size, byte [] data, int offset) {
        super(id, size, data, offset);
    }

	/**
	 * Constructor to create a built-in named region
	 * @param builtin Built-in byte representation for the name record, use the public constants
	 * @param index 
	 */
	public NameRecord(byte builtin, short index)
	{
	    this();	    
	    this.field_12_builtIn_name = builtin;
	    this.setOptionFlag((short)(this.getOptionFlag() | OPT_BUILTIN));
	    this.setNameTextLength((byte)1);
	    this.setEqualsToIndexToSheet(index); //the extern sheets are set through references
	    
	    //clearing these because they are not used with builtin records
		this.setCustomMenuLength((byte)0);
		this.setDescriptionTextLength((byte)0);
		this.setHelpTopicLength((byte)0);
		this.setStatusBarLength((byte)0);

	    
	}

    /** sets the option flag for the named range
     * @param flag option flag
     */
    public void setOptionFlag(short flag){
        field_1_option_flag = flag;
    }


    /** sets the keyboard shortcut
     * @param shortcut keyboard shortcut
     */
    public void setKeyboardShortcut(byte shortcut){
        field_2_keyboard_shortcut = shortcut;
    }

    /** sets the name of the named range length
     * @param length name length
     */
    public void setNameTextLength(byte length){
        field_3_length_name_text = length;
    }

    /** sets the definition (reference - formula) length
     * @param length defenition length
     */
    public void setDefinitionTextLength(short length){
        field_4_length_name_definition = length;
    }

    /** sets the index number to the extern sheet (thats is what writen in documentation
     *  but as i saw , it works differently)
     * @param index extern sheet index
     */
    public void setUnused(short index){
        field_5_index_to_sheet = index;

        // field_6_equals_to_index_to_sheet is equal to field_5_index_to_sheet
//        field_6_equals_to_index_to_sheet = index;
    }

    public short getEqualsToIndexToSheet()
    {
        return field_6_equals_to_index_to_sheet;
    }

	/**
	 * Convenience method to retrieve the index the name refers to.
	 * @see #getEqualsToIndexToSheet()
	 * @return short
	 */
	public short getIndexToSheet() {
		return getEqualsToIndexToSheet();
	}

    /**
     * @return function group
     * @see FnGroupCountRecord
     */
    public byte getFnGroup() {
        int masked = field_1_option_flag & 0x0fc0;
        return (byte) (masked >> 4);
    }

    public void setEqualsToIndexToSheet(short value)
    {
        field_6_equals_to_index_to_sheet = value;
    }


    /** sets the custom menu length
     * @param length custom menu length
     */
    public void setCustomMenuLength(byte length){
        field_7_length_custom_menu = length;
    }

    /** sets the length of named range description
     * @param length description length
     */
    public void setDescriptionTextLength(byte length){
        field_8_length_description_text = length;
    }

    /** sets the help topic length
     * @param length help topic length
     */
    public void setHelpTopicLength(byte length){
        field_9_length_help_topic_text = length;
    }

    /** sets the length of the status bar text
     * @param length status bar text length
     */
    public void setStatusBarLength(byte length){
        field_10_length_status_bar_text = length;
    }

    /** sets the compressed unicode flag
     * @param flag unicode flag
     */
    public void setCompressedUnicodeFlag(byte flag) {
        field_11_compressed_unicode_flag = flag;
    }

    /** sets the name of the named range
     * @param name named range name
     */
    public void setNameText(String name){
        field_12_name_text = name;
    }

    //    public void setNameDefintion(String definition){
    //        test = definition;
    //    }

    /** sets the custom menu text
     * @param text custom menu text
     */
    public void setCustomMenuText(String text){
        field_14_custom_menu_text = text;
    }

    /** sets the description text
     * @param text the description text
     */
    public void setDescriptionText(String text){
        field_15_description_text = text;
    }

    /** sets the help topic text
     * @param text help topix text
     */
    public void setHelpTopicText(String text){
        field_16_help_topic_text = text;
    }

    /** sets the status bar text
     * @param text status bar text
     */
    public void setStatusBarText(String text){
        field_17_status_bar_text = text;
    }

    /** gets the option flag
     * @return option flag
     */
    public short getOptionFlag(){
        return field_1_option_flag;
    }

    /** returns the keyboard shortcut
     * @return keyboard shortcut
     */
    public byte getKeyboardShortcut(){
        return field_2_keyboard_shortcut ;
    }

    /** gets the name length
     * @return name length
     */
    public byte getNameTextLength(){
        return field_3_length_name_text;
    }

    /** get the definition length
     * @return definition length
     */
    public short getDefinitionTextLength(){
        return field_4_length_name_definition;
    }

    /** gets the index to extern sheet
     * @return index to extern sheet
     */
    public short getUnused(){
        return field_5_index_to_sheet;
    }

    /** gets the custom menu length
     * @return custom menu length
     */
    public byte getCustomMenuLength(){
        return field_7_length_custom_menu;
    }

    /** gets the description text length
     * @return description text length
     */
    public byte getDescriptionTextLength(){
        return field_8_length_description_text;
    }

    /** gets the help topic length
     * @return help topic length
     */
    public byte getHelpTopicLength(){
        return field_9_length_help_topic_text;
    }

    /** get the status bar text length
     * @return satus bar length
     */
    public byte getStatusBarLength(){
        return field_10_length_status_bar_text;
    }

    /** gets the name compressed Unicode flag
     * @return compressed unicode flag
     */
    public byte getCompressedUnicodeFlag() {
        return field_11_compressed_unicode_flag;
    }

    /**
     * @return true if name is hidden
     */
    public boolean isHiddenName() {
        return (field_1_option_flag & OPT_HIDDEN_NAME) != 0;
    }

    /**
     * @return true if name is a function
     */
    public boolean isFunctionName() {
        return (field_1_option_flag & OPT_FUNCTION_NAME) != 0;
    }

    /**
     * @return true if name is a command
     */
    public boolean isCommandName() {
        return (field_1_option_flag & OPT_COMMAND_NAME) != 0;
    }

    /**
     * @return true if function macro or command macro
     */
    public boolean isMacro() {
        return (field_1_option_flag & OPT_MACRO) != 0;
    }

    /**
     * @return true if array formula or user defined
     */
    public boolean isComplexFunction() {
        return (field_1_option_flag & OPT_COMPLEX) != 0;
    }


	/**Convenience Function to determine if the name is a built-in name
	 */
	public boolean isBuiltInName()
	{
	    return ((this.getOptionFlag() & OPT_BUILTIN) != 0);
	}


	/** gets the name
	 * @return name
	 */
	public String getNameText(){

    	return this.isBuiltInName() ? this.translateBuiltInName(this.getBuiltInName()) : field_12_name_text;
	}

	/** Gets the Built In Name
	 * @return the built in Name
	 */
	public byte getBuiltInName()
	{
	    return this.field_12_builtIn_name;
	}


    /** gets the definition, reference (Formula)
     * @return definition -- can be null if we cant parse ptgs
     */
    public List getNameDefinition() {
        return field_13_name_definition;
    }

    public void setNameDefinition(Stack nameDefinition) {
        field_13_name_definition = nameDefinition;
    }

    /** get the custom menu text
     * @return custom menu text
     */
    public String getCustomMenuText(){
        return field_14_custom_menu_text;
    }

    /** gets the description text
     * @return description text
     */
    public String getDescriptionText(){
        return field_15_description_text;
    }

    /** get the help topic text
     * @return gelp topic text
     */
    public String getHelpTopicText(){
        return field_16_help_topic_text;
    }

    /** gets the status bar text
     * @return status bar text
     */
    public String getStatusBarText(){
        return field_17_status_bar_text;
    }

    /**
     * called by constructor, should throw runtime exception in the event of a
     * record passed with a differing ID.
     *
     * @param id alleged id for this record
     */
    protected void validateSid(short id) {
        if (id != sid) {
            throw new RecordFormatException("NOT A valid Name RECORD");
        }
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
    public int serialize( int offset, byte[] data )
    {
        LittleEndian.putShort( data, 0 + offset, sid );
        // size defined below
        LittleEndian.putShort( data, 4 + offset, getOptionFlag() );
        data[6 + offset] = getKeyboardShortcut();
        data[7 + offset] = getNameTextLength();
        LittleEndian.putShort( data, 8 + offset, getDefinitionTextLength() );
        LittleEndian.putShort( data, 10 + offset, getUnused() );
        LittleEndian.putShort( data, 12 + offset, getEqualsToIndexToSheet() );
        data[14 + offset] = getCustomMenuLength();
        data[15 + offset] = getDescriptionTextLength();
        data[16 + offset] = getHelpTopicLength();
        data[17 + offset] = getStatusBarLength();
        data[18 + offset] = getCompressedUnicodeFlag();

        /* temp: gjs
        if (isBuiltInName())
        {
            LittleEndian.putShort( data, 2 + offset, (short) ( 16 + field_13_raw_name_definition.length ) );

            data[19 + offset] = field_12_builtIn_name;
            System.arraycopy( field_13_raw_name_definition, 0, data, 20 + offset, field_13_raw_name_definition.length );

            return 20 + field_13_raw_name_definition.length;
        }
        else
        {     */
            LittleEndian.putShort( data, 2 + offset, (short) ( 15 + getTextsLength() ) );
            
			int start_of_name_definition = 19 + field_3_length_name_text;

			if (this.isBuiltInName()) {
				//can send the builtin name directly in
				data [19 + offset] =  this.getBuiltInName();
			} else {
				StringUtil.putCompressedUnicode( getNameText(), data, 19 + offset );
				
			}


			if ( this.field_13_name_definition != null )
			{
				serializePtgs( data, start_of_name_definition + offset );
			}
			else
			{
				System.arraycopy( field_13_raw_name_definition, 0, data
					, start_of_name_definition + offset, field_13_raw_name_definition.length );
			}				


            int start_of_custom_menu_text = start_of_name_definition + field_4_length_name_definition;
            StringUtil.putCompressedUnicode( getCustomMenuText(), data, start_of_custom_menu_text + offset );

            int start_of_description_text = start_of_custom_menu_text + field_7_length_custom_menu;
            StringUtil.putCompressedUnicode( getDescriptionText(), data, start_of_description_text + offset );

            int start_of_help_topic_text = start_of_description_text + field_8_length_description_text;
            StringUtil.putCompressedUnicode( getHelpTopicText(), data, start_of_help_topic_text + offset );

            int start_of_status_bar_text = start_of_help_topic_text + field_9_length_help_topic_text;
            StringUtil.putCompressedUnicode( getStatusBarText(), data, start_of_status_bar_text + offset );

            return getRecordSize();
        /* } */
    }

    private void serializePtgs(byte [] data, int offset) {
        int pos = offset;

        for (int k = 0; k < field_13_name_definition.size(); k++) {
            Ptg ptg = ( Ptg ) field_13_name_definition.get(k);

            ptg.writeBytes(data, pos);
            pos += ptg.getSize();
        }
    }


    /** gets the length of all texts
     * @return total length
     */
    public int getTextsLength(){
        int result;

        result = getNameTextLength() + getDefinitionTextLength() + getDescriptionTextLength() +
        getHelpTopicLength() + getStatusBarLength();


        return result;
    }

    /** returns the record size
     */
    public int getRecordSize(){
        int result;

        result = 19 + getTextsLength();

        return result;
    }

    /** gets the extern sheet number
     * @return extern sheet index
     */
    public short getExternSheetNumber(){
        if (field_13_name_definition == null) return 0;
        Ptg ptg = (Ptg) field_13_name_definition.peek();
        short result = 0;

        if (ptg.getClass() == Area3DPtg.class){
            result = ((Area3DPtg) ptg).getExternSheetIndex();

        } else if (ptg.getClass() == Ref3DPtg.class){
            result = ((Ref3DPtg) ptg).getExternSheetIndex();
        }

        return result;
    }

    /** sets the extern sheet number
     * @param externSheetNumber extern sheet number
     */
    public void setExternSheetNumber(short externSheetNumber){
        Ptg ptg;

        if (field_13_name_definition == null || field_13_name_definition.isEmpty()){
            field_13_name_definition = new Stack();
            ptg = createNewPtg();
        } else {
            ptg = (Ptg) field_13_name_definition.peek();
        }

        if (ptg.getClass() == Area3DPtg.class){
            ((Area3DPtg) ptg).setExternSheetIndex(externSheetNumber);

        } else if (ptg.getClass() == Ref3DPtg.class){
            ((Ref3DPtg) ptg).setExternSheetIndex(externSheetNumber);
        }

    }

    private Ptg createNewPtg(){
        Ptg ptg = new Area3DPtg();
        field_13_name_definition.push(ptg);

        return ptg;
    }

    /** gets the reference , the area only (range)
     * @return area reference
     */
    public String getAreaReference(Workbook book){
        if (field_13_name_definition == null) return "#REF!";
        Ptg ptg = (Ptg) field_13_name_definition.peek();
        String result = "";

        if (ptg.getClass() == Area3DPtg.class){
            result = ptg.toFormulaString(book);

        } else if (ptg.getClass() == Ref3DPtg.class){
            result = ptg.toFormulaString(book);
        }

        return result;
    }

    /** sets the reference , the area only (range)
     * @param ref area reference
     */
    public void setAreaReference(String ref){
        //Trying to find if what ptg do we need
        RangeAddress ra = new RangeAddress(ref);
        Ptg oldPtg;
        Ptg ptg;

        if (field_13_name_definition==null ||field_13_name_definition.isEmpty()){
            field_13_name_definition = new Stack();
            oldPtg = createNewPtg();
        } else {
            //Trying to find extern sheet index
            oldPtg = (Ptg) field_13_name_definition.pop();
        }

        short externSheetIndex = 0;

        if (oldPtg.getClass() == Area3DPtg.class){
            externSheetIndex =  ((Area3DPtg) oldPtg).getExternSheetIndex();

        } else if (oldPtg.getClass() == Ref3DPtg.class){
            externSheetIndex =  ((Ref3DPtg) oldPtg).getExternSheetIndex();
        }

        if (ra.hasRange()) {
            ptg = new Area3DPtg();
            ((Area3DPtg) ptg).setExternSheetIndex(externSheetIndex);
            ((Area3DPtg) ptg).setArea(ref);
            this.setDefinitionTextLength((short)ptg.getSize());
        } else {
            ptg = new Ref3DPtg();
            ((Ref3DPtg) ptg).setExternSheetIndex(externSheetIndex);
            ((Ref3DPtg) ptg).setArea(ref);
            this.setDefinitionTextLength((short)ptg.getSize());
        }

        field_13_name_definition.push(ptg);

    }

    /**
     * called by the constructor, should set class level fields.  Should throw
     * runtime exception for bad/icomplete data.
     *
     * @param data raw data
     * @param size size of data
     * @param offset of the record's data (provided a big array of the file)
     */
    protected void fillFields(byte[] data, short size, int offset) {
        field_1_option_flag             = LittleEndian.getShort(data, 0 + offset);
        field_2_keyboard_shortcut       = data [2 + offset];
        field_3_length_name_text        = data [3 + offset];
        field_4_length_name_definition  = LittleEndian.getShort(data, 4 + offset);
        field_5_index_to_sheet          = LittleEndian.getShort(data, 6 + offset);
        field_6_equals_to_index_to_sheet= LittleEndian.getShort(data, 8 + offset);
        field_7_length_custom_menu      = data [10 + offset];
        field_8_length_description_text = data [11 + offset];
        field_9_length_help_topic_text  = data [12 + offset];
        field_10_length_status_bar_text = data [13 + offset];
        

        /*
        temp: gjs
        if (isBuiltInName()) {
            // DEBUG
            // System.out.println( "Built-in name" );

            field_11_compressed_unicode_flag = data[ 14 + offset ];
            field_12_builtIn_name = data[ 15 + offset ];

            if ( (field_12_builtIn_name & (short)0x07) != 0 ) {
                field_12_name_text = "Print_Titles";

                // DEBUG
                // System.out.println( field_12_name_text );

                field_13_raw_name_definition = new byte[ field_4_length_name_definition ];
                System.arraycopy( data, 16 + offset, field_13_raw_name_definition, 0, field_13_raw_name_definition.length );

                // DEBUG
                // System.out.println( HexDump.toHex( field_13_raw_name_definition ) );
            }
        }
        else { */
    
            field_11_compressed_unicode_flag= data [14 + offset];
            
            
			//store the name in byte form if it's a builtin name
			if (this.isBuiltInName()) {
				field_12_builtIn_name = data[ 15 + offset ];
			}
            
            field_12_name_text = StringUtil.getFromCompressedUnicode(data, 15 + offset,
            LittleEndian.ubyteToInt(field_3_length_name_text));
        
            int start_of_name_definition    = 15 + field_3_length_name_text;
            field_13_name_definition = getParsedExpressionTokens(data, field_4_length_name_definition,
            offset, start_of_name_definition);
    
            int start_of_custom_menu_text   = start_of_name_definition + field_4_length_name_definition;
            field_14_custom_menu_text       = StringUtil.getFromCompressedUnicode(data, start_of_custom_menu_text + offset,
            LittleEndian.ubyteToInt(field_7_length_custom_menu));
    
            int start_of_description_text   = start_of_custom_menu_text + field_7_length_custom_menu;;
            field_15_description_text       = StringUtil.getFromCompressedUnicode(data, start_of_description_text + offset,
            LittleEndian.ubyteToInt(field_8_length_description_text));
    
            int start_of_help_topic_text    = start_of_description_text + field_8_length_description_text;
            field_16_help_topic_text        = StringUtil.getFromCompressedUnicode(data, start_of_help_topic_text + offset,
            LittleEndian.ubyteToInt(field_9_length_help_topic_text));
    
            int start_of_status_bar_text       = start_of_help_topic_text + field_9_length_help_topic_text;
            field_17_status_bar_text        = StringUtil.getFromCompressedUnicode(data, start_of_status_bar_text +  offset,
            LittleEndian.ubyteToInt(field_10_length_status_bar_text));
        /*} */
    }

    private Stack getParsedExpressionTokens(byte [] data, short size,
        int offset, int start_of_expression) {
        Stack stack = new Stack();
        int   pos           = start_of_expression + offset;
        int   sizeCounter   = 0;
        try {
            while (sizeCounter < size) {
                Ptg ptg = Ptg.createPtg(data, pos);

                pos += ptg.getSize();
                sizeCounter += ptg.getSize();
                stack.push(ptg);
                field_13_raw_name_definition=new byte[size];
                System.arraycopy(data,offset,field_13_raw_name_definition,0,size);
            }
        } catch (java.lang.UnsupportedOperationException uoe) {
            System.err.println("[WARNING] Unknown Ptg "
                    + uoe.getMessage() );
            field_13_raw_name_definition=new byte[size];
            System.arraycopy(data,offset,field_13_raw_name_definition,0,size);
            return null;
        }
        return stack;
    }


    /**
     * return the non static version of the id for this record.
     */
    public short getSid() {
        return this.sid;
    }
    /*
      20 00 
      00 
      01 
      1A 00 // sz = 0x1A = 26
      00 00 
      01 00 
      00 
      00 
      00 
      00 
      00 // unicode flag
      07 // name
      
      29 17 00 3B 00 00 00 00 FF FF 00 00 02 00 3B 00 //{ 26
      00 07 00 07 00 00 00 FF 00 10                   //  }
      
      
      
      20 00 
      00 
      01 
      0B 00 // sz = 0xB = 11
      00 00 
      01 00 
      00 
      00 
      00 
      00 
      00 // unicode flag
      07 // name
      
      3B 00 00 07 00 07 00 00 00 FF 00   // { 11 }
  */
    /*
      18, 00, 
      1B, 00, 
      
      20, 00, 
      00, 
      01, 
      0B, 00, 
      00, 
      00, 
      00, 
      00, 
      00, 
      07, 
      3B 00 00 07 00 07 00 00 00 FF 00 ]     
     */

    /**
     * @see Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[NAME]\n");
        buffer.append("    .option flags         = ").append( HexDump.toHex( field_1_option_flag ) )
            .append("\n");
        buffer.append("    .keyboard shortcut    = ").append( HexDump.toHex( field_2_keyboard_shortcut ) )
            .append("\n");
        buffer.append("    .length of the name   = ").append( field_3_length_name_text )
            .append("\n");
        buffer.append("    .size of the formula data = ").append( field_4_length_name_definition )
            .append("\n");
        buffer.append("    .unused                   = ").append( field_5_index_to_sheet )
            .append("\n");
        buffer.append("    .index to sheet (1-based, 0=Global)           = ").append( field_6_equals_to_index_to_sheet )
            .append("\n");
        buffer.append("    .Length of menu text (character count)        = ").append( field_7_length_custom_menu )
            .append("\n");
        buffer.append("    .Length of description text (character count) = ").append( field_8_length_description_text )
            .append("\n");
        buffer.append("    .Length of help topic text (character count)  = ").append( field_9_length_help_topic_text )
            .append("\n");
        buffer.append("    .Length of status bar text (character count)  = ").append( field_10_length_status_bar_text )
            .append("\n");
        buffer.append("    .Name (Unicode flag)  = ").append( field_11_compressed_unicode_flag )
            .append("\n");
        buffer.append("    .Name (Unicode text)  = ").append( getNameText() )
            .append("\n");
        buffer.append("    .Formula data (RPN token array without size field)      = ").append( HexDump.toHex( 
                       ((field_13_raw_name_definition != null) ? field_13_raw_name_definition : new byte[0] ) ) )
            .append("\n");
            
        buffer.append("    .Menu text (Unicode string without length field)        = ").append( field_14_custom_menu_text )
            .append("\n");
        buffer.append("    .Description text (Unicode string without length field) = ").append( field_15_description_text )
            .append("\n");
        buffer.append("    .Help topic text (Unicode string without length field)  = ").append( field_16_help_topic_text )
            .append("\n");
        buffer.append("    .Status bar text (Unicode string without length field)  = ").append( field_17_status_bar_text )
            .append("\n");
        if (field_13_raw_name_definition != null)
            buffer.append(org.apache.poi.util.HexDump.dump(this.field_13_raw_name_definition,0,0));
        buffer.append("[/NAME]\n");
        
        return buffer.toString();
    }

	/**Creates a human readable name for built in types
	 * @return Unknown if the built-in name cannot be translated
	 */
	protected String translateBuiltInName(byte name)
	{
	    switch (name)
	    {
	        case NameRecord.BUILTIN_AUTO_ACTIVATE :     return "Auto_Activate";
	        case NameRecord.BUILTIN_AUTO_CLOSE :        return "Auto_Close";
	        case NameRecord.BUILTIN_AUTO_DEACTIVATE :   return "Auto_Deactivate";
	        case NameRecord.BUILTIN_AUTO_OPEN :         return "Auto_Open";
	        case NameRecord.BUILTIN_CONSOLIDATE_AREA :  return "Consolidate_Area";
	        case NameRecord.BUILTIN_CRITERIA :          return "Criteria";
	        case NameRecord.BUILTIN_DATABASE :          return "Database";
	        case NameRecord.BUILTIN_DATA_FORM :         return "Data_Form";            
	        case NameRecord.BUILTIN_PRINT_AREA :        return "Print_Area";
	        case NameRecord.BUILTIN_PRINT_TITLE :       return "Print_Titles";
	        case NameRecord.BUILTIN_RECORDER :          return "Recorder";
	        case NameRecord.BUILTIN_SHEET_TITLE :       return "Sheet_Title";
	        
	    }
	    
	    return "Unknown";
	}
	

}
