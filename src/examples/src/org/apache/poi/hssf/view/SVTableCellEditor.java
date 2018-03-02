
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

package org.apache.poi.hssf.view;

import static org.apache.poi.hssf.view.SVTableUtils.getAWTColor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Sheet Viewer Table Cell Editor -- not commented via javadoc as it
 * nearly completely consists of overridden methods.
 *
 * @author Jason Height
 */
public class SVTableCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
    private static final Color black = getAWTColor(HSSFColorPredefined.BLACK);
    private static final Color white = getAWTColor(HSSFColorPredefined.WHITE);
    private static final POILogger logger = POILogFactory.getLogger(SVTableCellEditor.class);

    private HSSFWorkbook wb;
    private JTextField editor;

    public SVTableCellEditor(HSSFWorkbook wb) {
        this.wb = wb;
        this.editor = new JTextField();
    }


    /**
     * Gets the cellEditable attribute of the SVTableCellEditor object
     *
     * @return The cellEditable value
     */
    @Override
    public boolean isCellEditable(java.util.EventObject e) {
        if (e instanceof MouseEvent) {
            return ((MouseEvent) e).getClickCount() >= 2;
        }
        return false;
    }


    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }


    public boolean startCellEditing(EventObject anEvent) {
        logger.log(POILogger.INFO, "Start Cell Editing");
        return true;
    }


    @Override
    public boolean stopCellEditing() {
        logger.log(POILogger.INFO, "Stop Cell Editing");
        fireEditingStopped();
        return true;
    }


    @Override
    public void cancelCellEditing() {
        logger.log(POILogger.INFO, "Cancel Cell Editing");
        fireEditingCanceled();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        logger.log(POILogger.INFO, "Action performed");
        stopCellEditing();
    }


    /**
     * Gets the cellEditorValue attribute of the SVTableCellEditor object
     *
     * @return The cellEditorValue value
     */
    @Override
    public Object getCellEditorValue() {
        logger.log(POILogger.INFO, "GetCellEditorValue");
        //JMH Look at when this method is called. Should it return a HSSFCell?
        return editor.getText();
    }


    /**
     * Gets the tableCellEditorComponent attribute of the SVTableCellEditor object
     *
     * @return The tableCellEditorComponent value
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {
        logger.log(POILogger.INFO, "GetTableCellEditorComponent");
        HSSFCell cell = (HSSFCell) value;
        if (cell != null) {
            HSSFCellStyle style = cell.getCellStyle();
            HSSFFont f = wb.getFontAt(style.getFontIndexAsInt());
            boolean isbold = f.getBold();
            boolean isitalics = f.getItalic();

            int fontstyle = Font.PLAIN;

            if (isbold) {
                fontstyle = Font.BOLD;
            }
            if (isitalics) {
                fontstyle = fontstyle | Font.ITALIC;
            }

            int fontheight = f.getFontHeightInPoints();
            if (fontheight == 9) {
                fontheight = 10; //fix for stupid ol Windows
            }

            Font font = new Font(f.getFontName(), fontstyle, fontheight);
            editor.setFont(font);

            if (style.getFillPattern() == FillPatternType.SOLID_FOREGROUND) {
                editor.setBackground(getAWTColor(style.getFillForegroundColor(), white));
            } else {
                editor.setBackground(white);
            }

            editor.setForeground(getAWTColor(f.getColor(), black));


            //Set the value that is rendered for the cell
            switch (cell.getCellType()) {
                case BLANK:
                    editor.setText("");
                    break;
                case BOOLEAN:
                    if (cell.getBooleanCellValue()) {
                        editor.setText("true");
                    } else {
                        editor.setText("false");
                    }
                    break;
                case NUMERIC:
                    editor.setText(Double.toString(cell.getNumericCellValue()));
                    break;
                case STRING:
                    editor.setText(cell.getRichStringCellValue().getString());
                    break;
                case FORMULA:
                default:
                    editor.setText("?");
            }
            switch (style.getAlignment()) {
                case LEFT:
                case JUSTIFY:
                case FILL:
                    editor.setHorizontalAlignment(SwingConstants.LEFT);
                    break;
                case CENTER:
                case CENTER_SELECTION:
                    editor.setHorizontalAlignment(SwingConstants.CENTER);
                    break;
                case GENERAL:
                case RIGHT:
                    editor.setHorizontalAlignment(SwingConstants.RIGHT);
                    break;
                default:
                    editor.setHorizontalAlignment(SwingConstants.LEFT);
                    break;
            }

        }
        return editor;
    }
}
