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


package org.apache.poi.hssf.usermodel.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;


/**
 * Demonstrates how to add an image to a worksheet and set that image's size
 * to a specific number of milimetres irrespective of the width of the columns
 * or height of the rows. Overridden methods are provided so that the location
 * of the image - the cells row and column co-ordinates that define the top
 * left hand corners of the image - can be identified either in the familiar
 * Excel manner - A1 for instance - or using POI's methodolody of a column and
 * row index where 0, 0 would indicate cell A1.
 *
 * The best way to make use of these techniques is to delay adding the image to
 * the sheet until all other work has been completed. That way, the sizes of
 * all rows and columns will have been adjusted - assuming that step was
 * necessary. Even though the anchors type is set to prevent the image moving
 * or re-sizing, this setting does not have any effect until the sheet is being
 * viewed using the Excel application.
 *
 * The key to the process is the HSSFClientAnchor class. It accepts eight
 * parameters that define, in order;
 *
 *      * How far - in terms of co-ordinate position - the image should be inset
 *      from the left hand border of a cell.
 *      * How far - in terms of co-ordinate positions - the image should be inset
 *      from the from the top of the cell.
 *      * How far - in terms of co-ordinate positions - the right hand edge of
 *      the image should protrude into a cell (measured from the cell's left hand
 *      edge to the image's right hand edge).
 *      * How far - in terms of co-ordinate positions - the bottm edge of the
 *      image should protrude into a row (measured from the cell's top edge to
 *      the image's bottom edge).
 *      * The index of the column that contains the cell whose top left hand
 *      corner should be aligned with the top left hand corner of the image.
 *      * The index of the row that contains the cell whose top left hand corner
 *      should be aligned with the image's top left hand corner.
 *      * The index of the column that contains the cell whose top left hand
 *      corner should be aligned with the image's bottom right hand corner
 *      * The index number of the row that contains the cell whose top left
 *      hand corner should be aligned with the images bottom right hand corner.
 *
 * It can be used to add an image into cell A1, for example, in the following
 * manner;
 *
 *      HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 0, 0,
 *          (short)0, 0, (short)1, 1);
 *
 * The final four parameters determine that the top left hand corner should be
 * aligned with the top left hand corner of cell A1 and it's bottom right
 * hand corner with the top left hand corner of cell B2. Think of the image as
 * being stretched so that it's top left hand corner is aligned with the top
 * left hand corner of cell A1 and it's bottom right hand corner is aligned with
 * the top left hand corner of cell B1. Interestingly, this would also produce
 * the same results;
 *
 *       anchor = new HSSFClientAnchor(0, 0, 1023, 255,
 *          (short)0, 0, (short)0, 0);
 *
 * Note that the final four parameters all contain the same value and seem to
 * indicate that the images top left hand corner is aligned with the top left
 * hand corner of cell A1 and that it's bottom right hand corner is also
 * aligned with the top left hand corner of cell A1. Yet, running this code
 * would see the image fully occupying cell A1. That is the result of the
 * values passed to parameters three and four; these I have referred to as
 * determing the images co-ordinates within the cell. They indicate that the
 * image should occupy - in order - the full width of the column and the full
 * height of the row.
 *
 * The co-ordinate values shown are the maxima; and they are independent of
 * row height/column width and of the font used. Passing 255 will always result
 * in the image occupying the full height of the row and passing 1023 will
 * always result in the image occupying the full width of the column. They help
 * in situations where an image is larger than a column/row and must overlap
 * into the next column/row. Using them does mean, however, that it is often
 * necessary to perform conversions between Excel's characters units, points,
 * pixels and millimetres in order to establish how many rows/columns an image
 * should occupy and just what the varous insets ought to be.
 *
 * Note that the first two parameters of the HSSFClientAchor classes constructor
 * are not made use of in the code that follows. It would be fairly trivial
 * however to extend these example further and provide methods that would centre
 * an image within a cell or allow the user to specify that a plain border a
 * fixed number of millimetres wide should wrap around the image. Those first
 * two parameters would make this sort of functionality perfectly possible.
 *
 * Owing to the various conversions used, the actual size of the image may vary
 * from that required; testing has so far found this to be in the region of
 * plus or minus two millimetres. Most likely by modifying the way the
 * calculations are performed - possibly using double(s) throughout and
 * rounding the values at the correct point - it is likely that these errors
 * could be reduced or removed.
 *
 * A note concerning Excels' image resizing behaviour. The HSSFClientAnchor
 * class contains a method called setAnchorType(int) which can be used to
 * determine how Excel will resize an image in reponse to the user increasing
 * or decreasing the dimensions of the cell containing the image. There are
 * three values that can be passed to this method; 0 = To move and size the
 * image with the cell, 2 = To move but don't size the image with the cell,
 * 3 = To prevent the image from moving or being resized along with the cell. If
 * an image is inserted using this class and placed into a single cell then if
 * the setAnchorType(int) method is called and a value of either 0 or 2 passed
 * to it, the resultant resizing behaviour may be a surprise. The image will not
 * grow in size of the column is made wider or the row higher but it will shrink
 * if the columns width or rows height are reduced.
 *
 * @author Mark Beardsley [msb at apache.org]
 * @version 1.00 5th August 2009.
 */
