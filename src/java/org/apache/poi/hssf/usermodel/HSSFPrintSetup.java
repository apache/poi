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

import org.apache.poi.hssf.record.PrintSetupRecord;
import org.apache.poi.ss.usermodel.PrintSetup;

/**
 * Used to modify the print setup.
 * <P>
 * Paper size constants have been added for the ones I have access
 * to.  They follow as:<br>
 *  public static final short LETTER_PAPERSIZE 	          = 1;<br>
 *  public static final short LEGAL_PAPERSIZE 		  = 5;<br>
 *  public static final short EXECUTIVE_PAPERSIZE 	  = 7;<br>
 *  public static final short A4_PAPERSIZE 	  	  = 9;<br>
 *  public static final short A5_PAPERSIZE 		  = 11;<br>
 *  public static final short ENVELOPE_10_PAPERSIZE 	  = 20;<br>
 *  public static final short ENVELOPE_DL_PAPERSIZE 	  = 27;<br>
 *  public static final short ENVELOPE_CS_PAPERSIZE 	  = 28;<br>
 *  public static final short ENVELOPE_MONARCH_PAPERSIZE  = 37;<br>
 * <P>
 * @author Shawn Laubach (slaubach at apache dot org) */
public class HSSFPrintSetup implements PrintSetup {    
    PrintSetupRecord printSetupRecord;
    
    /**    
     * Constructor.  Takes the low level print setup record.    
     * @param printSetupRecord the low level print setup record    
     */
    protected HSSFPrintSetup(PrintSetupRecord printSetupRecord) {
	this.printSetupRecord = printSetupRecord;
    }    
    
    /**    
     * Set the paper size.    
     * @param size the paper size.    
     */
    public void setPaperSize(short size)    {
	printSetupRecord.setPaperSize(size);
    }    
    
    /**    
     * Set the scale.    
     * @param scale the scale to use    
     */
    public void setScale(short scale)    {
	printSetupRecord.setScale(scale);
    }    
    
    /**    
     * Set the page numbering start.    
     * @param start the page numbering start    
     */
    public void setPageStart(short start)    {
	printSetupRecord.setPageStart(start);
    }    
    
    /**    
     * Set the number of pages wide to fit the sheet in    
     * @param width the number of pages    
     */
    public void setFitWidth(short width)    {
	printSetupRecord.setFitWidth(width);
    }    
    
    /**    
     * Set the number of pages high to fit the sheet in    
     * @param height the number of pages    
     */
    public void setFitHeight(short height)    {
	printSetupRecord.setFitHeight(height);
    }    
    
    /**    
     * Sets the options flags.  Not advisable to do it directly.    
     * @param options The bit flags for the options    
     */
    public void setOptions(short options)    {
	printSetupRecord.setOptions(options);
    }    
    
    /**    
     * Set whether to go left to right or top down in ordering    
     * @param ltor left to right    
     */
    public void setLeftToRight(boolean ltor)    {
	printSetupRecord.setLeftToRight(ltor);
    }    
    
    /**    
     * Set whether to print in landscape    
     * @param ls landscape    
     */
    public void setLandscape(boolean ls)    {
	printSetupRecord.setLandscape(!ls);
    }    
    
    /**    
     * Valid settings.  I'm not for sure.    
     * @param valid Valid    
     */
    public void setValidSettings(boolean valid)    {
	printSetupRecord.setValidSettings(valid);
    }    
    
    /**    
     * Set whether it is black and white    
     * @param mono Black and white    
     */
    public void setNoColor(boolean mono)    {
	printSetupRecord.setNoColor(mono);
    }    
    
    /**    
     * Set whether it is in draft mode    
     * @param d draft    
     */
    public void setDraft(boolean d)    {
	printSetupRecord.setDraft(d);
    }    
    
    /**    
     * Print the include notes    
     * @param printnotes print the notes    
     */
    public void setNotes(boolean printnotes)    {
	printSetupRecord.setNotes(printnotes);
    }    
    
    /**    
     * Set no orientation. ?    
     * @param orientation Orientation.    
     */
    public void setNoOrientation(boolean orientation)    {
	printSetupRecord.setNoOrientation(orientation);
    }    
    
    /**    
     * Set whether to use page start    
     * @param page Use page start    
     */
    public void setUsePage(boolean page)    {
	printSetupRecord.setUsePage(page);
    }    
    
