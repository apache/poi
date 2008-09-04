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

package org.apache.poi.hssf.record;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.model.FormulaParser;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import org.apache.poi.hssf.record.formula.UnionPtg;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.AreaReference;
import org.apache.poi.hssf.util.RangeAddress;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * Title:        DEFINEDNAME Record (0x0018) <p/>
 * Description:  Defines a named range within a workbook. <P>
 * REFERENCE:  <P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @author  Sergei Kozello (sergeikozello at mail.ru)
 * @author Glen Stampoultzis (glens at apache.org)
 * @version 1.0-pre
 */
public final class NameRecord extends Record {
    public final static short sid = 0x0018;
	/**Included for completeness sake, not implemented */
	public final static byte  BUILTIN_CONSOLIDATE_AREA      = 1;
	/**Included for completeness sake, not implemented */
	public final static byte  BUILTIN_AUTO_OPEN             = 2;
	/**Included for completeness sake, not implemented */
	public final static byte  BUILTIN_AUTO_CLOSE            = 3;
	/**Included for completeness sake, not implemented */
	public final static byte  BUILTIN_DATABASE              = 4;
	/**Included for completeness sake, not implemented */
	public final static byte  BUILTIN_CRITERIA              = 5;

	public final static byte  BUILTIN_PRINT_AREA            = 6;
	public final static byte  BUILTIN_PRINT_TITLE           = 7;

	/**Included for completeness sake, not implemented */
	public final static byte  BUILTIN_RECORDER              = 8;
	/**Included for completeness sake, not implemented */
	public final static byte  BUILTIN_DATA_FORM             = 9;
	/**Included for completeness sake, not implemented */
	public final static byte  BUILTIN_AUTO_ACTIVATE         = 10;
	/**Included for completeness sake, not implemented */
	public final static byte  BUILTIN_AUTO_DEACTIVATE       = 11;
	/**Included for completeness sake, not implemented */
	public final static byte  BUILTIN_SHEET_TITLE           = 12;

	public final static byte  BUILTIN_FILTER_DB             = 13;

	private static final class Option {
		public static final int OPT_HIDDEN_NAME =   0x0001;
		public static final int OPT_FUNCTION_NAME = 0x0002;
		public static final int OPT_COMMAND_NAME =  0x0004;
		public static final int OPT_MACRO =         0x0008;
		public static final int OPT_COMPLEX =       0x0010;
		public static final int OPT_BUILTIN =       0x0020;
		public static final int OPT_BINDATA =       0x1000;
	}

	private short             field_1_option_flag;
	private byte              field_2_keyboard_shortcut;
	private short             field_5_index_to_sheet;     // unused: see field_6
	/** the one based sheet number.  Zero if this is a global name */
	private int               field_6_sheetNumber;
	private boolean           field_11_nameIsMultibyte;
	private byte              field_12_built_in_code;
	private String            field_12_name_text;
	private Ptg[]             field_13_name_definition;
	private String            field_14_custom_menu_text;
	private String            field_15_description_text;
	private String            field_16_help_topic_text;
	private String            field_17_status_bar_text;


	/** Creates new NameRecord */
	public NameRecord() {
		field_13_name_definition = Ptg.EMPTY_PTG_ARRAY;

		field_12_name_text = "";
		field_14_custom_menu_text = "";
		field_15_description_text = "";
		field_16_help_topic_text = "";
		field_17_status_bar_text = "";
	}

	/**
	 * Constructs a Name record and sets its fields appropriately.
	 *
	 * @param in the RecordInputstream to read the record from
	 */
	public NameRecord(RecordInputStream in) {
		super(in);
	}