public class AddDimensionedImage {

    // Four constants that determine how - and indeed whether - the rows
    // and columns an image may overlie should be expanded to accommodate that
    // image.
    // Passing EXPAND_ROW will result in the height of a row being increased
    // to accommodate the image if it is not already larger. The image will
    // be layed across one or more columns.
    // Passing EXPAND_COLUMN will result in the width of the column being
    // increased to accommodate the image if it is not already larger. The image
    // will be layed across one or many rows.
    // Passing EXPAND_ROW_AND_COLUMN will result in the height of the row
    // bing increased along with the width of the column to accomdate the
    // image if either is not already larger.
    // Passing OVERLAY_ROW_AND_COLUMN will result in the image being layed
    // over one or more rows and columns. No row or column will be resized,
    // instead, code will determine how many rows and columns the image should
    // overlie.
    public static final int EXPAND_ROW = 1;
    public static final int EXPAND_COLUMN = 2;
    public static final int EXPAND_ROW_AND_COLUMN = 3;
    public static final int OVERLAY_ROW_AND_COLUMN = 7;

    /**
     * Add an image to a worksheet.
     *
     * @param cellNumber A String that contains the location of the cell whose
     *                   top left hand corner should be aligned with the top
     *                   left hand corner of the image; for example "A1", "A2"
     *                   etc. This is to support the familiar Excel syntax.
     *                   Whilst images are are not actually inserted into cells
     *                   this provides a convenient method of indicating where
     *                   the image should be positioned on the sheet.
     * @param sheet A reference to the sheet that contains the cell referenced
     *              above.
     * @param imageFile A String that encapsulates the name of and path to
     *                  the image that is to be 'inserted into' the sheet.
     * @param reqImageWidthMM A primitive double that contains the required
     *                        width of the image in millimetres.
     * @param reqImageHeightMM A primitive double that contains the required
     *                         height of the image in millimetres.
     * @param resizeBehaviour A primitive int whose value will determine how
     *                        the code should react if the image is larger than
     *                        the cell referenced by the cellNumber parameter.
     *                        Four constants are provided to determine what
     *                        should happen;
     *                          AddDimensionedImage.EXPAND_ROW
     *                          AddDimensionedImage.EXPAND_COLUMN
     *                          AddDimensionedImage.EXPAND_ROW_AND_COLUMN
     *                          AddDimensionedImage.OVERLAY_ROW_AND_COLUMN
     * @throws java.io.FileNotFoundException If the file containing the image
     *                                       cannot be located.
     * @throws java.io.IOException If a problem occurs whilst reading the file
     *                             of image data.
     * @throws java.lang.IllegalArgumentException If an invalid value is passed
     *                                            to the resizeBehaviour
     *                                            parameter.
     */
    public void addImageToSheet(String cellNumber, HSSFSheet sheet,
            String imageFile, double reqImageWidthMM, double reqImageHeightMM,
            int resizeBehaviour) throws IOException, IllegalArgumentException {
        // Convert the String into column and row indices then chain the
        // call to the overridden addImageToSheet() method.
        CellReference cellRef = new CellReference(cellNumber);
        this.addImageToSheet(cellRef.getCol(), cellRef.getRow(), sheet,
                imageFile, reqImageWidthMM, reqImageHeightMM,resizeBehaviour);
    }

