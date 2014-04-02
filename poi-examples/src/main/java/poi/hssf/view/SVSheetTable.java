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

import org.apache.poi.hssf.view.brush.PendingPaintings;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

/**
 * This class is a table that represents the values in a single worksheet.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public class SVSheetTable extends JTable {
  private final HSSFSheet sheet;
  private final PendingPaintings pendingPaintings;
  private FormulaDisplayListener formulaListener;
  private JScrollPane scroll;

  private static final Color HEADER_BACKGROUND = new Color(235, 235, 235);

  /**
   * This field is the magic number to convert from a Character width to a java
   * pixel width.
   * <p/>
   * When the "normal" font size in a workbook changes, this effects all of the
   * heights and widths. Unfortunately there is no way to retrieve this
   * information, hence the MAGIC number.
   * <p/>
   * This number may only work for the normal style font size of Arial size 10.
   */
  private static final int magicCharFactor = 7;

  private class HeaderCell extends JLabel {
    private final int row;

    public HeaderCell(Object value, int row) {
      super(value.toString(), CENTER);
      this.row = row;
      setBackground(HEADER_BACKGROUND);
      setOpaque(true);
      setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
      setRowSelectionAllowed(false);
    }

    @Override
    public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      if (row >= 0) {
        d.height = getRowHeight(row);
      }
      return d;
    }

    @Override
    public Dimension getMaximumSize() {
      Dimension d = super.getMaximumSize();
      if (row >= 0) {
        d.height = getRowHeight(row);
      }
      return d;
    }

    @Override
    public Dimension getMinimumSize() {
      Dimension d = super.getMinimumSize();
      if (row >= 0) {
        d.height = getRowHeight(row);
      }
      return d;
    }
  }

  private class HeaderCellRenderer implements TableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {

      return new HeaderCell(value, row);
    }
  }

  private class FormulaDisplayListener implements ListSelectionListener {
    private final JTextComponent formulaDisplay;

    public FormulaDisplayListener(JTextComponent formulaDisplay) {
      this.formulaDisplay = formulaDisplay;
    }

    public void valueChanged(ListSelectionEvent e) {
      int row = getSelectedRow();
      int col = getSelectedColumn();
      if (row < 0 || col < 0) {
        return;
      }

      if (e.getValueIsAdjusting()) {
        return;
      }

      HSSFCell cell = (HSSFCell) getValueAt(row, col);
      String formula = "";
      if (cell != null) {
        if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
          formula = cell.getCellFormula();
        } else {
          formula = cell.toString();
        }
        if (formula == null)
          formula = "";
      }
      formulaDisplay.setText(formula);
    }
  }

  public SVSheetTable(HSSFSheet sheet) {
    super(new SVTableModel(sheet));
    this.sheet = sheet;

    setIntercellSpacing(new Dimension(0, 0));
    setAutoResizeMode(AUTO_RESIZE_OFF);
    JTableHeader header = getTableHeader();
    header.setDefaultRenderer(new HeaderCellRenderer());
    pendingPaintings = new PendingPaintings(this);

    //Set the columns the correct size
    TableColumnModel columns = getColumnModel();
    for (int i = 0; i < columns.getColumnCount(); i++) {
      TableColumn column = columns.getColumn(i);
      int width = sheet.getColumnWidth(i);
      //256 is because the width is in 256ths of a character
      column.setPreferredWidth(width / 256 * magicCharFactor);
    }

    Toolkit t = getToolkit();
    int res = t.getScreenResolution();
    TableModel model = getModel();
    for (int i = 0; i < model.getRowCount(); i++) {
      Row row = sheet.getRow(i - sheet.getFirstRowNum());
      if (row != null) {
        short h = row.getHeight();
        int height = Math.round(Math.max(1, h / (res / 70 * 20) + 3));
        System.out.printf("%d: %d (%d @ %d)%n", i, height, h, res);
        setRowHeight(i, height);
      }
    }

    addHierarchyListener(new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
          Container changedParent = e.getChangedParent();
          if (changedParent instanceof JViewport) {
            Container grandparent = changedParent.getParent();
            if (grandparent instanceof JScrollPane) {
              JScrollPane jScrollPane = (JScrollPane) grandparent;
              setupScroll(jScrollPane);
            }
          }
        }
      }
    });
  }

  public void setupScroll(JScrollPane scroll) {
    if (scroll == this.scroll)
      return;

    this.scroll = scroll;
    if (scroll == null)
      return;

    SVRowHeader rowHeader = new SVRowHeader(sheet, this, 0);
    scroll.setRowHeaderView(rowHeader);
    scroll.setCorner(JScrollPane.UPPER_LEADING_CORNER, headerCell("?"));
  }

  public void setFormulaDisplay(JTextComponent formulaDisplay) {
    ListSelectionModel rowSelMod = getSelectionModel();
    ListSelectionModel colSelMod = getColumnModel().getSelectionModel();

    if (formulaDisplay == null) {
      rowSelMod.removeListSelectionListener(formulaListener);
      colSelMod.removeListSelectionListener(formulaListener);
      formulaListener = null;
    }

    if (formulaDisplay != null) {
      formulaListener = new FormulaDisplayListener(formulaDisplay);
      rowSelMod.addListSelectionListener(formulaListener);
      colSelMod.addListSelectionListener(formulaListener);
    }
  }

  public JTextComponent getFormulaDisplay() {
    if (formulaListener == null)
      return null;
    else
      return formulaListener.formulaDisplay;
  }

  public Component headerCell(String text) {
    return new HeaderCell(text, -1);
  }

  @Override
  public void paintComponent(Graphics g1) {
    Graphics2D g = (Graphics2D) g1;

    pendingPaintings.clear();

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    super.paintComponent(g);

    pendingPaintings.paint(g);
  }
}