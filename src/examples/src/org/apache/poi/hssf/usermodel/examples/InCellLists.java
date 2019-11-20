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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;

/**
 * This class contains code that demonstrates how to insert plain, numbered
 * and bulleted lists into an Excel spreadsheet cell.
 *
 * Look at the code contained in the demonstrateMethodCalls() method. It calls
 * other methods that create plain, numbered and bulleted single and
 * multi-level lists. The demonstrateMethodCalls() method appears at the top
 * of the class definition.
 *
 * Though different methods are provided to construct single and multi-level
 * plain, numbered and bulleted lists, close examination will reveal that they
 * are not strictly necessary. If the inputs to the listInCell() and
 * multilLevelListInCell() methods are constructed to include the bullet
 * character or the item numbers then these methods alone may be sufficient.
 *
 * @author Mark Beardsley [msb at apache.org]
 */
public class InCellLists {

    // This character looks like a solid, black, loser case letter 'o'
    // positioned up from the base line of the text.
    private static final char BULLET_CHARACTER = '\u2022';

    // The tab character - \t - cannot be used to create a tab space
    // within a cell as it is rendered as a square. Therefore, four
    // spaces are used to simulate that character.
    private static final String TAB = "    ";

    /**
     * Call each of the list creation methods.
     *
     * @param outputFilename A String that encapsulates the name of and path to
     *                       the Excel spreadsheet file this code will create.
     */
    public void demonstrateMethodCalls(String outputFilename) throws IOException {
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            HSSFSheet sheet = workbook.createSheet("In Cell Lists");
            HSSFRow row = sheet.createRow(0);

            // Create a cell at A1 and insert a single, bulleted, item into
            // that cell.
            HSSFCell cell = row.createCell(0);
            this.bulletedItemInCell(workbook, "List Item", cell);

            // Create a cell at A2 and insert a plain list - that is one
            // whose items are neither bulleted or numbered - into that cell.
            row = sheet.createRow(1);
            cell = row.createCell(0);
            ArrayList<String> listItems = new ArrayList<>();
            listItems.add("List Item One.");
            listItems.add("List Item Two.");
            listItems.add("List Item Three.");
            listItems.add("List Item Four.");
            this.listInCell(workbook, listItems, cell);
            // The row height and cell width are set here to ensure that the
            // list may be seen.
            row.setHeight((short) 1100);
            sheet.setColumnWidth(0, 9500);

            // Create a cell at A3 and insert a numbered list into that cell.
            // Note that a couple of items have been added to the listItems
            // ArrayList
            row = sheet.createRow(2);
            cell = row.createCell(0);
            listItems.add("List Item Five.");
            listItems.add("List Item Six.");
            this.numberedListInCell(workbook, listItems, cell, 1, 2);
            row.setHeight((short) 1550);

            // Create a cell at A4 and insert a numbered list into that cell.
            // Note that a couple of items have been added to the listItems
            // ArrayList
            row = sheet.createRow(3);
            cell = row.createCell(0);
            listItems.add("List Item Seven.");
            listItems.add("List Item Eight.");
            listItems.add("List Item Nine.");
            listItems.add("List Item Ten.");
            this.bulletedListInCell(workbook, listItems, cell);
            row.setHeight((short) 2550);

            // Insert a plain, multi-level list into cell A5. Note that
            // the major difference here is that the list items are passed as
            // an ArrayList of MultiLevelListItems. Note that an ArrayList
            // of instances of an inner class was used here in preference to
            // a Hashtable or HashMap as the ArrayList will preserve the
            // ordering of the items added to it; the first item added will
            // be the first item recovered and the last item added, the last
            // item recovered. Alternatively, a LinkedHashMap could be used
            // to preserve order.
            row = sheet.createRow(4);
            cell = row.createCell(0);
            ArrayList<MultiLevelListItem> multiLevelListItems = new ArrayList<>();
            listItems = new ArrayList<>();
            listItems.add("ML List Item One - Sub Item One.");
            listItems.add("ML List Item One - Sub Item Two.");
            listItems.add("ML List Item One - Sub Item Three.");
            listItems.add("ML List Item One - Sub Item Four.");
            multiLevelListItems.add(new MultiLevelListItem("List Item One.", listItems));
            // Passing either null or an empty ArrayList will signal that
            // there are no lower level items associated with the top level
            // item
            multiLevelListItems.add(new MultiLevelListItem("List Item Two.", null));
            multiLevelListItems.add(new MultiLevelListItem("List Item Three.", null));
            listItems = new ArrayList<>();
            listItems.add("ML List Item Four - Sub Item One.");
            listItems.add("ML List Item Four - Sub Item Two.");
            listItems.add("ML List Item Four - Sub Item Three.");
            multiLevelListItems.add(new MultiLevelListItem("List Item Four.", listItems));
            this.multiLevelListInCell(workbook, multiLevelListItems, cell);
            row.setHeight((short) 2800);

            // Insert a numbered multi-level list into cell A6. Note that the
            // same ArrayList as constructed for the above plain multi-level
            // list example will be re-used
            row = sheet.createRow(5);
            cell = row.createCell(0);
            this.multiLevelNumberedListInCell(workbook, multiLevelListItems,
                    cell, 1, 1, 1, 2);
            row.setHeight((short) 2800);

            // Insert a numbered multi-level list into cell A7. Note that the
            // same ArrayList as constructed for the plain multi-level list
            // example will be re-used
            row = sheet.createRow(6);
            cell = row.createCell(0);
            this.multiLevelBulletedListInCell(workbook, multiLevelListItems, cell);
            row.setHeight((short) 2800);

            // Save the completed workbook
            try (FileOutputStream fos = new FileOutputStream(new File(outputFilename))) {
                workbook.write(fos);
            }
        } catch (IOException ioEx) {
            System.out.println("Caught a: " + ioEx.getClass().getName());
            System.out.println("Message: " + ioEx.getMessage());
            System.out.println("Stacktrace follows...........");
            ioEx.printStackTrace(System.out);
        }
    }

    /**
     * Inserts a single bulleted item into a cell.
     *
     * @param workbook A reference to the HSSFWorkbook that 'contains' the
     *                 cell.
     * @param listItem An instance of the String class encapsulating the
     *                 items text.
     * @param cell An instance of the HSSFCell class that encapsulates a
     *             reference to the spreadsheet cell into which the list item
     *             will be written.
     */
    public void bulletedItemInCell(HSSFWorkbook workbook, String listItem, HSSFCell cell) {
        // A format String must be built to ensure that the contents of the
        // cell appear as a bulleted item.
        HSSFDataFormat format = workbook.createDataFormat();
        String formatString = InCellLists.BULLET_CHARACTER + " @";
        int formatIndex = format.getFormat(formatString);

        // Construct an HSSFCellStyle and set it's data formt to use the
        // object created above.
        HSSFCellStyle bulletStyle = workbook.createCellStyle();
        bulletStyle.setDataFormat((short)formatIndex);

        // Set the cells contents and style.
        cell.setCellValue(new HSSFRichTextString(listItem));
        cell.setCellStyle(bulletStyle);
    }

    /**
     * Inserts a list of plain items - that is items that are neither
     * numbered or bulleted - into a single cell.
     *
     * @param workbook A reference to the HSSFWorkbook that 'contains' the
     *                 cell.
     * @param listItems An ArrayList whose elements encapsulate the text for
     *                  the list's items.
     * @param cell An instance of the HSSFCell class that encapsulates a
     *             reference to the spreadsheet cell into which the list
     *             will be written.
     */
    public void listInCell(HSSFWorkbook workbook, ArrayList<String> listItems, HSSFCell cell) {
        StringBuilder buffer = new StringBuilder();
        HSSFCellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        for(String listItem : listItems) {
            buffer.append(listItem);
            buffer.append("\n");
        }
        // The StringBuilder's contents are the source for the contents
        // of the cell.
        cell.setCellValue(new HSSFRichTextString(buffer.toString().trim()));
        cell.setCellStyle(wrapStyle);
    }

    /**
     * Inserts a numbered list into a single cell.
     *
     * @param workbook A reference to the HSSFWorkbook that 'contains' the
     *                 cell.
     * @param listItems An ArrayList whose elements encapsulate the text for
     *                  the lists items.
     * @param cell An instance of the HSSFCell class that encapsulates a
     *             reference to the spreadsheet cell into which the list
     *             will be written.
     * @param startingValue A primitive int containing the number for the first
     *                      item in the list.
     * @param increment A primitive int containing the value that should be used
     *                  to calculate subsequent item numbers.
     */
    public void numberedListInCell(HSSFWorkbook workbook,
                                   ArrayList<String> listItems,
                                   HSSFCell cell,
                                   int startingValue,
                                   int increment) {
        StringBuilder buffer = new StringBuilder();
        int itemNumber = startingValue;
        // Note that again, an HSSFCellStye object is required and that
        // it's wrap text property should be set to 'true'
        HSSFCellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        // Note that the basic method is identical to the listInCell() method
        // with one difference; a number prefixed to the items text.
        for(String listItem : listItems) {
            buffer.append(itemNumber).append(". ");
            buffer.append(listItem);
            buffer.append("\n");
            itemNumber += increment;
        }
        // The StringBuilder's contents are the source for the contents
        // of the cell.
        cell.setCellValue(new HSSFRichTextString(buffer.toString().trim()));
        cell.setCellStyle(wrapStyle);
    }

    /**
     * Insert a bulleted list into a cell.
     *
     * @param workbook A reference to the HSSFWorkbook that 'contains' the
     *                 cell.
     * @param listItems An ArrayList whose elements encapsulate the text for
     *                  the lists items.
     * @param cell An instance of the HSSFCell class that encapsulates a
     *             reference to the spreadsheet cell into which the list
     *             will be written.
     */
    public void bulletedListInCell(HSSFWorkbook workbook,
                                   ArrayList<String> listItems,
                                   HSSFCell cell) {
        StringBuilder buffer = new StringBuilder();
        // Note that again, an HSSFCellStye object is required and that
        // it's wrap text property should be set to 'true'
        HSSFCellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        // Note that the basic method is identical to the listInCell() method
        // with one difference; the bullet character prefixed to the items text.
        for(String listItem : listItems) {
            buffer.append(InCellLists.BULLET_CHARACTER + " ");
            buffer.append(listItem);
            buffer.append("\n");
        }
        // The StringBuilder's contents are the source for the contents
        // of the cell.
        cell.setCellValue(new HSSFRichTextString(buffer.toString().trim()));
        cell.setCellStyle(wrapStyle);
    }

    /**
     * Insert a multi-level list into a cell.
     *
     * @param workbook A reference to the HSSFWorkbook that 'contains' the
     *                 cell.
     * @param multiLevelListItems An ArrayList whose elements contain instances
     *                            of the MultiLevelListItem class. Each element
     *                            encapsulates the text for the high level item
     *                            along with an ArrayList. Each element of this
     *                            ArrayList encapsulates the text for a lower
     *                            level item.
     * @param cell An instance of the HSSFCell class that encapsulates a
     *             reference to the spreadsheet cell into which the list
     *             will be written.
     */
    public void multiLevelListInCell(HSSFWorkbook workbook,
                                     ArrayList<MultiLevelListItem> multiLevelListItems,
                                     HSSFCell cell) {
        StringBuilder buffer = new StringBuilder();
        // Note that again, an HSSFCellStye object is required and that
        // it's wrap text property should be set to 'true'
        HSSFCellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        // Step through the ArrayList of MultilLevelListItem instances.
        for(MultiLevelListItem multiLevelListItem : multiLevelListItems) {
            // For each element in the ArrayList, get the text for the high
            // level list item......
            buffer.append(multiLevelListItem.getItemText());
            buffer.append("\n");
            // and then an ArrayList whose elements encapsulate the text
            // for the lower level list items.
            ArrayList<String> lowerLevelItems = multiLevelListItem.getLowerLevelItems();
            if(!(lowerLevelItems == null) && !(lowerLevelItems.isEmpty())) {
                for(String item : lowerLevelItems) {
                    buffer.append(InCellLists.TAB);
                    buffer.append(item);
                    buffer.append("\n");
                }
            }
        }
        // The StringBuilder's contents are the source for the contents
        // of the cell.
        cell.setCellValue(new HSSFRichTextString(buffer.toString().trim()));
        cell.setCellStyle(wrapStyle);
    }

    /**
     * Insert a multi-level list into a cell.
     *
     * @param workbook A reference to the HSSFWorkbook that 'contains' the
     *                 cell.
     * @param multiLevelListItems An ArrayList whose elements contain instances
     *                            of the MultiLevelListItem class. Each element
     *                            encapsulates the text for the high level item
     *                            along with an ArrayList. Each element of this
     *                            ArrayList encapsulates the text for a lower
     *                            level item.
     * @param cell An instance of the HSSFCell class that encapsulates a
     *             reference to the spreadsheet cell into which the list
     *             will be written.
     * @param highLevelStartingValue A primitive int containing the number
     *                               for the first high level item in the list.
     * @param highLevelIncrement A primitive int containing the value that
     *                           should be used to calculate the number of
     *                           subsequent high level item.
     * @param lowLevelStartingValue A primitive int will containing the number
     *                              for the first low level item associated
     *                              with a high level item.
     * @param lowLevelIncrement A primitive int containing the value that
     *                          should be used to calculate the number of
     *                          subsequent low level item.
     */
    public void multiLevelNumberedListInCell(HSSFWorkbook workbook,
                                             ArrayList<MultiLevelListItem> multiLevelListItems,
                                             HSSFCell cell,
                                             int highLevelStartingValue,
                                             int highLevelIncrement,
                                             int lowLevelStartingValue,
                                             int lowLevelIncrement) {
        StringBuilder buffer = new StringBuilder();
        int highLevelItemNumber = highLevelStartingValue;
        // Note that again, an HSSFCellStye object is required and that
        // it's wrap text property should be set to 'true'
        HSSFCellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        // Step through the ArrayList of MultilLevelListItem instances.
        for(MultiLevelListItem multiLevelListItem : multiLevelListItems) {
            // For each element in the ArrayList, get the text for the high
            // level list item......
            buffer.append(highLevelItemNumber);
            buffer.append(". ");
            buffer.append(multiLevelListItem.getItemText());
            buffer.append("\n");
            // and then an ArrayList whose elements encapsulate the text
            // for the lower level list items.
            ArrayList<String> lowerLevelItems = multiLevelListItem.getLowerLevelItems();
            if(!(lowerLevelItems == null) && !(lowerLevelItems.isEmpty())) {
                int lowLevelItemNumber = lowLevelStartingValue;
                for(String item : lowerLevelItems) {
                    buffer.append(InCellLists.TAB);
                    buffer.append(highLevelItemNumber);
                    buffer.append(".");
                    buffer.append(lowLevelItemNumber);
                    buffer.append(" ");
                    buffer.append(item);
                    buffer.append("\n");
                    lowLevelItemNumber += lowLevelIncrement;
                }
            }
            highLevelItemNumber += highLevelIncrement;
        }
        // The StringBuilder's contents are the source for the contents
        // of the cell.
        cell.setCellValue(new HSSFRichTextString(buffer.toString().trim()));
        cell.setCellStyle(wrapStyle);
    }

    /**
     * Insert a bulleted multi-level list into a cell.
     *
     * @param workbook A reference to the HSSFWorkbook that 'contains' the
     *                 cell.
     * @param multiLevelListItems An ArrayList whose elements contain instances
     *                            of the MultiLevelListItem class. Each element
     *                            encapsulates the text for the high level item
     *                            along with an ArrayList. Each element of this
     *                            ArrayList encapsulates the text for a lower
     *                            level item.
     * @param cell An instance of the HSSFCell class that encapsulates a
     *             reference to the spreadsheet cell into which the list
     *             will be written.
     */
    public void multiLevelBulletedListInCell(HSSFWorkbook workbook,
                                             ArrayList<MultiLevelListItem> multiLevelListItems,
                                             HSSFCell cell) {
        StringBuilder buffer = new StringBuilder();
        // Note that again, an HSSFCellStye object is required and that
        // it's wrap text property should be set to 'true'
        HSSFCellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        // Step through the ArrayList of MultilLevelListItem instances.
        for(MultiLevelListItem multiLevelListItem : multiLevelListItems) {
            // For each element in the ArrayList, get the text for the high
            // level list item......
            buffer.append(InCellLists.BULLET_CHARACTER);
            buffer.append(" ");
            buffer.append(multiLevelListItem.getItemText());
            buffer.append("\n");
            // and then an ArrayList whose elements encapsulate the text
            // for the lower level list items.
            ArrayList<String> lowerLevelItems = multiLevelListItem.getLowerLevelItems();
            if(!(lowerLevelItems == null) && !(lowerLevelItems.isEmpty())) {
                for(String item : lowerLevelItems) {
                    buffer.append(InCellLists.TAB);
                    buffer.append(InCellLists.BULLET_CHARACTER);
                    buffer.append(" ");
                    buffer.append(item);
                    buffer.append("\n");
                }
            }
        }
        // The StringBuilder's contents are the source for the contents
        // of the cell.
        cell.setCellValue(new HSSFRichTextString(buffer.toString().trim()));
        cell.setCellStyle(wrapStyle);
    }

    /**
     * The main entry point to the program. Demonstrates how to call the method
     * that will create an Excel workbook containing many different sorts of
     * lists.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) throws IOException {
        new InCellLists().demonstrateMethodCalls("Latest In Cell List.xls");
    }

    /**
     * An instance of this inner class models an item or element in a
     * multi-level list. Each multi-level list item consists of the text for the
     * high level items and an ArrayList containing the text for each of the
     * associated lower level items. When written into a cell, each multi-level
     * list item will have this general appearance.
     *
     *     Item One
     *         Sub Item One.
     *         Sub Item Two.
     *     Item Two
     *         Sub Item One.
     *         Sub Item Two.
     *     etc.
     *
     * It would be quite possible to modify this class to model much more
     * complex list structures descending through two, three or even more
     * levels.
     */
    public final class MultiLevelListItem {

        private String itemText;
        private ArrayList<String> lowerLevelItems;

        /**
         * Create a new instance of the MultiLevelListItem class using the
         * following parameters.
         *
         * @param itemText A String that encapsulates the text for the high
         *                 level list item.
         * @param lowerLevelItems An ArrayList whose elements encapsulate the
         *                        text for the associated lower level list
         *                        items.
         */
        public MultiLevelListItem(String itemText, ArrayList<String> lowerLevelItems) {
            this.itemText = itemText;
            this.lowerLevelItems = lowerLevelItems;
        }

        /**
         * Get the text for the high level list item.
         *
         * @return A String that encapsulates the text for the high level list
         *         item.
         */
        public String getItemText() {
            return(this.itemText);
        }

        /**
         * Get the text for the associated lower level list items.
         *
         * @return An ArrayList whose elements each encapsulate the text for a
         *         single associated lower level list item.
         */
        public ArrayList<String> getLowerLevelItems() {
            return(this.lowerLevelItems);
        }
    }
}