    /**
     * Add an image to a worksheet.
     *
     * @param colNumber A primitive int that contains the index number of a
     *                  column on the worksheet; POI column indices are zero
     *                  based. Together with the rowNumber parameter's value,
     *                  this parameter identifies a cell on the worksheet. The
     *                  image's top left hand corner will be aligned with the
     *                  top left hand corner of this cell.
     * @param rowNumber A primtive int that contains the index number of a row
     *                  on the worksheet; POI row indices are zero based.
     *                  Together with the rowNumber parameter's value, this
     *                  parameter identifies a cell on the worksheet. The
     *                  image's top left hand corner will be aligned with the
     *                  top left hand corner of this cell.
     * @param sheet A reference to the sheet that contains the cell identified
     *              by the two parameters above.
     * @param imageFile A String that encapsulates the name of and path to
     *                  the image that is to be 'inserted into' the sheet.
     * @param reqImageWidthMM A primitive double that contains the required
     *                        width of the image in millimetres.
     * @param reqImageHeightMM A primitive double that contains the required
     *                         height of the image in millimetres.
     * @param resizeBehaviour A primitive int whose value will determine how
     *                        the code should react if the image is larger than
     *                        the cell referenced by the colNumber and
     *                        rowNumber parameters. Four constants are provided
     *                        to determine what should happen;
     *                          AddDimensionedImage.EXPAND_ROW
     *                          AddDimensionedImage.EXPAND_COLUMN
     *                          AddDimensionedImage.EXPAND_ROW_AND_COLUMN
     *                          AddDimensionedImage.OVERLAY_ROW_AND_COLUMN
     * @throws java.io.FileNotFoundException If the file containing the image
     *                                       cannot be located.
     * @throws java.io.IOException If a problem occurs whilst reading the file
     *                             of image data.
     * @throws java.lang.IllegalArgumentException If an invalid value is passed
     *                                            to the resizeBehaviour
     *                                            parameter.
     */
    private void addImageToSheet(int colNumber, int rowNumber, HSSFSheet sheet,
            String imageFile, double reqImageWidthMM, double reqImageHeightMM,
            int resizeBehaviour) throws FileNotFoundException, IOException,
                                                     IllegalArgumentException  {
        HSSFClientAnchor anchor;
        HSSFPatriarch patriarch;
        ClientAnchorDetail rowClientAnchorDetail;
        ClientAnchorDetail colClientAnchorDetail;

        // Validate the resizeBehaviour parameter.
        if((resizeBehaviour != AddDimensionedImage.EXPAND_COLUMN) &&
           (resizeBehaviour != AddDimensionedImage.EXPAND_ROW) &&
           (resizeBehaviour != AddDimensionedImage.EXPAND_ROW_AND_COLUMN) &&
           (resizeBehaviour != AddDimensionedImage.OVERLAY_ROW_AND_COLUMN)) {
            throw new IllegalArgumentException("Invalid value passed to the " +
                    "resizeBehaviour parameter of AddDimensionedImage.addImageToSheet()");
        }

        // Call methods to calculate how the image and sheet should be
        // manipulated to accommodate the image; columns and then rows.
        colClientAnchorDetail = this.fitImageToColumns(sheet, colNumber,
                reqImageWidthMM, resizeBehaviour);
        rowClientAnchorDetail = this.fitImageToRows(sheet, rowNumber,
                reqImageHeightMM, resizeBehaviour);

        // Having determined if and how to resize the rows, columns and/or the
        // image, create the HSSFClientAnchor object to position the image on
        // the worksheet. Note how the two ClientAnchorDetail records are
        // interrogated to recover the row/column co-ordinates and any insets.
        // The first two parameters are not used currently but could be if the
        // need arose to extend the functionality of this code by adding the
        // ability to specify that a clear 'border' be placed around the image.
        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
        short col1 = 0, col2 = 0, row1 = 0, row2 = 0;
        if (colClientAnchorDetail != null) {
            dx2 = colClientAnchorDetail.getInset();
            col1 = (short)colClientAnchorDetail.getFromIndex();
            col2 = (short)colClientAnchorDetail.getToIndex();
        }
        if (rowClientAnchorDetail != null) {
            dy2 = rowClientAnchorDetail.getInset();
            row1 = (short)rowClientAnchorDetail.getFromIndex();
            row2 = (short)rowClientAnchorDetail.getToIndex();
        }
        anchor = new HSSFClientAnchor(dx1, dy1, dx2, dy2, col1, row1, col2, row2);

        // For now, set the anchor type to do not move or resize the
        // image as the size of the row/column is adjusted. This could easilly
        // become another parameter passed to the method.
        //anchor.setAnchorType(HSSFClientAnchor.DONT_MOVE_AND_RESIZE);
        anchor.setAnchorType(AnchorType.MOVE_AND_RESIZE);

        // Now, add the picture to the workbook. Note that the type is assumed
        // to be a JPEG/JPG, this could easily (and should) be parameterised
        // however.
        //int index = sheet.getWorkbook().addPicture(this.imageToBytes(imageFile),
        //            HSSFWorkbook.PICTURE_TYPE_JPEG);
        int index = sheet.getWorkbook().addPicture(this.imageToBytes(imageFile), HSSFWorkbook.PICTURE_TYPE_PNG);

        // Get the drawing patriarch and create the picture.
        patriarch = sheet.createDrawingPatriarch();
        patriarch.createPicture(anchor, index);
    }

