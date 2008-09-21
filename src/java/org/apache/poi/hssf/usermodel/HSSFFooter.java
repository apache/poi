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

import org.apache.poi.hssf.record.FooterRecord;

/**
 * Class to read and manipulate the footer.
 * <P>
 * The footer works by having a left, center, and right side.  The total cannot
 * be more that 255 bytes long.  One uses this class by getting the HSSFFooter
 * from HSSFSheet and then getting or setting the left, center, and right side.
 * For special things (such as page numbers and date), one can use a the methods
 * that return the characters used to represent these.  One can also change the
 * fonts by using similar methods.
 * <P>
 * @author Shawn Laubach (slaubach at apache dot org)
 */
public class HSSFFooter extends HeaderFooter {
    private FooterRecord footerRecord;

    /**
     * Constructor.  Creates a new footer interface from a footer record
     * @param footerRecord Footer record to create the footer with
     */
    protected HSSFFooter(FooterRecord footerRecord) {
    	super(footerRecord.getFooter());
    	this.footerRecord = footerRecord;
    }

    /**
     * Sets the left string.
     * @param newLeft The string to set as the left side.
     */
    public void setLeft(String newLeft) {
	left = newLeft;
	createFooterString();
    }

    /**
     * Sets the center string.
     * @param newCenter The string to set as the center.
     */
    public void setCenter(String newCenter) {
	center = newCenter;
	createFooterString();
    }

    /**
     * Sets the right string.
     * @param newRight The string to set as the right side.
     */
    public void setRight(String newRight) {
	right = newRight;
	createFooterString();
    }
    
    protected String getRawFooter() {
    	return footerRecord.getFooter();
    }


    /**
     * Creates the complete footer string based on the left, center, and middle
     * strings.
     */
    private void createFooterString() {
	footerRecord.setFooter(
			       "&C" + (center == null ? "" : center) +
			       "&L" + (left == null ? "" : left) +
			       "&R" + (right == null ? "" : right));
	footerRecord.setFooterLength((byte)footerRecord.getFooter().length());
    }
}

