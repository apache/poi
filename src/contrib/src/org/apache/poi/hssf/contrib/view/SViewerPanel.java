
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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFCell;

/**
 * This class presents the sheets to the user.
 *
 *
 * @author Andrew C. Oliver
 * @author Jason Height
 */
public class SViewerPanel extends JPanel {
  private HSSFWorkbook wb;
  private JTable sheets[];

  /**Construct the representation of the workbook*/
  public SViewerPanel(HSSFWorkbook wb) {
    this.wb = wb;

    //Initialise the Panel
    JTabbedPane sheetPane = new JTabbedPane(JTabbedPane.BOTTOM);
    int sheetCount = wb.getNumberOfSheets();
    sheets = new JTable[sheetCount];
    SVTableCellRenderer rnd = new SVTableCellRenderer(wb);
    for (int i=0; i<sheetCount;i++) {
      String sheetName = wb.getSheetName(i);
      //Construct the view of the sheet
      SVTableModel tm = new SVTableModel(wb.getSheetAt(i));
      sheets[i] = new JTable(tm);
      sheets[i].setDefaultRenderer(HSSFCell.class, rnd);
      //Add the new sheet to the tabbed pane
      sheetPane.addTab(sheetName, new JScrollPane(sheets[i]));
    }
    setLayout(new BorderLayout());
    add(sheetPane, BorderLayout.CENTER);
  }

  public void paint(Graphics g) {
    long start = System.currentTimeMillis();
    super.paint(g);
    long elapsed = System.currentTimeMillis()-start;
    System.out.println("Paint time = "+elapsed);
  }

  /**Main method*/
  public static void main(String[] args) {
    try {
      FileInputStream in = new FileInputStream(args[0]);
      HSSFWorkbook wb = new HSSFWorkbook(in);
      in.close();

      SViewerPanel p = new SViewerPanel(wb);
      JFrame frame;
      frame = new JFrame() {
        protected void processWindowEvent(WindowEvent e) {
          super.processWindowEvent(e);
          if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            System.exit(0);
          }
        }
        public synchronized void setTitle(String title) {
          super.setTitle(title);
          enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        }
      };
      frame.setTitle("Viewer Frame");
      frame.getContentPane().add(p, BorderLayout.CENTER);
      frame.setSize(400,320);
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
      frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
      frame.setVisible(true);
    } catch (IOException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }
}