    /**
     * Determines whether the sheets columns should be re-sized to accommodate
     * the image, adjusts the columns width if necessary and creates then
     * returns a ClientAnchorDetail object that facilitates construction of
     * an HSSFClientAnchor that will fix the image on the sheet and establish
     * it's size.
     *
     * @param sheet A reference to the sheet that will 'contain' the image.
     * @param colNumber A primtive int that contains the index number of a
     *                  column on the sheet.
     * @param reqImageWidthMM A primtive double that contains the required
     *                        width of the image in millimetres
     * @param resizeBehaviour A primitve int whose value will indicate how the
     *                        width of the column should be adjusted if the
     *                        required width of the image is greater than the
     *                        width of the column.
     * @return An instance of the ClientAnchorDetail class that will contain
     *         the index number of the column containing the cell whose top
     *         left hand corner also defines the top left hand corner of the
     *         image, the index number column containing the cell whose top
     *         left hand corner also defines the bottom right hand corner of
     *         the image and an inset that determines how far the right hand
     *         edge of the image can protrude into the next column - expressed
     *         as a specific number of co-ordinate positions.
     */
    private ClientAnchorDetail fitImageToColumns(HSSFSheet sheet, int colNumber,
            double reqImageWidthMM, int resizeBehaviour) {

        double colWidthMM;
        double colCoordinatesPerMM;
        int pictureWidthCoordinates;
        ClientAnchorDetail colClientAnchorDetail = null;

        // Get the colum's width in millimetres
        colWidthMM = ConvertImageUnits.widthUnits2Millimetres(
                (short)sheet.getColumnWidth(colNumber));

        // Check that the column's width will accommodate the image at the
        // required dimension. If the width of the column is LESS than the
        // required width of the image, decide how the application should
        // respond - resize the column or overlay the image across one or more
        // columns.
        if(colWidthMM < reqImageWidthMM) {

            // Should the column's width simply be expanded?
            if((resizeBehaviour == AddDimensionedImage.EXPAND_COLUMN) ||
               (resizeBehaviour == AddDimensionedImage.EXPAND_ROW_AND_COLUMN)) {
                // Set the width of the column by converting the required image
                // width from millimetres into Excel's column width units.
                sheet.setColumnWidth(colNumber,
                        ConvertImageUnits.millimetres2WidthUnits(reqImageWidthMM));
                // To make the image occupy the full width of the column, convert
                // the required width of the image into co-ordinates. This value
                // will become the inset for the ClientAnchorDetail class that
                // is then instantiated.
                colWidthMM = reqImageWidthMM;
                colCoordinatesPerMM = ConvertImageUnits.TOTAL_COLUMN_COORDINATE_POSITIONS /
                    colWidthMM;
                pictureWidthCoordinates = (int)(reqImageWidthMM * colCoordinatesPerMM);
                colClientAnchorDetail = new ClientAnchorDetail(colNumber,
                        colNumber, pictureWidthCoordinates);
            }
            // If the user has chosen to overlay both rows and columns or just
            // to expand ONLY the size of the rows, then calculate how to lay
            // the image out across one or more columns.
            else if ((resizeBehaviour == AddDimensionedImage.OVERLAY_ROW_AND_COLUMN) ||
                     (resizeBehaviour == AddDimensionedImage.EXPAND_ROW)) {
                colClientAnchorDetail = this.calculateColumnLocation(sheet,
                        colNumber, reqImageWidthMM);
            }
        }
        // If the column is wider than the image.
        else {
            // Mow many co-ordinate positions are there per millimetre?
            colCoordinatesPerMM = ConvertImageUnits.TOTAL_COLUMN_COORDINATE_POSITIONS /
                    colWidthMM;
            // Given the width of the image, what should be it's co-ordinate?
            pictureWidthCoordinates = (int)(reqImageWidthMM * colCoordinatesPerMM);
            colClientAnchorDetail = new ClientAnchorDetail(colNumber,
                    colNumber, pictureWidthCoordinates);
        }
        return(colClientAnchorDetail);
    }

