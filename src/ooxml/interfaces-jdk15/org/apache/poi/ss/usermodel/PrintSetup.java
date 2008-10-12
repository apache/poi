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

package org.apache.poi.ss.usermodel;

public interface PrintSetup {

    public static final short LETTER_PAPERSIZE = 1;

    public static final short LEGAL_PAPERSIZE = 5;

    public static final short EXECUTIVE_PAPERSIZE = 7;

    public static final short A4_PAPERSIZE = 9;

    public static final short A5_PAPERSIZE = 11;

    public static final short ENVELOPE_10_PAPERSIZE = 20;

    public static final short ENVELOPE_DL_PAPERSIZE = 27;

    public static final short ENVELOPE_CS_PAPERSIZE = 28;

    public static final short ENVELOPE_MONARCH_PAPERSIZE = 37;

    /**    
     * Set the paper size.    
     * @param size the paper size.    
     */
    void setPaperSize(short size);

    /**    
     * Set the scale.    
     * @param scale the scale to use    
     */
    void setScale(short scale);

    /**    
     * Set the page numbering start.    
     * @param start the page numbering start    
     */
    void setPageStart(short start);

    /**    
     * Set the number of pages wide to fit the sheet in    
     * @param width the number of pages    
     */
    void setFitWidth(short width);

    /**    
     * Set the number of pages high to fit the sheet in    
     * @param height the number of pages    
     */
    void setFitHeight(short height);

    /**
     * Set whether to go left to right or top down in ordering    
     * @param ltor left to right    
     */
    void setLeftToRight(boolean ltor);

    /**    
     * Set whether to print in landscape    
     * @param ls landscape    
     */
    void setLandscape(boolean ls);

    /**    
     * Valid settings.  I'm not for sure.    
     * @param valid Valid    
     */
    void setValidSettings(boolean valid);

    /**    
     * Set whether it is black and white    
     * @param mono Black and white    
     */
    void setNoColor(boolean mono);

    /**    
     * Set whether it is in draft mode    
     * @param d draft    
     */
    void setDraft(boolean d);

    /**    
     * Print the include notes    
     * @param printnotes print the notes    
     */
    void setNotes(boolean printnotes);

    /**    
     * Set no orientation. ?    
     * @param orientation Orientation.    
     */
    void setNoOrientation(boolean orientation);

    /**    
     * Set whether to use page start    
     * @param page Use page start    
     */
    void setUsePage(boolean page);

    /**    
     * Sets the horizontal resolution.    
     * @param resolution horizontal resolution    
     */
    void setHResolution(short resolution);

    /**    
     * Sets the vertical resolution.    
     * @param resolution vertical resolution    
     */
    void setVResolution(short resolution);

    /**    
     * Sets the header margin.    
     * @param headermargin header margin    
     */
    void setHeaderMargin(double headermargin);

    /**    
     * Sets the footer margin.    
     * @param footermargin footer margin    
     */
    void setFooterMargin(double footermargin);

    /**    
     * Sets the number of copies.    
     * @param copies number of copies    
     */
    void setCopies(short copies);

    /**    
     * Returns the paper size.    
     * @return paper size    
     */
    short getPaperSize();

    /**    
     * Returns the scale.    
     * @return scale    
     */
    short getScale();

    /**    
     * Returns the page start.    
     * @return page start    
     */
    short getPageStart();

    /**    
     * Returns the number of pages wide to fit sheet in.    
     * @return number of pages wide to fit sheet in    
     */
    short getFitWidth();

    /**    
     * Returns the number of pages high to fit the sheet in.    
     * @return number of pages high to fit the sheet in    
     */
    short getFitHeight();

    /**    
     * Returns the left to right print order.    
     * @return left to right print order    
     */
    boolean getLeftToRight();

    /**    
     * Returns the landscape mode.    
     * @return landscape mode    
     */
    boolean getLandscape();

    /**    
     * Returns the valid settings.    
     * @return valid settings    
     */
    boolean getValidSettings();

    /**    
     * Returns the black and white setting.    
     * @return black and white setting    
     */
    boolean getNoColor();

    /**    
     * Returns the draft mode.    
     * @return draft mode    
     */
    boolean getDraft();

    /**    
     * Returns the print notes.    
     * @return print notes    
     */
    boolean getNotes();

    /**    
     * Returns the no orientation.    
     * @return no orientation    
     */
    boolean getNoOrientation();

    /**    
     * Returns the use page numbers.    
     * @return use page numbers    
     */
    boolean getUsePage();

    /**    
     * Returns the horizontal resolution.    
     * @return horizontal resolution    
     */
    short getHResolution();

    /**    
     * Returns the vertical resolution.    
     * @return vertical resolution    
     */
    short getVResolution();

    /**    
     * Returns the header margin.    
     * @return header margin    
     */
    double getHeaderMargin();

    /**    
     * Returns the footer margin.    
     * @return footer margin    
     */
    double getFooterMargin();

    /**    
     * Returns the number of copies.    
     * @return number of copies    
     */
    short getCopies();

}