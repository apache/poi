
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

import java.util.Iterator;
import javax.swing.table.*;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFCell;

/**
 * Sheet Viewer Table Model - The model for the Sheet Viewer just overrides things.
 * @author Andrew C. Oliver
 */

public class SVTableModel extends AbstractTableModel {
  private HSSFSheet st = null;
  int maxcol = 0;

  public SVTableModel(HSSFSheet st, int maxcol) {
    this.st = st;
    this.maxcol=maxcol;
  }

  public SVTableModel(HSSFSheet st) {
    this.st = st;
    Iterator i = st.rowIterator();

    while (i.hasNext()) {
      HSSFRow row = (HSSFRow)i.next();
      if (maxcol < (row.getLastCellNum()+1)) {
         this.maxcol = row.getLastCellNum();
      }
    }
  }


  public int getColumnCount() {
    return this.maxcol+1;
  }
  public Object getValueAt(int row, int col) {
    HSSFRow r = st.getRow(row);
    HSSFCell c = null;
    if (r != null) {
      c = r.getCell(col);
    }
    return c;
  }
  public int getRowCount() {
    return st.getLastRowNum() + 1;
  }

  public Class getColumnClass(int c) {
	return HSSFCell.class;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return true;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (aValue != null)
      System.out.println("SVTableModel.setValueAt. value type = "+aValue.getClass().getName());
    else System.out.println("SVTableModel.setValueAt. value type = null");
  }


}