    /**
     * Determines whether the sheet's row should be re-sized to accommodate
     * the image, adjusts the rows height if necessary and creates then
     * returns a ClientAnchorDetail object that facilitates construction of
     * an HSSFClientAnchor that will fix the image on the sheet and establish
     * it's size.
     *
     * @param sheet A reference to the sheet that will 'contain' the image.
     * @param rowNumber A primtive int that contains the index number of a
     *                  row on the sheet.
     * @param reqImageHeightMM A primtive double that contains the required
     *                         height of the image in millimetres
     * @param resizeBehaviour A primitve int whose value will indicate how the
     *                        height of the row should be adjusted if the
     *                        required height of the image is greater than the
     *                        height of the row.
     * @return An instance of the ClientAnchorDetail class that will contain
     *         the index number of the row containing the cell whose top
     *         left hand corner also defines the top left hand corner of the
     *         image, the index number of the row containing the cell whose
     *         top left hand corner also defines the bottom right hand
     *         corner of the image and an inset that determines how far the
     *         bottom edge of the image can protrude into the next (lower)
     *         row - expressed as a specific number of co-ordinate positions.
     */
    private ClientAnchorDetail fitImageToRows(HSSFSheet sheet, int rowNumber,
            double reqImageHeightMM, int resizeBehaviour) {
        double rowCoordinatesPerMM;
        int pictureHeightCoordinates;
        ClientAnchorDetail rowClientAnchorDetail = null;

        // Get the row and it's height
        HSSFRow row = sheet.getRow(rowNumber);
        if(row == null) {
            // Create row if it does not exist.
            row = sheet.createRow(rowNumber);
        }

        // Get the row's height in millimetres
        double rowHeightMM = row.getHeightInPoints() / ConvertImageUnits.POINTS_PER_MILLIMETRE;

        // Check that the row's height will accommodate the image at the required
        // dimensions. If the height of the row is LESS than the required height
        // of the image, decide how the application should respond - resize the
        // row or overlay the image across a series of rows.
        if(rowHeightMM < reqImageHeightMM) {
            if((resizeBehaviour == AddDimensionedImage.EXPAND_ROW) ||
               (resizeBehaviour == AddDimensionedImage.EXPAND_ROW_AND_COLUMN)) {
                row.setHeightInPoints((float)(reqImageHeightMM *
                        ConvertImageUnits.POINTS_PER_MILLIMETRE));
                rowHeightMM = reqImageHeightMM;
                rowCoordinatesPerMM = ConvertImageUnits.TOTAL_ROW_COORDINATE_POSITIONS /
                    rowHeightMM;
                pictureHeightCoordinates = (int)(reqImageHeightMM * rowCoordinatesPerMM);
                rowClientAnchorDetail = new ClientAnchorDetail(rowNumber,
                        rowNumber, pictureHeightCoordinates);
            }
            // If the user has chosen to overlay both rows and columns or just
            // to expand ONLY the size of the columns, then calculate how to lay
            // the image out ver one or more rows.
            else if((resizeBehaviour == AddDimensionedImage.OVERLAY_ROW_AND_COLUMN) ||
                    (resizeBehaviour == AddDimensionedImage.EXPAND_COLUMN)) {
                rowClientAnchorDetail = this.calculateRowLocation(sheet,
                        rowNumber, reqImageHeightMM);
            }
        }
        // Else, if the image is smaller than the space available
        else {
            rowCoordinatesPerMM = ConvertImageUnits.TOTAL_ROW_COORDINATE_POSITIONS /
                    rowHeightMM;
            pictureHeightCoordinates = (int)(reqImageHeightMM * rowCoordinatesPerMM);
            rowClientAnchorDetail = new ClientAnchorDetail(rowNumber,
                        rowNumber, pictureHeightCoordinates);
        }
        return(rowClientAnchorDetail);
    }