	/**
	 * Constructor to create a built-in named region
	 * @param builtin Built-in byte representation for the name record, use the public constants
	 */
	public NameRecord(byte builtin, int sheetNumber)
	{
		this();
		field_12_built_in_code = builtin;
		setOptionFlag((short)(field_1_option_flag | Option.OPT_BUILTIN));
		field_6_sheetNumber = sheetNumber; //the extern sheets are set through references
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

	/**
	 * For named ranges, and built-in names
	 * @return the 1-based sheet number.  Zero if this is a global name
	 */
	public int getSheetNumber()
	{
		return field_6_sheetNumber;
	}

	/**
	 * @return function group
	 * @see FnGroupCountRecord
	 */
	public byte getFnGroup() {
		int masked = field_1_option_flag & 0x0fc0;
		return (byte) (masked >> 4);
	}


	public void setSheetNumber(int value)
	{
		field_6_sheetNumber = value;
	}


	/** sets the name of the named range
	 * @param name named range name
	 */
	public void setNameText(String name){
		field_12_name_text = name;
		field_11_nameIsMultibyte = StringUtil.hasMultibyte(name);
	}

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

	/**
	 * gets the name length, in characters
	 * @return name length
	 */
	private int getNameTextLength(){
		if (isBuiltInName()) {
			return 1;
		}
		return field_12_name_text.length();
	}


	/**
	 * @return true if name is hidden
	 */
	public boolean isHiddenName() {
		return (field_1_option_flag & Option.OPT_HIDDEN_NAME) != 0;
	}
	public void setHidden(boolean b) {
		if (b) {
			field_1_option_flag |= Option.OPT_HIDDEN_NAME;
		} else {
			field_1_option_flag &= (~Option.OPT_HIDDEN_NAME);
		}
	}
	/**
	 * @return <code>true</code> if name is a function
	 */
	public boolean isFunctionName() {
		return (field_1_option_flag & Option.OPT_FUNCTION_NAME) != 0;
	}

	/**
	 * @return <code>true</code> if name has a formula (named range or defined value)
	 */
	public boolean hasFormula() {
		return field_1_option_flag == 0 && field_13_name_definition.length > 0;
	}

	/**
	 * @return true if name is a command
	 */
	public boolean isCommandName() {
		return (field_1_option_flag & Option.OPT_COMMAND_NAME) != 0;
	}
	/**
	 * @return true if function macro or command macro
	 */
	public boolean isMacro() {
		return (field_1_option_flag & Option.OPT_MACRO) != 0;
	}
	/**
	 * @return true if array formula or user defined
	 */
	public boolean isComplexFunction() {
		return (field_1_option_flag & Option.OPT_COMPLEX) != 0;
	}

	/**Convenience Function to determine if the name is a built-in name
	 */
	public boolean isBuiltInName()
	{
		return ((field_1_option_flag & Option.OPT_BUILTIN) != 0);
	}


	/** gets the name
	 * @return name
	 */
	public String getNameText(){

		return isBuiltInName() ? translateBuiltInName(getBuiltInName()) : field_12_name_text;
	}

	/** Gets the Built In Name
	 * @return the built in Name
	 */
	public byte getBuiltInName()
	{
		return field_12_built_in_code;
	}


	/** gets the definition, reference (Formula)
	 * @return the name formula. never <code>null</code>
	 */
	public Ptg[] getNameDefinition() {
		return (Ptg[]) field_13_name_definition.clone();
	}

	public void setNameDefinition(Ptg[] ptgs) {
		field_13_name_definition = (Ptg[]) ptgs.clone();
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
	 * @param offset to begin writing at
	 * @param data byte array containing instance data
	 * @return number of bytes written
	 */
	public int serialize( int offset, byte[] data ) {

		int field_7_length_custom_menu = field_14_custom_menu_text.length();
		int field_8_length_description_text = field_15_description_text.length();
		int field_9_length_help_topic_text = field_16_help_topic_text.length();
		int field_10_length_status_bar_text = field_17_status_bar_text.length();
		int rawNameSize = getNameRawSize();
		
		int formulaTotalSize = Ptg.getEncodedSize(field_13_name_definition);
		int dataSize = 15 // 4 shorts + 7 bytes
			+ rawNameSize
			+ field_7_length_custom_menu
			+ field_8_length_description_text
			+ field_9_length_help_topic_text
			+ field_10_length_status_bar_text
			+ formulaTotalSize;
		
		LittleEndian.putShort(data, 0 + offset, sid);
		LittleEndian.putUShort(data, 2 + offset, dataSize);
		// size defined below
		LittleEndian.putShort(data, 4 + offset, getOptionFlag());
		LittleEndian.putByte(data, 6 + offset, getKeyboardShortcut());
		LittleEndian.putByte(data, 7 + offset, getNameTextLength());
		// Note -
		LittleEndian.putUShort(data, 8 + offset, Ptg.getEncodedSizeWithoutArrayData(field_13_name_definition));
		LittleEndian.putUShort(data, 10 + offset, field_5_index_to_sheet);
		LittleEndian.putUShort(data, 12 + offset, field_6_sheetNumber);
		LittleEndian.putByte(data, 14 + offset, field_7_length_custom_menu);
		LittleEndian.putByte(data, 15 + offset, field_8_length_description_text);
		LittleEndian.putByte(data, 16 + offset, field_9_length_help_topic_text);
		LittleEndian.putByte(data, 17 + offset, field_10_length_status_bar_text);
		LittleEndian.putByte(data, 18 + offset, field_11_nameIsMultibyte ? 1 : 0);
		int pos = 19 + offset;

		if (isBuiltInName()) {
			//can send the builtin name directly in
			LittleEndian.putByte(data, pos,  field_12_built_in_code);
		} else {
			String nameText = field_12_name_text;
			if (field_11_nameIsMultibyte) {
    			StringUtil.putUnicodeLE(nameText, data, pos);
     		} else {
    			StringUtil.putCompressedUnicode(nameText, data, pos);
    		}
		}
		pos += rawNameSize;

		Ptg.serializePtgs(field_13_name_definition,  data, pos);
		pos += formulaTotalSize;
		
		StringUtil.putCompressedUnicode( getCustomMenuText(), data, pos);
		pos += field_7_length_custom_menu;
		StringUtil.putCompressedUnicode( getDescriptionText(), data, pos);
		pos += field_8_length_description_text;
		StringUtil.putCompressedUnicode( getHelpTopicText(), data, pos);
		pos += field_9_length_help_topic_text;
		StringUtil.putCompressedUnicode( getStatusBarText(), data, pos);

		return 4 + dataSize;
	}
	private int getNameRawSize() {
		if (isBuiltInName()) {
			return 1;
		} 
		int nChars = field_12_name_text.length();
		if(field_11_nameIsMultibyte) {
			return 2 * nChars;
		} 
		return nChars;
	}

	/** returns the record size
	 */
	public int getRecordSize(){
		return 4 // sid + size
			+ 15 // 4 shorts + 7 bytes
			+ getNameRawSize()
			+ field_14_custom_menu_text.length()
			+ field_15_description_text.length()
			+ field_16_help_topic_text.length()
			+ field_17_status_bar_text.length()
			+ Ptg.getEncodedSize(field_13_name_definition);
	}

	/** gets the extern sheet number
	 * @return extern sheet index
	 */
	public short getExternSheetNumber(){
		if (field_13_name_definition.length < 1) {
			return 0;
		}
		Ptg ptg = field_13_name_definition[0];

		if (ptg.getClass() == Area3DPtg.class){
			return ((Area3DPtg) ptg).getExternSheetIndex();

		}
		if (ptg.getClass() == Ref3DPtg.class){
			return ((Ref3DPtg) ptg).getExternSheetIndex();
		}
		return 0;
	}

	/** sets the extern sheet number
	 * @param externSheetNumber extern sheet number
	 */
	public void setExternSheetNumber(short externSheetNumber){
		Ptg ptg;

		if (field_13_name_definition.length < 1){
			ptg = createNewPtg();
			field_13_name_definition = new Ptg[] {
				ptg,
			};
		} else {
			ptg = field_13_name_definition[0];
		}

		if (ptg.getClass() == Area3DPtg.class){
			((Area3DPtg) ptg).setExternSheetIndex(externSheetNumber);

		} else if (ptg.getClass() == Ref3DPtg.class){
			((Ref3DPtg) ptg).setExternSheetIndex(externSheetNumber);
		}

	}

	private static Ptg createNewPtg(){
		return new Area3DPtg("A1", 0); // TODO - change to not be partially initialised
	}

	/** gets the reference , the area only (range)
	 * @return area reference
	 */
	public String getAreaReference(HSSFWorkbook book){
		return FormulaParser.toFormulaString(book, field_13_name_definition);
	}

	/** sets the reference , the area only (range)
	 * @param ref area reference
	 */
	public void setAreaReference(String ref){
		//Trying to find if what ptg do we need
		RangeAddress ra = new RangeAddress(ref);
		Ptg oldPtg;

		if (field_13_name_definition.length < 1){
			oldPtg = createNewPtg();
		} else {
			//Trying to find extern sheet index
			oldPtg = field_13_name_definition[0];
		}
		List temp = new ArrayList();
		short externSheetIndex = 0;

		if (oldPtg.getClass() == Area3DPtg.class){
			externSheetIndex =  ((Area3DPtg) oldPtg).getExternSheetIndex();

		} else if (oldPtg.getClass() == Ref3DPtg.class){
			externSheetIndex =  ((Ref3DPtg) oldPtg).getExternSheetIndex();
		}

		if (ra.hasRange()) {
			// Is it contiguous or not?
			AreaReference[] refs = AreaReference.generateContiguous(ref);

			// Add the area reference(s)
			for(int i=0; i<refs.length; i++) {
				Ptg ptg = new Area3DPtg(refs[i].formatAsString(), externSheetIndex);
				temp.add(ptg);
			}
			// And then a union if we had more than one area
			if(refs.length > 1) {
				Ptg ptg = UnionPtg.instance;
				temp.add(ptg);
			}
		} else {
			Ptg ptg = new Ref3DPtg();
			((Ref3DPtg) ptg).setExternSheetIndex(externSheetIndex);
			((Ref3DPtg) ptg).setArea(ref);
			temp.add(ptg);
		}
		Ptg[] ptgs = new Ptg[temp.size()];
		temp.toArray(ptgs);
		field_13_name_definition = ptgs;
	}

	/**
	 * called by the constructor, should set class level fields.  Should throw
	 * runtime exception for bad/icomplete data.
	 *
	 * @param in the RecordInputstream to read the record from
	 */
	protected void fillFields(RecordInputStream in) {
		field_1_option_flag                 = in.readShort();
		field_2_keyboard_shortcut           = in.readByte();
		int field_3_length_name_text        = in.readByte();
		int field_4_length_name_definition  = in.readShort();
		field_5_index_to_sheet              = in.readShort();
		field_6_sheetNumber                 = in.readUShort();
		int field_7_length_custom_menu      = in.readUByte();
		int field_8_length_description_text = in.readUByte();
		int field_9_length_help_topic_text  = in.readUByte();
		int field_10_length_status_bar_text = in.readUByte();

		//store the name in byte form if it's a built-in name
		field_11_nameIsMultibyte = (in.readByte() != 0);
		if (isBuiltInName()) {
			field_12_built_in_code = in.readByte();
		} else {
			if (field_11_nameIsMultibyte) {
				field_12_name_text = in.readUnicodeLEString(field_3_length_name_text);
			} else {
				field_12_name_text = in.readCompressedUnicode(field_3_length_name_text);
			}
		}

		field_13_name_definition = Ptg.readTokens(field_4_length_name_definition, in);

		//Who says that this can only ever be compressed unicode???
		field_14_custom_menu_text = in.readCompressedUnicode(field_7_length_custom_menu);
		field_15_description_text = in.readCompressedUnicode(field_8_length_description_text);
		field_16_help_topic_text  = in.readCompressedUnicode(field_9_length_help_topic_text);
		field_17_status_bar_text  = in.readCompressedUnicode(field_10_length_status_bar_text);
	}

	/**
	 * return the non static version of the id for this record.
	 */
	public short getSid() {
		return sid;
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

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[NAME]\n");
		sb.append("    .option flags           = ").append(HexDump.shortToHex(field_1_option_flag)).append("\n");
		sb.append("    .keyboard shortcut      = ").append(HexDump.byteToHex(field_2_keyboard_shortcut)).append("\n");
		sb.append("    .length of the name     = ").append(getNameTextLength()).append("\n");
		sb.append("    .unused                 = ").append( field_5_index_to_sheet ).append("\n");
		sb.append("    .index to sheet (1-based, 0=Global) = ").append( field_6_sheetNumber ).append("\n");
		sb.append("    .Menu text length       = ").append(field_14_custom_menu_text.length()).append("\n");
		sb.append("    .Description text length= ").append(field_15_description_text.length()).append("\n");
		sb.append("    .Help topic text length = ").append(field_16_help_topic_text.length()).append("\n");
		sb.append("    .Status bar text length = ").append(field_17_status_bar_text.length()).append("\n");
		sb.append("    .NameIsMultibyte        = ").append(field_11_nameIsMultibyte).append("\n");
		sb.append("    .Name (Unicode text)    = ").append( getNameText() ).append("\n");
		sb.append("    .Formula (nTokens=").append(field_13_name_definition.length).append("):") .append("\n");
		for (int i = 0; i < field_13_name_definition.length; i++) {
			Ptg ptg = field_13_name_definition[i];
			sb.append("       " + ptg.toString()).append(ptg.getRVAType()).append("\n");
		}

		sb.append("    .Menu text       = ").append(field_14_custom_menu_text).append("\n");
		sb.append("    .Description text= ").append(field_15_description_text).append("\n");
		sb.append("    .Help topic text = ").append(field_16_help_topic_text).append("\n");
		sb.append("    .Status bar text = ").append(field_17_status_bar_text).append("\n");
		sb.append("[/NAME]\n");

		return sb.toString();
	}

	/**Creates a human readable name for built in types
	 * @return Unknown if the built-in name cannot be translated
	 */
	private static String translateBuiltInName(byte name)
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
			case NameRecord.BUILTIN_FILTER_DB  :        return "_FilterDatabase";

		}

		return "Unknown";
	}
}
