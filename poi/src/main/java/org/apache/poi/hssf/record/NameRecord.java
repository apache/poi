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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hssf.record.cont.ContinuableRecord;
import org.apache.poi.hssf.record.cont.ContinuableRecordOutput;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.StringUtil;

/**
 * Defines a named range within a workbook.
 */
@SuppressWarnings("unused")
public final class NameRecord extends ContinuableRecord {
    public static final short sid = 0x0018;
	/**Included for completeness sake, not implemented */
	public static final byte  BUILTIN_CONSOLIDATE_AREA      = 1;
	/**Included for completeness sake, not implemented */
	public static final byte  BUILTIN_AUTO_OPEN             = 2;
	/**Included for completeness sake, not implemented */
	public static final byte  BUILTIN_AUTO_CLOSE            = 3;
	/**Included for completeness sake, not implemented */
	public static final byte  BUILTIN_DATABASE              = 4;
	/**Included for completeness sake, not implemented */
	public static final byte  BUILTIN_CRITERIA              = 5;

	public static final byte  BUILTIN_PRINT_AREA            = 6;
	public static final byte  BUILTIN_PRINT_TITLE           = 7;

	/**Included for completeness sake, not implemented */
	public static final byte  BUILTIN_RECORDER              = 8;
	/**Included for completeness sake, not implemented */
	public static final byte  BUILTIN_DATA_FORM             = 9;
	/**Included for completeness sake, not implemented */
	public static final byte  BUILTIN_AUTO_ACTIVATE         = 10;
	/**Included for completeness sake, not implemented */
	public static final byte  BUILTIN_AUTO_DEACTIVATE       = 11;
	/**Included for completeness sake, not implemented */
	public static final byte  BUILTIN_SHEET_TITLE           = 12;

	public static final byte  BUILTIN_FILTER_DB             = 13;

	private static final class Option {
		public static final int OPT_HIDDEN_NAME =   0x0001;
		public static final int OPT_FUNCTION_NAME = 0x0002;
		public static final int OPT_COMMAND_NAME =  0x0004;
		public static final int OPT_MACRO =         0x0008;
		public static final int OPT_COMPLEX =       0x0010;
		public static final int OPT_BUILTIN =       0x0020;
		public static final int OPT_BINDATA =       0x1000;
		public static boolean isFormula(int optValue) {
			return (optValue & 0x0F) == 0;
		}
	}

	private short             field_1_option_flag;
	private byte              field_2_keyboard_shortcut;
	/** One-based extern index of sheet (resolved via LinkTable). Zero if this is a global name  */
	private short             field_5_externSheetIndex_plus1;
	/** the one based sheet number.  */
	private int               field_6_sheetNumber;
	private boolean           field_11_nameIsMultibyte;
	private byte              field_12_built_in_code;
	private String            field_12_name_text;
	private Formula           field_13_name_definition;
	private String            field_14_custom_menu_text;
	private String            field_15_description_text;
	private String            field_16_help_topic_text;
	private String            field_17_status_bar_text;


	/** Creates new NameRecord */
	public NameRecord() {
		field_13_name_definition = Formula.create(Ptg.EMPTY_PTG_ARRAY);

		field_12_name_text = "";
		field_14_custom_menu_text = "";
		field_15_description_text = "";
		field_16_help_topic_text = "";
		field_17_status_bar_text = "";
	}

	public NameRecord(NameRecord other) {
		super(other);
		field_1_option_flag = other.field_1_option_flag;
		field_2_keyboard_shortcut = other.field_2_keyboard_shortcut;
		field_5_externSheetIndex_plus1 = other.field_5_externSheetIndex_plus1;
		field_6_sheetNumber = other.field_6_sheetNumber;
		field_11_nameIsMultibyte = other.field_11_nameIsMultibyte;
		field_12_built_in_code = other.field_12_built_in_code;
		field_12_name_text = other.field_12_name_text;
		field_13_name_definition = other.field_13_name_definition;
		field_14_custom_menu_text = other.field_14_custom_menu_text;
		field_15_description_text = other.field_15_description_text;
		field_16_help_topic_text = other.field_16_help_topic_text;
		field_17_status_bar_text = other.field_17_status_bar_text;
	}

