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
    /** Whatever the printer's default paper size is */
    public static final short PRINTER_DEFAULT_PAPERSIZE = 0;
    /** US Letter 8 1/2 x 11 in */
    public static final short LETTER_PAPERSIZE = 1;
    /** US Letter Small 8 1/2 x 11 in */
    public static final short LETTER_SMALL_PAGESIZE = 2;
    /** US Tabloid 11 x 17 in */
    public static final short TABLOID_PAPERSIZE = 3;
    /** US Ledger 17 x 11 in */
    public static final short LEDGER_PAPERSIZE = 4;
    /** US Legal 8 1/2 x 14 in */
    public static final short LEGAL_PAPERSIZE = 5;
    /** US Statement 5 1/2 x 8 1/2 in */
    public static final short STATEMENT_PAPERSIZE = 6;
    /** US Executive 7 1/4 x 10 1/2 in */
    public static final short EXECUTIVE_PAPERSIZE = 7;
    /** A3 - 297x420 mm */
    public static final short A3_PAPERSIZE = 8;
    /** A4 - 210x297 mm */
    public static final short A4_PAPERSIZE = 9;
    /** A4 Small - 210x297 mm */
    public static final short A4_SMALL_PAPERSIZE = 10;
    /** A5 - 148x210 mm */
    public static final short A5_PAPERSIZE = 11;
    /** B4 (JIS) 250x354 mm */
    public static final short B4_PAPERSIZE = 12;
    /** B5 (JIS) 182x257 mm */
    public static final short B5_PAPERSIZE = 13;
    /** Folio 8 1/2 x 13 in */
    public static final short FOLIO8_PAPERSIZE = 14;
    /** Quarto 215x275 mm */
    public static final short QUARTO_PAPERSIZE = 15;
    /** 10 x 14 in */
    public static final short TEN_BY_FOURTEEN_PAPERSIZE = 16;
    /** 11 x 17 in */
    public static final short ELEVEN_BY_SEVENTEEN_PAPERSIZE = 17;
    /** US Note 8 1/2 x 11 in */
    public static final short NOTE8_PAPERSIZE = 18;
    /** US Envelope #9 3 7/8 x 8 7/8 */
    public static final short ENVELOPE_9_PAPERSIZE = 19;
    /** US Envelope #10 4 1/8 x 9 1/2 */
    public static final short ENVELOPE_10_PAPERSIZE = 20;
    /** Envelope DL 110x220 mm */
    public static final short ENVELOPE_DL_PAPERSIZE = 27;
    /** Envelope C5 162x229 mm */
    public static final short ENVELOPE_CS_PAPERSIZE = 28;
    public static final short ENVELOPE_C5_PAPERSIZE = 28;
    /** Envelope C3 324x458 mm */
    public static final short ENVELOPE_C3_PAPERSIZE = 29;
    /** Envelope C4 229x324 mm */
    public static final short ENVELOPE_C4_PAPERSIZE = 30;
    /** Envelope C6 114x162 mm */
    public static final short ENVELOPE_C6_PAPERSIZE = 31;

    public static final short ENVELOPE_MONARCH_PAPERSIZE = 37;
    /** A4 Extra - 9.27 x 12.69 in */
    public static final short A4_EXTRA_PAPERSIZE = 53;
    /** A4 Transverse - 210x297 mm */
    public static final short A4_TRANSVERSE_PAPERSIZE = 55;
    /** A4 Plus - 210x330 mm */
    public static final short A4_PLUS_PAPERSIZE = 60;
    /** US Letter Rotated 11 x 8 1/2 in */
    public static final short LETTER_ROTATED_PAPERSIZE = 75;
    /** A4 Rotated - 297x210 mm */
    public static final short A4_ROTATED_PAPERSIZE = 77;

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