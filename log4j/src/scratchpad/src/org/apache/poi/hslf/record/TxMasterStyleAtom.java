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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.model.textproperties.TextPropCollection.TextPropType;
import org.apache.poi.sl.usermodel.TextShape.TextPlaceholder;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianOutputStream;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

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
 */
public final class TxMasterStyleAtom extends RecordAtom {
    private static final POILogger LOG = POILogFactory.getLogger(TxMasterStyleAtom.class);
    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    /**
     * Maximum number of indentation levels allowed in PowerPoint documents
     */
    public static final int MAX_INDENT = 5;

    private static final long _type = RecordTypes.TxMasterStyleAtom.typeID;

    private byte[] _header;
    private byte[] _data;

    private List<TextPropCollection> paragraphStyles;
    private List<TextPropCollection> charStyles;

    protected TxMasterStyleAtom(byte[] source, int start, int len) {
        _header = Arrays.copyOfRange(source, start, start+8);

        _data = IOUtils.safelyClone(source, start+8, len-8, MAX_RECORD_LENGTH);

        //read available styles
        try {
            init();
        } catch (Exception e){
            LOG.log(POILogger.WARN, "Exception when reading available styles", e);
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
        pos += LittleEndianConsts.SHORT_SIZE;

        paragraphStyles = new ArrayList<>(levels);
        charStyles = new ArrayList<>(levels);

        for(short i = 0; i < levels; i++) {
            TextPropCollection prprops = new TextPropCollection(0, TextPropType.paragraph);
            if (type >= TextPlaceholder.CENTER_BODY.nativeId) {
                // Fetch the 2 byte value, that is safe to ignore for some types of text
                short indentLevel = LittleEndian.getShort(_data, pos);
                prprops.setIndentLevel(indentLevel);
                pos += LittleEndianConsts.SHORT_SIZE;
            } else {
                prprops.setIndentLevel((short)-1);
            }

            head = LittleEndian.getInt(_data, pos);
            pos += LittleEndianConsts.INT_SIZE;

            pos += prprops.buildTextPropList( head, _data, pos);
            paragraphStyles.add(prprops);

            head = LittleEndian.getInt(_data, pos);
            pos += LittleEndianConsts.INT_SIZE;
            TextPropCollection chprops = new TextPropCollection(0, TextPropType.character);
            pos += chprops.buildTextPropList( head, _data, pos);
            charStyles.add(chprops);
        }
    }

    /**
     * Updates the rawdata from the modified paragraph/character styles
     *
     * @since POI 3.14-beta1
     */
    public void updateStyles() {
        int type = getTextType();

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            LittleEndianOutputStream leos = new LittleEndianOutputStream(bos);
            int levels = paragraphStyles.size();
            leos.writeShort(levels);

            for (int i=0; i<levels; i++) {
                TextPropCollection prdummy = paragraphStyles.get(i).copy();
                TextPropCollection chdummy = charStyles.get(i).copy();

                if (type >= TextPlaceholder.CENTER_BODY.nativeId) {
                    leos.writeShort(prdummy.getIndentLevel());
                }

                // Indent level is not written for master styles
                prdummy.setIndentLevel((short)-1);
                prdummy.writeOut(bos, true);
                chdummy.writeOut(bos, true);
            }

            _data = bos.toByteArray();
            leos.close();

            LittleEndian.putInt(_header, 4, _data.length);
        } catch (IOException e) {
            throw new HSLFException("error in updating master style properties", e);
        }
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "paragraphStyles", this::getParagraphStyles,
            "charStyles", this::getCharacterStyles
        );
    }
}