    /**    
     * Sets the horizontal resolution.    
     * @param resolution horizontal resolution    
     */
    public void setHResolution(short resolution)    {
	printSetupRecord.setHResolution(resolution);
    }    
    
    /**    
     * Sets the vertical resolution.    
     * @param resolution vertical resolution    
     */
    public void setVResolution(short resolution)    {
	printSetupRecord.setVResolution(resolution);
    }    
    
    /**    
     * Sets the header margin.    
     * @param headermargin header margin    
     */
    public void setHeaderMargin(double headermargin)    {
	printSetupRecord.setHeaderMargin(headermargin);
    }    
    
    /**    
     * Sets the footer margin.    
     * @param footermargin footer margin    
     */
    public void setFooterMargin(double footermargin)    {
	printSetupRecord.setFooterMargin(footermargin);
    }    
    
    /**    
     * Sets the number of copies.    
     * @param copies number of copies    
     */
    public void setCopies(short copies)    {
	printSetupRecord.setCopies(copies);
    }    
    
    /**    
     * Returns the paper size.    
     * @return paper size    
     */
    public short getPaperSize()    {
        return printSetupRecord.getPaperSize();
    }    
    
    /**    
     * Returns the scale.    
     * @return scale    
     */
    public short getScale()    {
        return printSetupRecord.getScale();
    }    
    
    /**    
     * Returns the page start.    
     * @return page start    
     */
    public short getPageStart()    {
        return printSetupRecord.getPageStart();
    }    
    
    /**    
     * Returns the number of pages wide to fit sheet in.    
     * @return number of pages wide to fit sheet in    
     */
    public short getFitWidth()    {
        return printSetupRecord.getFitWidth();
    }    
    
    /**    
     * Returns the number of pages high to fit the sheet in.    
     * @return number of pages high to fit the sheet in    
     */
    public short getFitHeight()    {
        return printSetupRecord.getFitHeight();
    }    
    
    /**    
     * Returns the bit flags for the options.    
     * @return bit flags for the options    
     */
    public short getOptions()    {
        return printSetupRecord.getOptions();
    }    
    
    /**    
     * Returns the left to right print order.    
     * @return left to right print order    
     */
    public boolean getLeftToRight()    {
        return printSetupRecord.getLeftToRight();
    }    
    
    /**    
     * Returns the landscape mode.    
     * @return landscape mode    
     */
    public boolean getLandscape()    {
        return !printSetupRecord.getLandscape();
    }    
    
    /**    
     * Returns the valid settings.    
     * @return valid settings    
     */
    public boolean getValidSettings()    {
        return printSetupRecord.getValidSettings();
    }    
    
    /**    
     * Returns the black and white setting.    
     * @return black and white setting    
     */
    public boolean getNoColor()    {
        return printSetupRecord.getNoColor();
    }    
    
    /**    
     * Returns the draft mode.    
     * @return draft mode    
     */
    public boolean getDraft()    {
        return printSetupRecord.getDraft();
    }    
    
    /**    
     * Returns the print notes.    
     * @return print notes    
     */
    public boolean getNotes()    {
        return printSetupRecord.getNotes();
    }    
    
    /**    
     * Returns the no orientation.    
     * @return no orientation    
     */
    public boolean getNoOrientation()    {
        return printSetupRecord.getNoOrientation();
    }    
    
    /**    
     * Returns the use page numbers.    
     * @return use page numbers    
     */
    public boolean getUsePage()    {
        return printSetupRecord.getUsePage();
    }    
    
    /**    
     * Returns the horizontal resolution.    
     * @return horizontal resolution    
     */
    public short getHResolution()    {
        return printSetupRecord.getHResolution();
    }    
    
    /**    
     * Returns the vertical resolution.    
     * @return vertical resolution    
     */
    public short getVResolution()    {
        return printSetupRecord.getVResolution();
    }    
    
    /**    
     * Returns the header margin.    
     * @return header margin    
     */
    public double getHeaderMargin()    {
        return printSetupRecord.getHeaderMargin();
    }    
    
    /**    
     * Returns the footer margin.    
     * @return footer margin    
     */
    public double getFooterMargin()    {
        return printSetupRecord.getFooterMargin();
    }    
    
    /**    
     * Returns the number of copies.    
     * @return number of copies    
     */
    public short getCopies()    {
        return printSetupRecord.getCopies();
    }
}