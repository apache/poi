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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.hslf.usermodel.HSLFFontInfo;
import org.apache.poi.hslf.usermodel.HSLFFontInfoPredefined;
import org.apache.poi.util.POILogger;

/**
 * {@code FontCollection} ia a container that holds information
 * about all the fonts in the presentation.
 */

public final class FontCollection extends RecordContainer {
    private final Map<String,HSLFFontInfo> fonts = new LinkedHashMap<>();
    private byte[] _header;

	protected FontCollection(byte[] source, int start, int len) {
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		_children = Record.findChildRecords(source,start+8,len-8);

		for (Record r : _children){
			if(r instanceof FontEntityAtom) {
			    HSLFFontInfo fi = new HSLFFontInfo((FontEntityAtom)r);
	            fonts.put(fi.getTypeface(), fi);
			} else {
				logger.log(POILogger.WARN, "Warning: FontCollection child wasn't a FontEntityAtom, was " + r.getClass().getSimpleName());
			}
		}
	}

	/**
	 * Return the type, which is 2005
	 */
	@Override
    public long getRecordType() {
        return RecordTypes.FontCollection.typeID;
    }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	@Override
    public void writeOut(OutputStream out) throws IOException {
		writeOut(_header[0],_header[1],getRecordType(),_children,out);
	}

    /**
     * Add font with the given FontInfo configuration to the font collection.
     * The returned FontInfo contains the HSLF specific details and the collection 
     * uniquely contains fonts based on their typeface, i.e. calling the method with FontInfo
     * objects having the same name results in the same HSLFFontInfo reference.
     * 
     * @param fontInfo the FontInfo configuration, can be a instance of {@link HSLFFontInfo},
     *      {@link HSLFFontInfoPredefined} or a custom implementation
     * @return the register HSLFFontInfo object
     */
    public HSLFFontInfo addFont(FontInfo fontInfo) {
        HSLFFontInfo fi = getFontInfo(fontInfo.getTypeface());
        if (fi != null) {
            return fi;
        }

        fi = new HSLFFontInfo(fontInfo);
        fi.setIndex(fonts.size());
        fonts.put(fi.getTypeface(), fi);
        
        FontEntityAtom fnt = fi.createRecord();

        // Append new child to the end
        appendChildRecord(fnt);

        // the added font is the last in the list
        return fi;
    }


    /**
     * Lookup a FontInfo object by its typeface
     * 
     * @param typeface the full font name
     * 
     * @return the HSLFFontInfo for the given name or {@code null} if not found
     */
    public HSLFFontInfo getFontInfo(String typeface) {
        return fonts.get(typeface);
    }

    /**
     * Lookup a FontInfo object by its internal font index
     * 
     * @param index the internal font index
     * 
     * @return the HSLFFontInfo for the given index or {@code null} if not found
     */
    public HSLFFontInfo getFontInfo(int index) {
        for (HSLFFontInfo fi : fonts.values()) {
            if (fi.getIndex() == index) {
                return fi;
            }
        }
        return null;
    }
    
    /**
     * @return the number of registered fonts
     */
    public int getNumberOfFonts() {
        return fonts.size();
    }
}