    /**
     * If the image is to overlie more than one column, calculations need to be
     * performed to determine how many columns and whether the image will
     * overlie just a part of one column in order to be presented at the
     * required size.
     *
     * @param sheet The sheet that will 'contain' the image.
     * @param startingColumn A primitive int whose value is the index of the
     *                       column that contains the cell whose top left hand
     *                       corner should be aligned with the top left hand
     *                       corner of the image.
     * @param reqImageWidthMM A primitive double whose value will indicate the
     *                        required width of the image in millimetres.
     * @return An instance of the ClientAnchorDetail class that will contain
     *         the index number of the column containing the cell whose top
     *         left hand corner also defines the top left hand corner of the
     *         image, the index number column containing the cell whose top
     *         left hand corner also defines the bottom right hand corner of
     *         the image and an inset that determines how far the right hand
     *         edge of the image can protrude into the next column - expressed
     *         as a specific number of co-ordinate positions.
     */
    private ClientAnchorDetail calculateColumnLocation(HSSFSheet sheet,
                                                       int startingColumn,
                                                       double reqImageWidthMM) {
        ClientAnchorDetail anchorDetail;
        double totalWidthMM = 0.0D;
        double colWidthMM = 0.0D;
        double overlapMM;
        double coordinatePositionsPerMM;
        int toColumn = startingColumn;
        int inset;

        // Calculate how many columns the image will have to
        // span in order to be presented at the required size.
        while(totalWidthMM < reqImageWidthMM) {
            colWidthMM = ConvertImageUnits.widthUnits2Millimetres(
                    (short)(sheet.getColumnWidth(toColumn)));
            // Note use of the cell border width constant. Testing with an image
            // declared to fit exactly into one column demonstrated that it's
            // width was greater than the width of the column the POI returned.
            // Further, this difference was a constant value that I am assuming
            // related to the cell's borders. Either way, that difference needs
            // to be allowed for in this calculation.
            totalWidthMM += (colWidthMM + ConvertImageUnits.CELL_BORDER_WIDTH_MILLIMETRES);
            toColumn++;
        }
        // De-crement by one the last column value.
        toColumn--;
        // Highly unlikely that this will be true but, if the width of a series
        // of columns is exactly equal to the required width of the image, then
        // simply build a ClientAnchorDetail object with an inset equal to the
        // total number of co-ordinate positions available in a column, a
        // from column co-ordinate (top left hand corner) equal to the value
        // of the startingColumn parameter and a to column co-ordinate equal
        // to the toColumn variable.
        //
        // Convert both values to ints to perform the test.
        if((int)totalWidthMM == (int)reqImageWidthMM) {
            // A problem could occur if the image is sized to fit into one or
            // more columns. If that occurs, the value in the toColumn variable
            // will be in error. To overcome this, there are two options, to
            // ibcrement the toColumn variable's value by one or to pass the
            // total number of co-ordinate positions to the third paramater
            // of the ClientAnchorDetail constructor. For no sepcific reason,
            // the latter option is used below.
            anchorDetail = new ClientAnchorDetail(startingColumn,
                    toColumn, ConvertImageUnits.TOTAL_COLUMN_COORDINATE_POSITIONS);
        }
        // In this case, the image will overlap part of another column and it is
        // necessary to calculate just how much - this will become the inset
        // for the ClientAnchorDetail object.
        else {
            // Firstly, claculate how much of the image should overlap into
            // the next column.
            overlapMM = reqImageWidthMM - (totalWidthMM - colWidthMM);

            // When the required size is very close indded to the column size,
            // the calcaulation above can produce a negative value. To prevent
            // problems occuring in later caculations, this is simply removed
            // be setting the overlapMM value to zero.
            if(overlapMM < 0) {
                overlapMM = 0.0D;
            }

            // Next, from the columns width, calculate how many co-ordinate
            // positons there are per millimetre
            coordinatePositionsPerMM = colWidthMM == 0 ? 0
                : ConvertImageUnits.TOTAL_COLUMN_COORDINATE_POSITIONS / colWidthMM;
            // From this figure, determine how many co-ordinat positions to
            // inset the left hand or bottom edge of the image.
            inset = (int)(coordinatePositionsPerMM * overlapMM);

            // Now create the ClientAnchorDetail object, setting the from and to
            // columns and the inset.
            anchorDetail = new ClientAnchorDetail(startingColumn, toColumn, inset);
        }
        return(anchorDetail);
    }

    /**
     * If the image is to overlie more than one rows, calculations need to be
     * performed to determine how many rows and whether the image will
     * overlie just a part of one row in order to be presented at the
     * required size.
     *
     * @param sheet The sheet that will 'contain' the image.
     * @param startingRow A primitive int whose value is the index of the row
     *                    that contains the cell whose top left hand corner
     *                    should be aligned with the top left hand corner of
     *                    the image.
     * @param reqImageHeightMM A primitive double whose value will indicate the
     *                         required height of the image in millimetres.
     * @return An instance of the ClientAnchorDetail class that will contain
     *         the index number of the row containing the cell whose top
     *         left hand corner also defines the top left hand corner of the
     *         image, the index number of the row containing the cell whose top
     *         left hand corner also defines the bottom right hand corner of
     *         the image and an inset that determines how far the bottom edge
     *         can protrude into the next (lower) row - expressed as a specific
     *         number of co-ordinate positions.
     */
    private ClientAnchorDetail calculateRowLocation(HSSFSheet sheet,
            int startingRow, double reqImageHeightMM) {
        ClientAnchorDetail clientAnchorDetail;
        HSSFRow row;
        double rowHeightMM = 0.0D;
        double totalRowHeightMM = 0.0D;
        double overlapMM;
        double rowCoordinatesPerMM;
        int toRow = startingRow;
        int inset;

        // Step through the rows in the sheet and accumulate a total of their
        // heights.
        while(totalRowHeightMM < reqImageHeightMM) {
            row = sheet.getRow(toRow);
            // Note, if the row does not already exist on the sheet then create
            // it here.
            if(row == null) {
                row = sheet.createRow(toRow);
            }
            // Get the row's height in millimetres and add to the running total.
            rowHeightMM = row.getHeightInPoints() / ConvertImageUnits.POINTS_PER_MILLIMETRE;
            totalRowHeightMM += rowHeightMM;
            toRow++;
        }
        // Owing to the way the loop above works, the rowNumber will have been
        // incremented one row too far. Undo that here.
        toRow--;
        // Check to see whether the image should occupy an exact number of
        // rows. If so, build the ClientAnchorDetail record to point
        // to those rows and with an inset of the total number of co-ordinate
        // position in the row.
        //
        // To overcome problems that can occur with comparing double values for
        // equality, cast both to int(s) to truncate the value; VERY crude and
        // I do not really like it!!
        if((int)totalRowHeightMM == (int)reqImageHeightMM) {
            clientAnchorDetail = new ClientAnchorDetail(startingRow, toRow,
                    ConvertImageUnits.TOTAL_ROW_COORDINATE_POSITIONS);
        }
        else {
            // Calculate how far the image will project into the next row. Note
            // that the height of the last row assessed is subtracted from the
            // total height of all rows assessed so far.
            overlapMM = reqImageHeightMM - (totalRowHeightMM - rowHeightMM);

            // To prevent an exception being thrown when the required width of
            // the image is very close indeed to the column size.
            if(overlapMM < 0) {
                overlapMM = 0.0D;
            }

            rowCoordinatesPerMM = rowHeightMM == 0 ? 0
                : ConvertImageUnits.TOTAL_ROW_COORDINATE_POSITIONS / rowHeightMM;
            inset = (int)(overlapMM * rowCoordinatesPerMM);
            clientAnchorDetail = new ClientAnchorDetail(startingRow, toRow, inset);
        }
        return(clientAnchorDetail);
    }

