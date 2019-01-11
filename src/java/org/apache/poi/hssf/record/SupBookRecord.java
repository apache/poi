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

import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.StringUtil;

/**
 * Title:        Sup Book - EXTERNALBOOK (0x01AE)<p>
 * Description:  A External Workbook Description (Supplemental Book)
 *               Its only a dummy record for making new ExternSheet Record<p>
 * REFERENCE:  5.38
 */
public final class SupBookRecord extends StandardRecord {

    private final static POILogger logger = POILogFactory.getLogger(SupBookRecord.class);
	
    public final static short sid = 0x01AE;

    private static final short SMALL_RECORD_SIZE = 4;
    private static final short TAG_INTERNAL_REFERENCES = 0x0401;
    private static final short TAG_ADD_IN_FUNCTIONS = 0x3A01;

    private short field_1_number_of_sheets;
    private String field_2_encoded_url;
    private String[] field_3_sheet_names;
    private boolean _isAddInFunctions;

    protected static final char CH_VOLUME = 1;
    protected static final char CH_SAME_VOLUME = 2;
    protected static final char CH_DOWN_DIR = 3;
    protected static final char CH_UP_DIR = 4;
    protected static final char CH_LONG_VOLUME = 5;
    protected static final char CH_STARTUP_DIR = 6;
    protected static final char CH_ALT_STARTUP_DIR = 7;
    protected static final char CH_LIB_DIR = 8;
    protected static final String PATH_SEPERATOR = System.getProperty("file.separator");

    public static SupBookRecord createInternalReferences(short numberOfSheets) {
        return new SupBookRecord(false, numberOfSheets);
    }
    public static SupBookRecord createAddInFunctions() {
        return new SupBookRecord(true, (short)1 /* this field MUST be 0x0001 for add-in referencing */);
    }
    public static SupBookRecord createExternalReferences(String url, String[] sheetNames) {
        return new SupBookRecord(url, sheetNames);
    }
    private SupBookRecord(boolean isAddInFuncs, short numberOfSheets) {
        // else not 'External References'
        field_1_number_of_sheets = numberOfSheets;
        field_2_encoded_url = null;
        field_3_sheet_names = null;
        _isAddInFunctions = isAddInFuncs;
    }
    public SupBookRecord(String url, String[] sheetNames) {
        field_1_number_of_sheets = (short) sheetNames.length;
        field_2_encoded_url = url;
        field_3_sheet_names = sheetNames;
        _isAddInFunctions = false;
    }

    public boolean isExternalReferences() {
        return field_3_sheet_names != null;
    }
    public boolean isInternalReferences() {
        return field_3_sheet_names == null && !_isAddInFunctions;
    }
    public boolean isAddInFunctions() {
        return field_3_sheet_names == null && _isAddInFunctions;
    }
    /**
     * called by the constructor, should set class level fields.  Should throw
     * runtime exception for bad/incomplete data.
     *
     * @param in the stream to read from
     */
    public SupBookRecord(RecordInputStream in) {
        int recLen = in.remaining();

        field_1_number_of_sheets = in.readShort();

        if(recLen > SMALL_RECORD_SIZE) {
            // 5.38.1 External References
            _isAddInFunctions = false;

            field_2_encoded_url = in.readString();
            String[] sheetNames = new String[field_1_number_of_sheets];
            for (int i = 0; i < sheetNames.length; i++) {
                sheetNames[i] = in.readString();
            }
            field_3_sheet_names = sheetNames;
            return;
        }
        // else not 'External References'
        field_2_encoded_url = null;
        field_3_sheet_names = null;

        short nextShort = in.readShort();
        if(nextShort == TAG_INTERNAL_REFERENCES) {
            // 5.38.2 'Internal References'
            _isAddInFunctions = false;
        } else if(nextShort == TAG_ADD_IN_FUNCTIONS) {
            // 5.38.3 'Add-In Functions'
            _isAddInFunctions = true;
            if(field_1_number_of_sheets != 1) {
                throw new RuntimeException("Expected 0x0001 for number of sheets field in 'Add-In Functions' but got ("
                     + field_1_number_of_sheets + ")");
            }
        } else {
            throw new RuntimeException("invalid EXTERNALBOOK code ("
                     + Integer.toHexString(nextShort) + ")");
        }
     }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[SUPBOOK ");

        if(isExternalReferences()) {
            sb.append("External References]\n");
            sb.append(" .url     = ").append(getURL()).append("\n");
            sb.append(" .nSheets = ").append(field_1_number_of_sheets).append("\n");
            for (String sheetname : field_3_sheet_names) {
                sb.append("    .name = ").append(sheetname).append("\n");
            }
            sb.append("[/SUPBOOK");
        } else if(_isAddInFunctions) {
            sb.append("Add-In Functions");
        } else {
            sb.append("Internal References");
            sb.append(" nSheets=").append(field_1_number_of_sheets);
        }
        sb.append("]");
        return sb.toString();
    }
    protected int getDataSize() {
        if(!isExternalReferences()) {
            return SMALL_RECORD_SIZE;
        }
        int sum = 2; // u16 number of sheets

        sum += StringUtil.getEncodedSize(field_2_encoded_url);

        for (String field_3_sheet_name : field_3_sheet_names) {
            sum += StringUtil.getEncodedSize(field_3_sheet_name);
        }
        return sum;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_number_of_sheets);

