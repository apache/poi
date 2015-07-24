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

package org.apache.poi.hslf.record;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.model.textproperties.TextPropCollection.TextPropType;
import org.apache.poi.util.LittleEndian;

/**
 * TxMasterStyleAtom atom (4003).
 * <p>
 * Stores default character and paragraph styles.
 * The atom instance value is the text type and is encoded like the txstyle field in
 * TextHeaderAtom. The text styles are located in the MainMaster container,
 * except for the "other" style, which is in the Document.Environment container.
 * </p>
 * <p>
 *  This atom can store up to 5 pairs of paragraph+character styles,
 *  each pair describes an indent level. The first pair describes
 *  first-level paragraph with no indentation.
 * </p>
 *
 *  @author Yegor Kozlov
 */
public final class TxMasterStyleAtom extends RecordAtom {
    /**
     * Maximum number of indentation levels allowed in PowerPoint documents
     */
    public static final int MAX_INDENT = 5;

    private byte[] _header;
    private static long _type = 4003;
    private byte[] _data;

    private List<TextPropCollection> paragraphStyles;
    private List<TextPropCollection> charStyles;

    protected TxMasterStyleAtom(byte[] source, int start, int len) {
        _header = new byte[8];
        System.arraycopy(source,start,_header,0,8);

        _data = new byte[len-8];
        System.arraycopy(source,start+8,_data,0,_data.length);

        //read available styles
        try {
            init();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * We are of type 4003
     *
     * @return type of this record
     * @see RecordTypes#TxMasterStyleAtom
     */
    public long getRecordType() {
        return _type;
    }

    /**
     * Write the contents of the record back, so it can be written
     *  to disk
     */
    public void writeOut(OutputStream out) throws IOException {
        // Write out the (new) header
        out.write(_header);

        // Write out the record data
        out.write(_data);

    }

    /**
     * Returns array of character styles defined in this record.
     *
     * @return character styles defined in this record
     */
    public List<TextPropCollection> getCharacterStyles(){
        return charStyles;
    }

    /**
     * Returns array of paragraph styles defined in this record.
     *
     * @return paragraph styles defined in this record
     */
    public List<TextPropCollection> getParagraphStyles(){
        return paragraphStyles;
    }

    /**
     * Return type of the text.
     * Must be a constant defined in <code>TextHeaderAtom</code>
     *
     * @return type of the text
     * @see TextHeaderAtom
     */
    public int getTextType(){
        //The atom instance value is the text type
        return LittleEndian.getShort(_header, 0) >> 4;
    }

    /**
     * parse the record data and initialize styles
     */
    protected void init(){
        //type of the text
        int type = getTextType();

        int head;
        int pos = 0;

        //number of indentation levels
        short levels = LittleEndian.getShort(_data, 0);
        pos += LittleEndian.SHORT_SIZE;

        paragraphStyles = new ArrayList<TextPropCollection>(levels);
        charStyles = new ArrayList<TextPropCollection>(levels);

        for(short j = 0; j < levels; j++) {
            TextPropCollection prprops = new TextPropCollection(0, TextPropType.paragraph); //  getParagraphProps(type, j)
            if (type >= TextHeaderAtom.CENTRE_BODY_TYPE) {
                // Fetch the 2 byte value, that is safe to ignore for some types of text
                short indentLevel = LittleEndian.getShort(_data, pos);
                prprops.setIndentLevel(indentLevel);
                pos += LittleEndian.SHORT_SIZE;
            } else {
                prprops.setIndentLevel((short)-1);
            }

            head = LittleEndian.getInt(_data, pos);
            pos += LittleEndian.INT_SIZE;
            
            pos += prprops.buildTextPropList( head, _data, pos);
            paragraphStyles.add(prprops);

            head = LittleEndian.getInt(_data, pos);
            pos += LittleEndian.INT_SIZE;
            TextPropCollection chprops = new TextPropCollection(0, TextPropType.character); //  getCharacterProps(type, j)
            pos += chprops.buildTextPropList( head, _data, pos);
            charStyles.add(chprops);
        }

    }

    /**
     * Paragraph properties for the specified text type and
     *  indent level
     * Depending on the level and type, it may be our special
     *  ones, or the standard StyleTextPropAtom ones
     */
//    protected TextProp[] getParagraphProps(int type, int level){
//        return StyleTextPropAtom.paragraphTextPropTypes;
//        return (level != 0 || type >= MAX_INDENT)
//            ? StyleTextPropAtom.paragraphTextPropTypes
//            : paragraphSpecialPropTypes;
//    }

    /**
     * Character properties for the specified text type and
     *  indent level.
     * Depending on the level and type, it may be our special
     *  ones, or the standard StyleTextPropAtom ones
     */
//    protected TextProp[] getCharacterProps(int type, int level){
//        return StyleTextPropAtom.characterTextPropTypes;
//        return (level != 0 || type >= MAX_INDENT) 
//            ? StyleTextPropAtom.characterTextPropTypes
//            : characterSpecialPropTypes;
//    }
}
