
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
import java.util.Stack;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import java.util.List;
import org.apache.poi.hssf.util.RangeAddress;

/**
 * Title:        Name Record (aka Named Range) <P>
 * Description:  Defines a named range within a workbook. <P>
 * REFERENCE:  <P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @version 1.0-pre
 */

public class NameRecord extends Record {
    /**
     */
    public final static short sid = 0x18; //Docs says that it is 0x218
    private short             field_1_option_flag;
    private byte              field_2_keyboard_shortcut;
    private byte              field_3_length_name_text;
    private short             field_4_length_name_definition;
    private short             field_5_index_to_sheet;
    private short             field_6_equals_to_index_to_sheet;
    private byte              field_7_length_custom_menu;
    private byte              field_8_length_description_text;
    private byte              field_9_length_help_topic_text;
    private byte              field_10_length_status_bar_text;
    private byte              field_11_compressed_unicode_flag;   // not documented
    private String            field_12_name_text;
    private Stack             field_13_name_definition;
    private byte[]            field_13_raw_name_definition = null; // raw data
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

    /** sets the index number to the extern sheet (thats is what writen in ducomentetion
     *  but as i saw , its work direrent)
     * @param index extern sheet index
     */
    public void setIndexToSheet(short index){
        field_5_index_to_sheet = index;

        // field_6_equals_to_index_to_sheet is equal to field_5_index_to_sheet
        field_6_equals_to_index_to_sheet = index;
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
    public short getIndexToSheet(){
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

    /** gets the name
     * @return name
     */
    public String getNameText(){
        return field_12_name_text;
    }

    /** gets the definition, reference (Formula)
     * @return definition -- can be null if we cant parse ptgs
     */
    protected List getNameDefinition() {
        return ( List ) field_13_name_definition;
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
    public int serialize(int offset, byte[] data) {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)( 15 + getTextsLength()));
        LittleEndian.putShort(data, 4 + offset, getOptionFlag());
        data[6 + offset] = getKeyboardShortcut();
        data[7 + offset] = getNameTextLength();
        LittleEndian.putShort(data, 8 + offset, getDefinitionTextLength());
        LittleEndian.putShort(data, 10 + offset, getIndexToSheet());
        LittleEndian.putShort(data, 12 + offset, getIndexToSheet());
        data [14 + offset] =  getCustomMenuLength();
        data [15 + offset] =  getDescriptionTextLength();
        data [16 + offset] =  getHelpTopicLength();
        data [17 + offset] =  getStatusBarLength();
        data [18 + offset] =  getCompressedUnicodeFlag();

        StringUtil.putCompressedUnicode(getNameText(), data , 19 + offset);

        int start_of_name_definition    = 19  + field_3_length_name_text;
        if (this.field_13_name_definition != null) {
            serializePtgs(data, start_of_name_definition + offset);
        } else {
            System.arraycopy(field_13_raw_name_definition,0,data
            ,start_of_name_definition + offset,field_13_raw_name_definition.length);
        }

        int start_of_custom_menu_text   = start_of_name_definition + field_4_length_name_definition;
        StringUtil.putCompressedUnicode(getCustomMenuText(), data , start_of_custom_menu_text + offset);

        int start_of_description_text   = start_of_custom_menu_text + field_8_length_description_text;
        StringUtil.putCompressedUnicode(getDescriptionText(), data , start_of_description_text + offset);

        int start_of_help_topic_text    = start_of_description_text + field_9_length_help_topic_text;
        StringUtil.putCompressedUnicode(getHelpTopicText(), data , start_of_help_topic_text + offset);

        int start_of_status_bar_text       = start_of_help_topic_text + field_10_length_status_bar_text;
        StringUtil.putCompressedUnicode(getStatusBarText(), data , start_of_status_bar_text + offset);


        return getRecordSize();
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
    public String getAreaReference(){
        if (field_13_name_definition == null) return "#REF!";
        Ptg ptg = (Ptg) field_13_name_definition.peek();
        String result = "";

        if (ptg.getClass() == Area3DPtg.class){
            result = ((Area3DPtg) ptg).getArea();

        } else if (ptg.getClass() == Ref3DPtg.class){
            result = ((Ref3DPtg) ptg).getArea();
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
            this.setDefinitionTextLength((short)((Area3DPtg) ptg).getSize());
        } else {
            ptg = new Ref3DPtg();
            ((Ref3DPtg) ptg).setExternSheetIndex(externSheetIndex);
            ((Ref3DPtg) ptg).setArea(ref);
            this.setDefinitionTextLength((short)((Ref3DPtg) ptg).getSize());
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

        field_11_compressed_unicode_flag= data [14 + offset];
        field_12_name_text = new String(data, 15 + offset,
        LittleEndian.ubyteToInt(field_3_length_name_text));

        int start_of_name_definition    = 15 + field_3_length_name_text;
        field_13_name_definition = getParsedExpressionTokens(data, field_4_length_name_definition,
        offset, start_of_name_definition);

        int start_of_custom_menu_text   = start_of_name_definition + field_4_length_name_definition;
        field_14_custom_menu_text       = new String(data, start_of_custom_menu_text + offset,
        LittleEndian.ubyteToInt(field_7_length_custom_menu));

        int start_of_description_text   = start_of_custom_menu_text + field_8_length_description_text;
        field_15_description_text       = new String(data, start_of_description_text + offset,
        LittleEndian.ubyteToInt(field_8_length_description_text));

        int start_of_help_topic_text    = start_of_description_text + field_9_length_help_topic_text;
        field_16_help_topic_text        = new String(data, start_of_help_topic_text + offset,
        LittleEndian.ubyteToInt(field_9_length_help_topic_text));

        int start_of_status_bar_text       = start_of_help_topic_text + field_10_length_status_bar_text;
        field_17_status_bar_text        = new String(data, start_of_status_bar_text +  offset,
        LittleEndian.ubyteToInt(field_10_length_status_bar_text));

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

}
