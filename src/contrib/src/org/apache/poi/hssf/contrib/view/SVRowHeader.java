
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
import javax.swing.*;
import javax.swing.table.*;

import org.apache.poi.hssf.usermodel.*;

/**
 * This class presents the row header to the table.
 *
 *
 * @author Jason Height
 */
public class SVRowHeader extends JList {
  /** This model simply returns an integer number up to the number of rows
   *  that are present in the sheet.
   *
   */
  private class SVRowHeaderModel extends AbstractListModel {
    private HSSFSheet sheet;

    public SVRowHeaderModel(HSSFSheet sheet) {
      this.sheet = sheet;
    }

    public int getSize() {
    	return sheet.getLastRowNum() + 1;
    }
    public Object getElementAt(int index) {
      return Integer.toString(index+1);
    }
  }

  /** Renderes the row number*/
  private class RowHeaderRenderer extends JLabel implements ListCellRenderer {
    private HSSFSheet sheet;
    private int extraHeight;

    RowHeaderRenderer(HSSFSheet sheet, JTable table, int extraHeight) {
      this.sheet = sheet;
      this.extraHeight = extraHeight;
      JTableHeader header = table.getTableHeader();
      setOpaque(true);
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      setHorizontalAlignment(CENTER);
      setForeground(header.getForeground());
      setBackground(header.getBackground());
      setFont(header.getFont());
    }

    public Component getListCellRendererComponent( JList list,
           Object value, int index, boolean isSelected, boolean cellHasFocus) {
      Dimension d = getPreferredSize();
      HSSFRow row = sheet.getRow(index);
      int rowHeight;
      if(row == null) {
    	  rowHeight = (int)sheet.getDefaultRowHeightInPoints();
      } else {
    	  rowHeight = (int)row.getHeightInPoints();
      }
      d.height = rowHeight+extraHeight;
      setPreferredSize(d);
      setText((value == null) ? "" : value.toString());
      return this;
    }
  }

  public SVRowHeader(HSSFSheet sheet, JTable table, int extraHeight) {
    ListModel lm = new SVRowHeaderModel(sheet);
    this.setModel(lm);

    setFixedCellWidth(50);
    setCellRenderer(new RowHeaderRenderer(sheet, table, extraHeight));
  }
}