    /**
     * Loads - reads in and converts into an array of byte(s) - an image from
     * a named file.
     *
     * Note: this method should be modified so that the type of the image may
     * also be passed to it. Currently, it assumes that all images are
     * JPG/JPEG(s).
     *
     * @param imageFilename A String that encapsulates the path to and name
     *                      of the file that contains the image which is to be
     *                      'loaded'.
     * @return An array of type byte that contains the raw data of the named
     *         image.
     * @throws java.io.FileNotFoundException Thrown if it was not possible to
     *                                       open the specified file.
     * @throws java.io.IOException Thrown if reading the file failed or was
     *                             interrupted.
     */
    private byte[] imageToBytes(String imageFilename) throws IOException {
        File imageFile = new File(imageFilename);
        try (FileInputStream fis = new FileInputStream(imageFile)) {
            return IOUtils.toByteArray(fis);
        }
    }

    /**
     * The main entry point to the program. It contains code that demonstrates
     * one way to use the program.
     *
     * Note, the code is not restricted to use on new workbooks only. If an
     * image is to be inserted into an existing workbook. just open that
     * workbook, gat a reference to a sheet and pass that;
     *
     *      AddDimensionedImage addImage = new AddDimensionedImage();
     *
     *      File file = new File("....... Existing Workbook .......");
     *      FileInputStream fis = new FileInputStream(file);
     *      HSSFWorkbook workbook = new HSSFWorkbook(fis);
     *      HSSFSheet sheet = workbook.getSheetAt(0);
     *      addImage.addImageToSheet("C3", sheet, "image.jpg", 30, 20,
     *          AddDimensionedImage.EXPAND.ROW);
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        if(args.length < 2){
            System.err.println("Usage: AddDimensionedImage imageFile outputFile");
            return;
        }
        String imageFile = args[0];
        String outputFile = args[1];

        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            HSSFSheet sheet = workbook.createSheet("Picture Test");
            new AddDimensionedImage().addImageToSheet("A1", sheet,
                    imageFile, 125, 125,
                    AddDimensionedImage.EXPAND_ROW_AND_COLUMN);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * The HSSFClientAnchor class accepts eight parameters. In order, these are;
     *
     *      * How far the left hand edge of the image is inset from the left hand
     *      edge of the cell
     *      * How far the top edge of the image is inset from the top of the cell
     *      * How far the right hand edge of the image is inset from the left
     *      hand edge of the cell
     *      * How far the bottom edge of the image is inset from the top of the
     *      cell.
     *      * Together, parameters five and six determine the column and row
     *      co-ordinates of the cell whose top left hand corner will be aligned
     *      with the image's top left hand corner.
     *      * Together, parameter seven and eight determine the column and row
     *      co-ordinates of the cell whose top left hand corner will be aligned
     *      with the images bottom right hand corner.
     *
     * An instance of the ClientAnchorDetail class provides three of the eight
     * parameters, one of the co-ordinates for the images top left hand corner,
     * one of the co-ordinates for the images bottom right hand corner and
     * either how far the image should be inset from the top or the left hand
     * edge of the cell.
     *
     * @author Mark Beardsley [mas at apache.org]
     * @version 1.00 5th August 2009.
     */
    public static class ClientAnchorDetail {

        private int fromIndex;
        private int toIndex;
        private int inset;

        /**
         * Create a new instance of the ClientAnchorDetail class using the
         * following parameters.
         *
         * @param fromIndex A primitive int that contains one of the
         *                  co-ordinates (row or column index) for the top left
         *                  hand corner of the image.
         * @param toIndex A primitive int that contains one of the
         *                co-ordinates (row or column index) for the bottom
         *                right hand corner of the image.
         * @param inset A primitive int that contains a value which indicates
         *              how far the image should be inset from the top or the
         *              left hand edge of a cell.
         */
        public ClientAnchorDetail(int fromIndex, int toIndex, int inset) {
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
            this.inset = inset;
        }

