/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2003 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache POI" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache POI", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package org.apache.poi.hssf.contrib.view;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * Sheet Viewer Table Cell Editor -- not commented via javadoc as it
 * nearly completely consists of overridden methods.
 *
 * @author     Jason Height
 * @since      16 July 2002
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
          editor.setText(cell.getStringCellValue());
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
      HSSFColor clr = (HSSFColor)colors.get(new Integer(index));
      if (clr == null) return deflt;
      return getAWTColor(clr);
    }

    private static final Color getAWTColor(HSSFColor clr) {
      short[] rgb = clr.getTriplet();
      return new Color(rgb[0],rgb[1],rgb[2]);
    }

}
