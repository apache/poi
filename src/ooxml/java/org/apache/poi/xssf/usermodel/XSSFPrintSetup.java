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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.POIXMLException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;


/**
 * Page setup and page margins settings for the worksheet.
 */
public class XSSFPrintSetup implements PrintSetup {

    private CTWorksheet ctWorksheet;
    private CTPageSetup pageSetup;
    private CTPageMargins pageMargins;


    protected XSSFPrintSetup(CTWorksheet worksheet) {
        this.ctWorksheet = worksheet;
        this.pageSetup = ctWorksheet.getPageSetup() == null ? ctWorksheet.addNewPageSetup() : ctWorksheet.getPageSetup();
        this.pageMargins = ctWorksheet.getPageMargins() == null ? ctWorksheet.addNewPageMargins() : ctWorksheet.getPageMargins();
    }

    /**
     * Set the paper size.
     *
     * @param size the paper size.
     */
    public void setPaperSize(short size) {
        pageSetup.setPaperSize(size);
    }

    /**
     * Set the paper size as enum value.
     *
     * @param size value for the paper size.
     */
    public void setPaperSize(PaperSize size) {
        setPaperSize((short) (size.ordinal() + 1));
    }

    /**
     * Set the scale.
     * Valid values range from 10 to 400.
     * This setting is overridden when fitToWidth and/or fitToHeight are in use
     *
     * @param scale the scale to use
     */
    public void setScale(short scale) {
        if (scale < 10 || scale > 400) throw new POIXMLException("Scale value not accepted: you must choose a value between 10 and 400.");
        pageSetup.setScale(scale);
    }

    /**
     * Set the page numbering start.
     * Page number for first printed page. If no value is specified, then 'automatic' is assumed.
     *
     * @param start the page numbering start
     */
    public void setPageStart(short start) {
        pageSetup.setFirstPageNumber(start);
    }

    /**
     * Set the number of pages wide to fit the sheet in
     *
     * @param width the number of pages
     */
    public void setFitWidth(short width) {
        pageSetup.setFitToWidth(width);
    }

    /**
     * Set the number of pages high to fit the sheet in
     *
     * @param height the number of pages
     */
    public void setFitHeight(short height) {
        pageSetup.setFitToHeight(height);
    }

    /**
     * Set whether to go left to right or top down in ordering
     *
     * @param ltor left to right
     */
    public void setLeftToRight(boolean ltor) {
        if (ltor)
            setPageOrder(PageOrder.OVER_THEN_DOWN);
    }

    /**
     * Set whether to print in landscape
     *
     * @param ls landscape
     */
    public void setLandscape(boolean ls) {
        if (ls)
            setOrientation(PrintOrientation.LANDSCAPE);
    }

    /**
     * Use the printer's defaults settings for page setup values and don't use the default values
     * specified in the schema. For example, if dpi is not present or specified in the XML, the
     * a plication shall not assume 600dpi as specified in the schema as a default and instead
     * shall let the printer specify the default dpi.
     *
     * @param valid Valid
     */
    public void setValidSettings(boolean valid) {
        pageSetup.setUsePrinterDefaults(valid);
    }

    /**
     * Set whether it is black and white
     *
     * @param mono Black and white
     */
    public void setNoColor(boolean mono) {
        pageSetup.setBlackAndWhite(mono);
    }

    /**
     * Set whether it is in draft mode
     *
     * @param d draft
     */
    public void setDraft(boolean d) {
        pageSetup.setDraft(d);
    }

    /**
     * Print the include notes
     *
     * @param printnotes print the notes
     */
    public void setNotes(boolean printnotes) {
        if (printnotes){
            pageSetup.setCellComments(STCellComments.AS_DISPLAYED);
        }
    }

    /**
     * Set no orientation.
     *
     * @param orientation Orientation.
     */
    public void setNoOrientation(boolean orientation) {
        if (orientation) {
            setOrientation(PrintOrientation.DEFAULT);
        }
    }

    /**
     * Set whether to use page start
     *
     * @param page Use page start
     */
    public void setUsePage(boolean page) {
        pageSetup.setUseFirstPageNumber(page);
    }

    /**
     * Sets the horizontal resolution.
     *
     * @param resolution horizontal resolution
     */
    public void setHResolution(short resolution) {
        pageSetup.setHorizontalDpi(resolution);
    }

    /**
     * Sets the vertical resolution.
     *
     * @param resolution vertical resolution
     */
    public void setVResolution(short resolution) {
        pageSetup.setVerticalDpi(resolution);
    }

    /**
     * Sets the header margin.
     *
     * @param headermargin header margin
     */
    public void setHeaderMargin(double headermargin) {
        pageMargins.setHeader(headermargin);
    }

    /**
     * Sets the footer margin.
     *
     * @param footermargin footer margin
     */
    public void setFooterMargin(double footermargin) {
        pageMargins.setFooter(footermargin);
    }

    /**
     * Sets the number of copies.
     *
     * @param copies number of copies
     */
    public void setCopies(short copies) {
        pageSetup.setCopies(copies);
    }