	/**
	 * Constructor to create a built-in named region
	 * @param builtin Built-in byte representation for the name record, use the public constants
	 * @param sheetNumber the sheet which the name applies to
	 */
	public NameRecord(byte builtin, int sheetNumber)
	{
		this();
		field_12_built_in_code = builtin;
		setOptionFlag((short)(field_1_option_flag | Option.OPT_BUILTIN));
		// the extern sheets are set through references
		field_6_sheetNumber = sheetNumber;
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
	 * @return the 1-based sheet number.
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
	 * Indicates that the defined name refers to a user-defined function.
	 * This attribute is used when there is an add-in or other code project associated with the file.
	 *
	 * @param function <code>true</code> indicates the name refers to a function.
	 */
	public void setFunction(boolean function){
		if (function) {
			field_1_option_flag |= Option.OPT_FUNCTION_NAME;
		} else {
			field_1_option_flag &= (~Option.OPT_FUNCTION_NAME);
		}
	}

	/**
	 * @return <code>true</code> if name has a formula (named range or defined value)
	 */
	public boolean hasFormula() {
		return Option.isFormula(field_1_option_flag) && field_13_name_definition.getEncodedTokenSize() > 0;
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

	/**
	 * Convenience Function to determine if the name is a built-in name
	 *
	 * @return true, if the name is a built-in name
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
		return field_13_name_definition.getTokens();
	}

	public void setNameDefinition(Ptg[] ptgs) {
		field_13_name_definition = Formula.create(ptgs);
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
     * NameRecord can span into
     *
     * @param out a data output stream
     */
	@Override
    public void serialize(ContinuableRecordOutput out) {

		int field_7_length_custom_menu = field_14_custom_menu_text.length();
		int field_8_length_description_text = field_15_description_text.length();
		int field_9_length_help_topic_text = field_16_help_topic_text.length();
		int field_10_length_status_bar_text = field_17_status_bar_text.length();

		// size defined below
		out.writeShort(getOptionFlag());
		out.writeByte(getKeyboardShortcut());
		out.writeByte(getNameTextLength());
		// Note - formula size is not immediately before encoded formula, and does not include any array constant data
		out.writeShort(field_13_name_definition.getEncodedTokenSize());
		out.writeShort(field_5_externSheetIndex_plus1);
		out.writeShort(field_6_sheetNumber);
		out.writeByte(field_7_length_custom_menu);
		out.writeByte(field_8_length_description_text);
		out.writeByte(field_9_length_help_topic_text);
		out.writeByte(field_10_length_status_bar_text);
		out.writeByte(field_11_nameIsMultibyte ? 1 : 0);

		if (isBuiltInName()) {
			//can send the builtin name directly in
			out.writeByte(field_12_built_in_code);
		} else {
			String nameText = field_12_name_text;
			if (field_11_nameIsMultibyte) {
				StringUtil.putUnicodeLE(nameText, out);
			} else {
				StringUtil.putCompressedUnicode(nameText, out);
			}
		}
		field_13_name_definition.serializeTokens(out);
		field_13_name_definition.serializeArrayConstantData(out);

		StringUtil.putCompressedUnicode( getCustomMenuText(), out);
		StringUtil.putCompressedUnicode( getDescriptionText(), out);
		StringUtil.putCompressedUnicode( getHelpTopicText(), out);
		StringUtil.putCompressedUnicode( getStatusBarText(), out);
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

	int getDataSize() {
		return 13 // 3 shorts + 7 bytes
			+ getNameRawSize()
			+ field_14_custom_menu_text.length()
			+ field_15_description_text.length()
			+ field_16_help_topic_text.length()
			+ field_17_status_bar_text.length()
			+ field_13_name_definition.getEncodedSize();
	}

	/** gets the extern sheet number
	 * @return extern sheet index
	 */
	public int getExternSheetNumber(){
	    Ptg[] tokens = field_13_name_definition.getTokens();
		if (tokens.length == 0) {
			return 0;
		}

		Ptg ptg = tokens[0];
		if (ptg.getClass() == Area3DPtg.class){
			return ((Area3DPtg) ptg).getExternSheetIndex();

		}
		if (ptg.getClass() == Ref3DPtg.class){
			return ((Ref3DPtg) ptg).getExternSheetIndex();
		}
		return 0;
	}

	/**
	 * called by the constructor, should set class level fields.  Should throw
	 * runtime exception for bad/icomplete data.
	 *
	 * @param ris the RecordInputstream to read the record from
	 */
	public NameRecord(RecordInputStream ris) {
        // YK: Formula data can span into continue records, for example,
        // when containing a large array of strings. See Bugzilla 50244

        // read all remaining bytes and wrap into a LittleEndianInput
        byte[] remainder = ris.readAllContinuedRemainder();
        LittleEndianInput in = new LittleEndianByteArrayInputStream(remainder);

		field_1_option_flag                 = in.readShort();
		field_2_keyboard_shortcut           = in.readByte();
		int field_3_length_name_text        = in.readUByte();
		int field_4_length_name_definition  = in.readShort();
		field_5_externSheetIndex_plus1      = in.readShort();
		field_6_sheetNumber                 = in.readUShort();
		int f7_customMenuLen      = in.readUByte();
		int f8_descriptionTextLen = in.readUByte();
		int f9_helpTopicTextLen  = in.readUByte();
		int f10_statusBarTextLen = in.readUByte();

		//store the name in byte form if it's a built-in name
		field_11_nameIsMultibyte = (in.readByte() != 0);
		if (isBuiltInName()) {
			field_12_built_in_code = in.readByte();
		} else {
			if (field_11_nameIsMultibyte) {
				field_12_name_text = StringUtil.readUnicodeLE(in, field_3_length_name_text);
			} else {
				field_12_name_text = StringUtil.readCompressedUnicode(in, field_3_length_name_text);
			}
		}

		int nBytesAvailable = in.available() - (f7_customMenuLen
				+ f8_descriptionTextLen + f9_helpTopicTextLen + f10_statusBarTextLen);
		field_13_name_definition = Formula.read(field_4_length_name_definition, in, nBytesAvailable);

		//Who says that this can only ever be compressed unicode???
		field_14_custom_menu_text = StringUtil.readCompressedUnicode(in, f7_customMenuLen);
		field_15_description_text = StringUtil.readCompressedUnicode(in, f8_descriptionTextLen);
		field_16_help_topic_text  = StringUtil.readCompressedUnicode(in, f9_helpTopicTextLen);
		field_17_status_bar_text  = StringUtil.readCompressedUnicode(in, f10_statusBarTextLen);
	}

	/**
	 * return the non static version of the id for this record.
	 */
	@Override
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

	@Override
	public NameRecord copy() {
		return new NameRecord(this);
	}

	@Override
	public HSSFRecordTypes getGenericRecordType() {
		return HSSFRecordTypes.NAME;
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		final Map<String,Supplier<?>> m = new LinkedHashMap<>();
		m.put("dataSize", this::getDataSize);
		m.put("optionFlag", this::getOptionFlag);
		m.put("keyboardShortcut", this::getKeyboardShortcut);
		m.put("externSheetIndex", () -> field_5_externSheetIndex_plus1);
		m.put("sheetNumber", this::getSheetNumber);
		m.put("nameIsMultibyte", () -> field_11_nameIsMultibyte);
		m.put("builtInName", this::getBuiltInName);
		m.put("nameLength", this::getNameTextLength);
		m.put("nameText", this::getNameText);
		m.put("formula", this::getNameDefinition);
		m.put("customMenuText", this::getCustomMenuText);
		m.put("descriptionText", this::getDescriptionText);
		m.put("helpTopicText", this::getHelpTopicText);
		m.put("statusBarText", this::getStatusBarText);
		return Collections.unmodifiableMap(m);
	}
}
