
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

