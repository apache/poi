
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * This class presents the sheets to the user.
 *
 *
 * @author Andrew C. Oliver
 * @author Jason Height
 */
public class SViewerPanel extends JPanel {
  /** This field is the magic number to convert from a Character width to a
   *  java pixel width.
   *
   * When the "normal" font size in a workbook changes, this effects all
   * of the heights and widths. Unfortunately there is no way to retrieve this
   * information, hence the MAGIC number.
   *
   * This number may only work for the normal style font size of Arial size 10.
   *
   */
  private static final int magicCharFactor = 7;
  /** Reference to the woorkbook that is being displayed*/
  private HSSFWorkbook wb;
  /** Reference to the tabs component*/
  private JTabbedPane sheetPane;
  /** Reference to the cell renderer that is used to render all cells*/
  private SVTableCellRenderer cellRenderer;
  /** Reference to the cell editor that is used to edit all cells.
   *  Only constructed if editing is allowed
   */
  private SVTableCellEditor cellEditor;
  /** Flag indicating if editing is allowed. Otherwise the viewer is in
   *  view only mode.
   */
  private boolean allowEdits;

  /**Construct the representation of the workbook*/
  public SViewerPanel(HSSFWorkbook wb, boolean allowEdits) {
    this.wb = wb;
    this.allowEdits = allowEdits;

    initialiseGui();
  }

  private void initialiseGui() {
    cellRenderer = new SVTableCellRenderer(this.wb);
    if (allowEdits)
      cellEditor = new SVTableCellEditor(this.wb);

    //Initialise the Panel
    sheetPane = new JTabbedPane(JTabbedPane.BOTTOM);

    if (allowEdits)
      sheetPane.addMouseListener(createTabListener());
    int sheetCount = wb.getNumberOfSheets();
    for (int i=0; i<sheetCount;i++) {
      String sheetName = wb.getSheetName(i);
      //Add the new sheet to the tabbed pane
      sheetPane.addTab(sheetName, makeSheetView(wb.getSheetAt(i)));
    }
    setLayout(new BorderLayout());
    add(sheetPane, BorderLayout.CENTER);
  }

  protected JComponent makeSheetView(HSSFSheet sheet) {
    JTable sheetView = new JTable(new SVTableModel(sheet));
    sheetView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    sheetView.setDefaultRenderer(HSSFCell.class, cellRenderer);
    if (allowEdits)
      sheetView.setDefaultEditor(HSSFCell.class, cellEditor);
    JTableHeader header = sheetView.getTableHeader();
    //Dont allow column reordering
    header.setReorderingAllowed(false);
    //Only allow column resizing if editing is allowed
    header.setResizingAllowed(allowEdits);

    //Set the columns the correct size
    TableColumnModel columns = sheetView.getColumnModel();
    for (int i=0; i< columns.getColumnCount(); i++) {
      TableColumn column = columns.getColumn(i);
      short width = sheet.getColumnWidth((short)i);
      //256 is because the width is in 256ths of a character
      column.setPreferredWidth(width/256*magicCharFactor);
    }

    //Set the rows to the correct size
    int rows = sheet.getPhysicalNumberOfRows();
    Insets insets = cellRenderer.getInsets();
    //Need to include the insets in the calculation of the row height to use.
    int extraHeight = insets.bottom+insets.top;
    for (int i=0; i< rows; i++) {
      HSSFRow row = sheet.getRow(i);
      if (row == null) {
        sheetView.setRowHeight(i, (int)sheet.getDefaultRowHeightInPoints()+extraHeight);
      } else {
        sheetView.setRowHeight(i, (int)row.getHeightInPoints()+extraHeight);
      }
    }

    //Add the row header to the sheet
    SVRowHeader rowHeader = new SVRowHeader(sheet, sheetView, extraHeight);
    JScrollPane scroll = new JScrollPane( sheetView );
    scroll.setRowHeaderView(rowHeader);
    return scroll;
  }

