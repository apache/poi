/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.poi.hssf.contrib.view;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

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

    public int getSize() { return sheet.getPhysicalNumberOfRows(); }
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
      int rowHeight = (int)sheet.getRow(index).getHeightInPoints();
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
