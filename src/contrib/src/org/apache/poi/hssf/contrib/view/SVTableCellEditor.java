
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

package org.apache.poi.hssf.contrib.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * Sheet Viewer Table Cell Editor -- not commented via javadoc as it
 * nearly completely consists of overridden methods.
 *
 * @author     Jason Height
 */
public class SVTableCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
  private static final Color black = getAWTColor(new HSSFColor.BLACK());
  private static final Color white = getAWTColor(new HSSFColor.WHITE());
  private Hashtable colors = HSSFColor.getIndexHash();


  private HSSFWorkbook wb;
  private JTextField editor;

  private HSSFCell editorValue;


  public SVTableCellEditor(HSSFWorkbook wb) {
    this.wb = wb;
    this.editor = new JTextField();
  }


  /**
   *  Gets the cellEditable attribute of the SVTableCellEditor object
   *
   * @return    The cellEditable value
   */
  public boolean isCellEditable(java.util.EventObject e) {
    if (e instanceof MouseEvent) {
      return ((MouseEvent) e).getClickCount() >= 2;
    }
    return false;
  }


  public boolean shouldSelectCell(EventObject anEvent) {
    return true;
  }


  public boolean startCellEditing(EventObject anEvent) {
    System.out.println("Start Cell Editing");
    return true;
  }


  public boolean stopCellEditing() {
    System.out.println("Stop Cell Editing");
    fireEditingStopped();
    return true;
  }


  public void cancelCellEditing() {
    System.out.println("Cancel Cell Editing");
    fireEditingCanceled();
  }


  public void actionPerformed(ActionEvent e) {
    System.out.println("Action performed");
    stopCellEditing();
  }


  /**
   *  Gets the cellEditorValue attribute of the SVTableCellEditor object
   *
   * @return    The cellEditorValue value
   */
  public Object getCellEditorValue() {
    System.out.println("GetCellEditorValue");
    //JMH Look at when this method is called. Should it return a HSSFCell?
    return editor.getText();
  }


  /**
   *  Gets the tableCellEditorComponent attribute of the SVTableCellEditor object
   *
   * @return             The tableCellEditorComponent value
   */
  public Component getTableCellEditorComponent(JTable table, Object value,
      boolean isSelected,
      int row,
      int column) {
    System.out.println("GetTableCellEditorComponent");
    HSSFCell cell = (HSSFCell) value;
    if (cell != null) {
          HSSFCellStyle style = cell.getCellStyle();
          HSSFFont f = wb.getFontAt(style.getFontIndex());
          boolean isbold = f.getBoldweight() > HSSFFont.BOLDWEIGHT_NORMAL;
          boolean isitalics = f.getItalic();

          int fontstyle = Font.PLAIN;

          if (isbold) fontstyle = Font.BOLD;
          if (isitalics) fontstyle = fontstyle | Font.ITALIC;

          int fontheight = f.getFontHeightInPoints();
          if (fontheight == 9) fontheight = 10; //fix for stupid ol Windows

          Font font = new Font(f.getFontName(),fontstyle,fontheight);
          editor.setFont(font);

          if (style.getFillPattern() == HSSFCellStyle.SOLID_FOREGROUND) {
            editor.setBackground(getAWTColor(style.getFillForegroundColor(), white));
          } else editor.setBackground(white);

          editor.setForeground(getAWTColor(f.getColor(), black));


      //Set the value that is rendered for the cell
      switch (cell.getCellType()) {
        case HSSFCell.CELL_TYPE_BLANK:
          editor.setText("");
          break;
        case HSSFCell.CELL_TYPE_BOOLEAN:
          if (cell.getBooleanCellValue()) {
            editor.setText("true");
          } else {
            editor.setText("false");
          }
          break;
        case HSSFCell.CELL_TYPE_NUMERIC:
          editor.setText(Double.toString(cell.getNumericCellValue()));
          break;
        case HSSFCell.CELL_TYPE_STRING:
          editor.setText(cell.getRichStringCellValue().getString());
          break;
        case HSSFCell.CELL_TYPE_FORMULA:
        default:
          editor.setText("?");
      }
      switch (style.getAlignment()) {
        case HSSFCellStyle.ALIGN_LEFT:
        case HSSFCellStyle.ALIGN_JUSTIFY:
        case HSSFCellStyle.ALIGN_FILL:
          editor.setHorizontalAlignment(SwingConstants.LEFT);
          break;
        case HSSFCellStyle.ALIGN_CENTER:
        case HSSFCellStyle.ALIGN_CENTER_SELECTION:
          editor.setHorizontalAlignment(SwingConstants.CENTER);
          break;
        case HSSFCellStyle.ALIGN_GENERAL:
        case HSSFCellStyle.ALIGN_RIGHT:
          editor.setHorizontalAlignment(SwingConstants.RIGHT);
          break;
        default:
          editor.setHorizontalAlignment(SwingConstants.LEFT);
          break;
      }

    }
    return editor;
  }

    /** This method retrieves the AWT Color representation from the colour hash table
     *
     */
    private final Color getAWTColor(int index, Color deflt) {
      HSSFColor clr = (HSSFColor)colors.get(Integer.valueOf(index));
      if (clr == null) return deflt;
      return getAWTColor(clr);
    }

    private static final Color getAWTColor(HSSFColor clr) {
      short[] rgb = clr.getTriplet();
      return new Color(rgb[0],rgb[1],rgb[2]);
    }
}
