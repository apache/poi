
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

import java.util.Hashtable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.border.*;

import java.awt.Component;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Font;

import java.io.Serializable;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFColor.WHITE;


/**
 * Sucky Viewer Table Cell Render -- not commented via javadoc as it
 * nearly completely consists of overridden methods.
 *
 * @author Andrew C. Oliver
 */
public class SVTableCellRenderer extends JLabel
    implements TableCellRenderer, Serializable
{

    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    private Color unselFG;
    private Color unselBG;
    private HSSFWorkbook wb = null;
    private HSSFSheet    st = null;
    private Hashtable colors = HSSFColor.getIndexHash();

    public SVTableCellRenderer(HSSFWorkbook wb, HSSFSheet st) {
	super();
	setOpaque(true);
        setBorder(noFocusBorder);
        this.wb = wb;
        this.st = st;
    }

    public void setForeground(Color c) {
        super.setForeground(c);
        unselFG = c;
    }

    public void setBackground(Color c) {
        super.setBackground(c);
        unselBG = c;
    }

    public void updateUI() {
        super.updateUI();
	setForeground(null);
	setBackground(null);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                          boolean isSelected, boolean hasFocus, int row, int column) {

	boolean isBorderSet = false;

	if (isSelected) {
	   super.setForeground(table.getSelectionForeground());
	   super.setBackground(table.getSelectionBackground());
	}

        HSSFCell c = getCell(row,column);

        if (c != null) {

          HSSFCellStyle s = c.getCellStyle();
          HSSFFont f = wb.getFontAt(s.getFontIndex());
          boolean isbold = f.getBoldweight() > HSSFFont.BOLDWEIGHT_NORMAL;
          boolean isitalics = f.getItalic();

          int fontstyle = 0;

          if (isbold) fontstyle = Font.BOLD;
          if (isitalics) fontstyle = fontstyle | Font.ITALIC;

          int fontheight = f.getFontHeightInPoints();
          if (fontheight == 9) fontheight = 10; //fix for stupid ol Windows

          Font font = new Font(f.getFontName(),fontstyle,fontheight);
          setFont(font);
          

          HSSFColor clr = null;
          if (s.getFillPattern() == HSSFCellStyle.SOLID_FOREGROUND) {
            clr = (HSSFColor)colors.get(new Integer(s.getFillForegroundColor()));
          }
          if (clr == null) clr = new HSSFColor.WHITE();

          short[] rgb = clr.getTriplet();
          Color awtcolor = new Color(rgb[0],rgb[1],rgb[2]);

          setBackground(awtcolor);

          clr = (HSSFColor)colors.get(new Integer(f.getColor()));
          if (clr == null) clr = new HSSFColor.BLACK();
          rgb = clr.getTriplet();
          awtcolor = new Color(rgb[0],rgb[1],rgb[2]);
          setForeground(awtcolor);

/*          if (s.getBorderBottom() != HSSFCellStyle.BORDER_NONE ||
              s.getBorderTop()    != HSSFCellStyle.BORDER_NONE ||
              s.getBorderLeft()   != HSSFCellStyle.BORDER_NONE ||
              s.getBorderRight()  != HSSFCellStyle.BORDER_NONE) {
*/
              int borderTop = s.getBorderTop();
              int borderRight = s.getBorderRight();
              int borderBottom = s.getBorderBottom();
              int borderLeft = s.getBorderLeft();
              
              SVBorder border = new SVBorder(Color.black, Color.black,
                                           Color.black, Color.black,
                                           borderTop, borderRight,
                                           borderBottom, borderLeft,
                                           s.getBorderTop() != HSSFCellStyle.BORDER_NONE,
                                           s.getBorderRight() != HSSFCellStyle.BORDER_NONE,
                                           s.getBorderBottom() != HSSFCellStyle.BORDER_NONE,
                                           s.getBorderLeft() != HSSFCellStyle.BORDER_NONE);
              setBorder(border);
              isBorderSet=true;

//          }
        } else {
          setBackground(Color.white);
        }


	if (hasFocus) {
            if (!isBorderSet) {
	        setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
            }
	    if (table.isCellEditable(row, column)) {
	        super.setForeground( UIManager.getColor("Table.focusCellForeground") );
	        super.setBackground( UIManager.getColor("Table.focusCellBackground") );
	    }
	} else if (!isBorderSet) {
	    setBorder(noFocusBorder);
	}

        if (c != null) {
          switch (c.getCellType()) {
            case HSSFCell.CELL_TYPE_BLANK:
              setValue("");
            break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
              if (c.getBooleanCellValue()) {
                setValue("true");
              } else {
                setValue("false");
              }
            break;
            case HSSFCell.CELL_TYPE_FORMULA:
            case HSSFCell.CELL_TYPE_NUMERIC:
              setValue(""+c.getNumericCellValue());
            break;
            case HSSFCell.CELL_TYPE_STRING:
              setValue(c.getStringCellValue());
            break;
            default:
              setValue("?");
          }
       } else {
           setValue("");
       }


	// ---- begin optimization to avoid painting background ----
	Color back = getBackground();
	boolean colorMatch = (back != null) && ( back.equals(table.getBackground()) ) && table.isOpaque();
        setOpaque(!colorMatch);
	// ---- end optimization to aviod painting background ----

	return this;
    }


    public void validate() {}

    public void revalidate() {}

    public void repaint(long tm, int x, int y, int width, int height) {}

    public void repaint(Rectangle r) { }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	// Strings get interned...
	if (propertyName=="text") {
	    super.firePropertyChange(propertyName, oldValue, newValue);
	}
    }

    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }


    /**
     * Sets the string to either the value or "" if the value is null.
     *
     */
    protected void setValue(Object value) {
	setText((value == null) ? "" : value.toString());
    }

    /**
     * Get a cell at a given row  (warning: slow)
     *
     */
    private HSSFCell getCell(int row, int col) {
      HSSFRow r = st.getRow(row);
      HSSFCell c = null;
      if ( r != null)
       c = r.getCell((short)col);
      return c;
    }
}