        if(isExternalReferences()) {
            StringUtil.writeUnicodeString(out, field_2_encoded_url);

            for (String field_3_sheet_name : field_3_sheet_names) {
                StringUtil.writeUnicodeString(out, field_3_sheet_name);
            }
        } else {
            int field2val = _isAddInFunctions ? TAG_ADD_IN_FUNCTIONS : TAG_INTERNAL_REFERENCES;

            out.writeShort(field2val);
        }
    }

    public void setNumberOfSheets(short number){
        field_1_number_of_sheets = number;
    }

    public short getNumberOfSheets(){
        return field_1_number_of_sheets;
    }

    public short getSid()
    {
        return sid;
    }
    public String getURL() {
        String encodedUrl = field_2_encoded_url;
        switch(encodedUrl.charAt(0)) {
            case 0: // Reference to an empty workbook name
                return encodedUrl.substring(1); // will this just be empty string?
            case 1: // encoded file name
                return decodeFileName(encodedUrl);
            case 2: // Self-referential external reference
                return encodedUrl.substring(1);

        }
        return encodedUrl;
    }
    private static String decodeFileName(String encodedUrl) {
        /* see "MICROSOFT OFFICE EXCEL 97-2007  BINARY FILE FORMAT SPECIFICATION" */
    	StringBuilder sb = new StringBuilder();
        for(int i=1; i<encodedUrl.length(); i++) {
        	char c = encodedUrl.charAt(i);
        	switch (c) {
        	case CH_VOLUME:
        		char driveLetter = encodedUrl.charAt(++i);
        		if (driveLetter == '@') {
        			sb.append("\\\\");
        		} else {
        			//Windows notation for drive letters
        			sb.append(driveLetter).append(":");
        		}
        		break;
        	case CH_SAME_VOLUME:
        		sb.append(PATH_SEPERATOR);
        		break;
        	case CH_DOWN_DIR:
        		sb.append(PATH_SEPERATOR);
        		break;
        	case CH_UP_DIR:
        		sb.append("..").append(PATH_SEPERATOR);
        		break;
        	case CH_LONG_VOLUME:
        		//Don't known to handle...
        		logger.log(POILogger.WARN, "Found unexpected key: ChLongVolume - IGNORING");
        		break;
        	case CH_STARTUP_DIR:
        	case CH_ALT_STARTUP_DIR:
        	case CH_LIB_DIR:
        		logger.log(POILogger.WARN, "EXCEL.EXE path unkown - using this directoy instead: .");
        		sb.append(".").append(PATH_SEPERATOR);
        		break;
        	default:
        		sb.append(c);
        	}
        }
        return sb.toString();
    }
    public String[] getSheetNames() {
        return field_3_sheet_names.clone();
    }
    
    public void setURL(String pUrl) {
    	//Keep the first marker character!
    	field_2_encoded_url = field_2_encoded_url.substring(0, 1) + pUrl; 
    }
}