    /**
     * Orientation of the page: landscape - portrait.
     *
     * @param orientation - Orientation of the page
     * @see PrintOrientation
     */
    public void setOrientation(PrintOrientation orientation) {
        STOrientation.Enum v = STOrientation.Enum.forInt(orientation.getValue());
        pageSetup.setOrientation(v);
    }

    /**
     * Orientation of the page: landscape - portrait.
     *
     * @return Orientation of the page
     * @see PrintOrientation
     */
    public PrintOrientation getOrientation() {
        STOrientation.Enum val = pageSetup.getOrientation();
        return val == null ? PrintOrientation.DEFAULT : PrintOrientation.valueOf(val.intValue());
    }


    public PrintCellComments getCellComment() {
        STCellComments.Enum val = pageSetup.getCellComments();
        return val == null ? PrintCellComments.NONE : PrintCellComments.valueOf(val.intValue());
    }

    /**
     * Set print page order.
     *
     * @param pageOrder
     */
    public void setPageOrder(PageOrder pageOrder) {
        STPageOrder.Enum v = STPageOrder.Enum.forInt(pageOrder.getValue());
        pageSetup.setPageOrder(v);
    }

    /**
     * get print page order.
     *
     * @return PageOrder
     */
    public PageOrder getPageOrder() {
        return (pageSetup.getPageOrder() == null) ? null : PageOrder.valueOf(pageSetup.getPageOrder().intValue());
    }

    /**
     * Returns the paper size.
     *
     * @return short - paper size
     */
    public short getPaperSize() {
        return (short) pageSetup.getPaperSize();
    }

    /**
     * Returns the paper size as enum.
     *
     * @return PaperSize paper size
     * @see PaperSize
     */
    public PaperSize getPaperSizeEnum() {
        return PaperSize.values()[getPaperSize() - 1];
    }

    /**
     * Returns the scale.
     *
     * @return short - scale
     */
    public short getScale() {
        return (short) pageSetup.getScale();
    }

    /**
     * Set the page numbering start.
     * Page number for first printed page. If no value is specified, then 'automatic' is assumed.
     *
     * @return page number for first printed page
     */
    public short getPageStart() {
        return (short) pageSetup.getFirstPageNumber();
    }

    /**
     * Returns the number of pages wide to fit sheet in.
     *
     * @return number of pages wide to fit sheet in
     */
    public short getFitWidth() {
        return (short) pageSetup.getFitToWidth();
    }

    /**
     * Returns the number of pages high to fit the sheet in.
     *
     * @return number of pages high to fit the sheet in
     */
    public short getFitHeight() {
        return (short) pageSetup.getFitToHeight();
    }

    /**
     * Returns the left to right print order.
     *
     * @return left to right print order
     */
    public boolean getLeftToRight() {
        return getPageOrder() == PageOrder.OVER_THEN_DOWN;
    }

    /**
     * Returns the landscape mode.
     *
     * @return landscape mode
     */
    public boolean getLandscape() {
        return getOrientation() == PrintOrientation.LANDSCAPE;
    }

    /**
     * Use the printer's defaults settings for page setup values and don't use the default values
     * specified in the schema. For example, if dpi is not present or specified in the XML, the
     * application shall not assume 600dpi as specified in the schema as a default and instead
     * shall let the printer specify the default dpi.
     *
     * @return valid settings
     */
    public boolean getValidSettings() {
        return pageSetup.getUsePrinterDefaults();
    }

    /**
     * Returns the black and white setting.
     *
     * @return black and white setting
     */
    public boolean getNoColor() {
        return pageSetup.getBlackAndWhite();
    }

    /**
     * Returns the draft mode.
     *
     * @return draft mode
     */
    public boolean getDraft() {
        return pageSetup.getDraft();
    }

    /**
     * Returns the print notes.
     *
     * @return print notes
     */
    public boolean getNotes() {
        return getCellComment() == PrintCellComments.AS_DISPLAYED;
    }

    /**
     * Returns the no orientation.
     *
     * @return no orientation
     */
    public boolean getNoOrientation() {
        return getOrientation() == PrintOrientation.DEFAULT;
    }

    /**
     * Returns the use page numbers.
     *
     * @return use page numbers
     */
    public boolean getUsePage() {
        return pageSetup.getUseFirstPageNumber();
    }

    /**
     * Returns the horizontal resolution.
     *
     * @return horizontal resolution
     */
    public short getHResolution() {
        return (short) pageSetup.getHorizontalDpi();
    }

    /**
     * Returns the vertical resolution.
     *
     * @return vertical resolution
     */
    public short getVResolution() {
        return (short) pageSetup.getVerticalDpi();
    }

    /**
     * Returns the header margin.
     *
     * @return header margin
     */
    public double getHeaderMargin() {
        return pageMargins.getHeader();
    }

    /**
     * Returns the footer margin.
     *
     * @return footer margin
     */
    public double getFooterMargin() {
        return pageMargins.getFooter();
    }

    /**
     * Returns the number of copies.
     *
     * @return number of copies
     */
    public short getCopies() {
        return (short) pageSetup.getCopies();
    }

}