        /**
         * Get one of the number of the column or row that contains the cell
         * whose top left hand corner will be aligned with the top left hand
         * corner of the image.
         *
         * @return The value - row or column index - for one of the co-ordinates
         *         of the top left hand corner of the image.
         */
        public int getFromIndex() {
            return(this.fromIndex);
        }

        /**
         * Get one of the number of the column or row that contains the cell
         * whose top left hand corner will be aligned with the bottom righ hand
         * corner of the image.
         *
         * @return The value - row or column index - for one of the co-ordinates
         *         of the bottom right hand corner of the image.
         */
        public int getToIndex() {
            return(this.toIndex);
        }

        /**
         * Get the image's offset from the edge of a cell.
         *
         * @return How far either the right hand or bottom edge of the image is
         *         inset from the left hand or top edge of a cell.
         */
        public int getInset() {
            return(this.inset);
        }
    }

    /**
     * Utility methods used to convert Excel's character based column and row
     * size measurements into pixels and/or millimetres. The class also contains
     * various constants that are required in other calculations.
     *
     * @author xio[darjino@hotmail.com]
     * @version 1.01 30th July 2009.
     *      Added by Mark Beardsley [msb at apache.org].
     *          Additional constants.
     *          widthUnits2Millimetres() and millimetres2Units() methods.
     */
    private static final class ConvertImageUnits {

        // Each cell conatins a fixed number of co-ordinate points; this number
        // does not vary with row height or column width or with font. These two
        // constants are defined below.
        public static final int TOTAL_COLUMN_COORDINATE_POSITIONS = 1023; // MB
        public static final int TOTAL_ROW_COORDINATE_POSITIONS = 255;     // MB
        // Cnstants that defines how many pixels and points there are in a
        // millimetre. These values are required for the conversion algorithm.
        public static final double PIXELS_PER_MILLIMETRES = 3.78;         // MB
        public static final double POINTS_PER_MILLIMETRE = 2.83;          // MB
        // The column width returned by HSSF and the width of a picture when
        // positioned to exactly cover one cell are different by almost exactly
        // 2mm - give or take rounding errors. This constant allows that
        // additional amount to be accounted for when calculating how many
        // celles the image ought to overlie.
        public static final double CELL_BORDER_WIDTH_MILLIMETRES = 2.0D;  // MB
        public static final short EXCEL_COLUMN_WIDTH_FACTOR = 256;
        public static final int UNIT_OFFSET_LENGTH = 7;
        private static final int[] UNIT_OFFSET_MAP = new int[]
            { 0, 36, 73, 109, 146, 182, 219 };

        /**
        * pixel units to excel width units(units of 1/256th of a character width)
        */
        public static short pixel2WidthUnits(int pxs) {
            short widthUnits = (short) (EXCEL_COLUMN_WIDTH_FACTOR *
                    (pxs / UNIT_OFFSET_LENGTH));
            widthUnits += UNIT_OFFSET_MAP[(pxs % UNIT_OFFSET_LENGTH)];
            return widthUnits;
        }

        /**
         * excel width units(units of 1/256th of a character width) to pixel
         * units.
         */
        public static int widthUnits2Pixel(short widthUnits) {
            int pixels = (widthUnits / EXCEL_COLUMN_WIDTH_FACTOR)
                    * UNIT_OFFSET_LENGTH;
            int offsetWidthUnits = widthUnits % EXCEL_COLUMN_WIDTH_FACTOR;
            pixels += Math.round(offsetWidthUnits /
                    ((float) EXCEL_COLUMN_WIDTH_FACTOR / UNIT_OFFSET_LENGTH));
            return pixels;
        }

        /**
         * Convert Excel's width units into millimetres.
         *
         * @param widthUnits The width of the column or the height of the
         *                   row in Excel's units.
         * @return A primitive double that contains the columns width or rows
         *         height in millimetres.
         */
        public static double widthUnits2Millimetres(short widthUnits) {
            return(ConvertImageUnits.widthUnits2Pixel(widthUnits) /
                   ConvertImageUnits.PIXELS_PER_MILLIMETRES);
        }

        /**
         * Convert into millimetres Excel's width units..
         *
         * @param millimetres A primitive double that contains the columns
         *                    width or rows height in millimetres.
         * @return A primitive int that contains the columns width or rows
         *         height in Excel's units.
         */
        public static int millimetres2WidthUnits(double millimetres) {
            return(ConvertImageUnits.pixel2WidthUnits((int)(millimetres *
                    ConvertImageUnits.PIXELS_PER_MILLIMETRES)));
        }
    }
}