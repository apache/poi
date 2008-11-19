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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.record.HeaderRecord;
import org.apache.poi.ss.usermodel.Header;

/**
 * Class to read and manipulate the header.
 * <P>
 * The header works by having a left, center, and right side.  The total cannot
 * be more that 255 bytes long.  One uses this class by getting the HSSFHeader
 * from HSSFSheet and then getting or setting the left, center, and right side.
 * For special things (such as page numbers and date), one can use a the methods
 * that return the characters used to represent these.  One can also change the
 * fonts by using similar methods.
 * <P>
 *
 * @author Shawn Laubach (slaubach at apache dot org)
 */
public class HSSFHeader extends HeaderFooter implements Header {
	private HeaderRecord headerRecord;

    /**
     * Constructor.  Creates a new header interface from a header record
     *
     * @param headerRecord Header record to create the header with
     */
    protected HSSFHeader( HeaderRecord headerRecord ) {
    	super(headerRecord.getHeader());
        this.headerRecord = headerRecord;
    }

    /**
     * Sets the left string.
     *
     * @param newLeft The string to set as the left side.
     */
    public void setLeft( String newLeft )
    {
        left = newLeft;
        createHeaderString();
    }

    /**
     * Sets the center string.
     *
     * @param newCenter The string to set as the center.
     */
    public void setCenter( String newCenter )
    {
        center = newCenter;
        createHeaderString();
    }

    /**
     * Sets the right string.
     *
     * @param newRight The string to set as the right side.
     */
    public void setRight( String newRight )
    {
        right = newRight;
        createHeaderString();
    }
    
    protected String getRawHeader() {
    	return headerRecord.getHeader();
    }

    /**
     * Creates the complete header string based on the left, center, and middle
     * strings.
     */
    private void createHeaderString()
    {
        headerRecord.setHeader( "&C" + ( center == null ? "" : center ) +
                "&L" + ( left == null ? "" : left ) +
                "&R" + ( right == null ? "" : right ) );
    }

}