  public void paint(Graphics g) {
    //JMH I am only overriding this to get a picture of the time taken to paint
    long start = System.currentTimeMillis();
    super.paint(g);
    long elapsed = System.currentTimeMillis()-start;
    System.out.println("Paint time = "+elapsed);
  }

  protected MouseListener createTabListener() {
    return new TabListener();
  }

  /** This class defines the default MouseListener that listens to
   *  mouse events in the tabbed pane
   *
   *  The default is to popup a menu when the event occurs over a tab
   */
  private class TabListener implements MouseListener {
    public JPopupMenu popup;
    public TabListener() {
      popup = new JPopupMenu("Sheet");
      popup.add(createInsertSheetAction());
      popup.add(createDeleteSheetAction());
      popup.add(createRenameSheetAction());
    }

    protected Action createInsertSheetAction() {
      return new InsertSheetAction();
    }

    protected Action createDeleteSheetAction() {
      return new DeleteSheetAction();
    }

    protected Action createRenameSheetAction() {
      return new RenameSheetAction();
    }


    /** This method will display the popup if the mouseevent is a popup event
     *  and the event occurred over a tab
     */
    protected void checkPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        int tab = sheetPane.getUI().tabForCoordinate(sheetPane, e.getX(), e.getY());
        if (tab != -1) {
          popup.show(sheetPane, e.getX(), e.getY());
        }
      }
    }

    public void mouseClicked(MouseEvent e) {
      checkPopup(e);
    }

    public void mousePressed(MouseEvent e) {
      checkPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
      checkPopup(e);
    }

    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
  }

  /** This class defines the action that is performed when the sheet is renamed*/
  private class RenameSheetAction extends AbstractAction {
    public RenameSheetAction() {
      super("Rename");
    }

    public void actionPerformed(ActionEvent e) {
      int tabIndex = sheetPane.getSelectedIndex();
      if (tabIndex != -1) {
        String newSheetName = (String)JOptionPane.showInputDialog(sheetPane, "Enter a new Sheetname", "Rename Sheet", JOptionPane.QUESTION_MESSAGE);
        if (newSheetName != null) {
          wb.setSheetName(tabIndex, newSheetName);
          sheetPane.setTitleAt(tabIndex, newSheetName);
        }
      }
    }
  }

  /** This class defines the action that is performed when a sheet is inserted*/
  private class InsertSheetAction extends AbstractAction {
    public InsertSheetAction() {
      super("Insert");
    }

    public void actionPerformed(ActionEvent e) {
      //Create a new sheet then search for the sheet and make sure that the
      //sheetPane shows it.
      HSSFSheet newSheet = wb.createSheet();
      for (int i=0; i<wb.getNumberOfSheets();i++) {
        HSSFSheet sheet = wb.getSheetAt(i);
        if (newSheet == sheet) {
          sheetPane.insertTab(wb.getSheetName(i), null, makeSheetView(sheet), null, i);
        }
      }
    }
  }

  /** This class defines the action that is performed when the sheet is deleted*/
  private class DeleteSheetAction extends AbstractAction {
    public DeleteSheetAction() {
      super("Delete");
    }

    public void actionPerformed(ActionEvent e) {
      int tabIndex = sheetPane.getSelectedIndex();
      if (tabIndex != -1) {
        if (JOptionPane.showConfirmDialog(sheetPane, "Are you sure that you want to delete the selected sheet", "Delete Sheet?", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
          wb.removeSheetAt(tabIndex);
          sheetPane.remove(tabIndex);
        }
      }
    }
  }

  public boolean isEditable() {
    return allowEdits;
  }

  /**Main method*/
  public static void main(String[] args) {
    try {
      FileInputStream in = new FileInputStream(args[0]);
      HSSFWorkbook wb = new HSSFWorkbook(in);
      in.close();

      SViewerPanel p = new SViewerPanel(wb, true);
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
      frame.setSize(800,640);
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
      frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
      frame.setVisible(true);
    } catch (IOException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }
}
