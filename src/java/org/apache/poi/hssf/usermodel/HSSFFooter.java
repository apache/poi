/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

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
public class HSSFFooter extends Object {

  FooterRecord footerRecord;
  String left;
  String center;
  String right;

  /**
   * Constructor.  Creates a new footer interface from a footer record
   * @param footerRecord Footer record to create the footer with
   */
  protected HSSFFooter(FooterRecord footerRecord) {
    this.footerRecord = footerRecord;
    String foot = footerRecord.getFooter();
    while (foot != null && foot.length() > 1) {
	int pos = foot.length();
	switch (foot.substring(1, 2).charAt(0)) {
	case 'L' :
 	    if (foot.indexOf("&C") >= 0) {
		pos = Math.min(pos, foot.indexOf("&C"));
	    } 
	    if (foot.indexOf("&R") >= 0) {
		pos = Math.min(pos, foot.indexOf("&R"));
	    } 
	    left = foot.substring(2, pos);
	    foot = foot.substring(pos);
	    break;
	case 'C' : 
	    if (foot.indexOf("&L") >= 0) {
		pos = Math.min(pos, foot.indexOf("&L"));
	    } 
	    if (foot.indexOf("&R") >= 0) {
		pos = Math.min(pos, foot.indexOf("&R"));
	    } 
	    center = foot.substring(2, pos);
	    foot = foot.substring(pos);
	    break;
	case 'R' : 
 	    if (foot.indexOf("&C") >= 0) {
		pos = Math.min(pos, foot.indexOf("&C"));
	    } 
	    if (foot.indexOf("&L") >= 0) {
		pos = Math.min(pos, foot.indexOf("&L"));
	    } 
	    right = foot.substring(2, pos);
	    foot = foot.substring(pos);
	    break;
	default : foot = null;
	}
    }
  }

  /**
   * Get the left side of the footer.
   * @return The string representing the left side.
   */
  public String getLeft() {
    return left;
  }

  /**
   * Sets the left string.
   * @newLeft The string to set as the left side.
   */
  public void setLeft(String newLeft) {
    left = newLeft;
    createFooterString();
  }

  /**
   * Get the center of the footer.
   * @return The string representing the center.
   */
  public String getCenter() {
    return center;
  }

  /**
   * Sets the center string.
   * @newLeft The string to set as the center.
   */
  public void setCenter(String newCenter) {
    center = newCenter;
    createFooterString();
  }

  /**
   * Get the right side of the footer.
   * @return The string representing the right side.
   */
  public String getRight() {
    return right;
  }

  /**
   * Sets the right string.
   * @newLeft The string to set as the right side.
   */
  public void setRight(String newRight) {
    right = newRight;
    createFooterString();
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

  /**
   * Returns the string that represents the change in font size.
   * @param size the new font size
   * @return The special string to represent a new font size
   */
  public static String fontSize(short size) {
    return "&" + size;
  }

  /**
   * Returns the string that represents the change in font.
   * @param font the new font
   * @param style the fonts style
   * @return The special string to represent a new font size
   */
  public static String font(String font, String style) {
    return "&\"" + font + "," + style + "\"";
  }

  /**
   * Returns the string representing the current page number
   * @return The special string for page number
   */
  public static String page() {
    return "&P";
  }

  /**
   * Returns the string representing the number of pages.
   * @return The special string for the number of pages
   */
  public static String numPages() {
    return "&N";
  }

  /**
   * Returns the string representing the current date
   * @return The special string for the date
   */
  public static String date() {
    return "&D";
  }

  /**
   * Returns the string representing the current time
   * @return The special string for the time
   */
  public static String time() {
    return "&T";
  }

  /**
   * Returns the string representing the current file name
   * @return The special string for the file name
   */
  public static String file() {
    return "&F";
  }

  /**
   * Returns the string representing the current tab (sheet) name
   * @return The special string for tab name
   */
  public static String tab() {
    return "&A";
  }
}

