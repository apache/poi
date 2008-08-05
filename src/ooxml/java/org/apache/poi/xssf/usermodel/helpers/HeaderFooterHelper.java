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

package org.apache.poi.xssf.usermodel.helpers;


public class HeaderFooterHelper {
    // Note - XmlBeans handles entity encoding for us,
	//  so these should be & forms, not the &amp; ones!
    private static final String HeaderFooterEntity_L = "&L";
    private static final String HeaderFooterEntity_C = "&C";
    private static final String HeaderFooterEntity_R = "&R";
    
    // These are other entities that may be used in the
    //  left, center or right. Not exhaustive
    public static final String HeaderFooterEntity_File = "&F";
    public static final String HeaderFooterEntity_Date = "&D";
    public static final String HeaderFooterEntity_Time = "&T";

    public String getLeftSection(String string) {
    	return getParts(string)[0];
    }
    public String getCenterSection(String string) {
    	return getParts(string)[1];
    }
    public String getRightSection(String string) {
    	return getParts(string)[2];
    }
    
    public String setLeftSection(String string, String newLeft) {
    	String[] parts = getParts(string);
    	parts[0] = newLeft;
        return joinParts(parts);
    }
    public String setCenterSection(String string, String newCenter) {
    	String[] parts = getParts(string);
    	parts[1] = newCenter;
        return joinParts(parts);
    }
    public String setRightSection(String string, String newRight) {
    	String[] parts = getParts(string);
    	parts[2] = newRight;
        return joinParts(parts);
    }
    
    /**
     * Split into left, center, right
     */
    private String[] getParts(String string) {
    	String[] parts = new String[] { "", "", "" };
    	if(string == null)
    		return parts;
    	
    	// They can come in any order, which is just nasty
    	// Work backwards from the end, picking the last
    	//  on off each time as we go
    	int lAt = 0;
    	int cAt = 0;
    	int rAt = 0;
    	
    	while(
    		// Ensure all indicies get updated, then -1 tested
    		(lAt = string.indexOf(HeaderFooterEntity_L)) > -2 &&
    		(cAt = string.indexOf(HeaderFooterEntity_C)) > -2 &&  
    		(rAt = string.indexOf(HeaderFooterEntity_R)) > -2 &&
    		(lAt > -1 || cAt > -1 || rAt > -1)
    	) {
    		// Pick off the last one
    		if(rAt > cAt && rAt > lAt) {
        		parts[2] = string.substring(rAt + HeaderFooterEntity_R.length());
        		string = string.substring(0, rAt);
    		} else if(cAt > rAt && cAt > lAt) {
        		parts[1] = string.substring(cAt + HeaderFooterEntity_C.length());
        		string = string.substring(0, cAt);
    		} else {
        		parts[0] = string.substring(lAt + HeaderFooterEntity_L.length());
        		string = string.substring(0, lAt);
    		}
    	}
    	
    	return parts;
    }
    private String joinParts(String[] parts) {
    	return joinParts(parts[0], parts[1], parts[2]);
    }
    private String joinParts(String l, String c, String r) {
    	StringBuffer ret = new StringBuffer();

    	// Join as c, l, r
    	if(c.length() > 0) {
    		ret.append(HeaderFooterEntity_C);
    		ret.append(c);
    	}
    	if(l.length() > 0) {
    		ret.append(HeaderFooterEntity_L);
    		ret.append(l);
    	}
    	if(r.length() > 0) {
    		ret.append(HeaderFooterEntity_R);
    		ret.append(r);
    	}
    	
    	return ret.toString();
    }
}
