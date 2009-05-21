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

import org.apache.poi.util.POILogger;

import java.io.*;
import java.util.*;

/**
 * <code>FontCollection</code> ia a container that holds information
 * about all the fonts in the presentation.
 *
 * @author Yegor Kozlov
 */

public final class FontCollection extends RecordContainer {
    private List fonts;
	private byte[] _header;

	protected FontCollection(byte[] source, int start, int len) {
		// Grab the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		_children = Record.findChildRecords(source,start+8,len-8);

		// Save font names into <code>List</code>
		fonts = new ArrayList();
		for (int i = 0; i < _children.length; i++){
			if(_children[i] instanceof FontEntityAtom) {
	            FontEntityAtom atom = (FontEntityAtom)_children[i];
	            fonts.add(atom.getFontName());
			} else {
				logger.log(POILogger.WARN, "Warning: FontCollection child wasn't a FontEntityAtom, was " + _children[i]);
			}
		}
	}

	/**
	 * Return the type, which is 2005
	 */
	public long getRecordType() {
        return RecordTypes.FontCollection.typeID;
    }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		writeOut(_header[0],_header[1],getRecordType(),_children,out);
	}

    /**
     * Add font with the specified name to the font collection.
     * If the font is already present return its index.
     * @param name of the font
     * @return zero based index of the font in the collection
     */
    public int addFont(String name) {
        int idx = getFontIndex(name);
        if(idx != -1) return idx;

        return addFont(name, 0, 0, 4, 34);
    }

    public int addFont(String name, int charset, int flags, int type, int pitch) {
        FontEntityAtom fnt = new FontEntityAtom();
        fnt.setFontIndex(fonts.size() << 4);
        fnt.setFontName(name);
        fnt.setCharSet(charset);
        fnt.setFontFlags(flags);
        fnt.setFontType(type);
        fnt.setPitchAndFamily(pitch);
        fonts.add(name);

        // Append new child to the end
		appendChildRecord(fnt);

        return fonts.size()-1; //the added font is the last in the list
    }

    /**
     * @return zero based index of the font in the collection or -1 if not found
     */
    public int getFontIndex(String name) {
        for (int i = 0; i < fonts.size(); i++) {
            if(fonts.get(i).equals(name)){
                //if the font is already present return its index
                return i;
            }
        }
        return -1;
    }

    public int getNumberOfFonts() {
        return fonts.size();
    }

    /**
	 * Get the name of the font at the given ID, or null if there is
	 *  no font at that ID.
	 * @param id
	 */
	public String getFontWithId(int id) {
		if(id >= fonts.size()) {
			// No font with that id
			return null;
		}
		return (String)fonts.get(id);
	}
}
