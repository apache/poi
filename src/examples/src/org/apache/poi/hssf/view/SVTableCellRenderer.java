
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

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.border.*;

import java.awt.Component;
import java.awt.Color;
import java.awt.Rectangle;

import java.io.Serializable;
import java.text.*;

import org.apache.poi.hssf.usermodel.*;


/**
 * Sheet Viewer Table Cell Render -- not commented via javadoc as it
 * nearly completely consists of overridden methods.
 *
 * @author Andrew C. Oliver
 */
public class SVTableCellRenderer extends JLabel
    implements TableCellRenderer, Serializable
{
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    protected SVBorder cellBorder = new SVBorder();


    private HSSFWorkbook wb = null;

    /** This class holds the references to the predefined cell formats.
     */
    private class CellFormatter {
      private Format[] textFormatter;

      private DecimalFormat generalNumberFormat = new DecimalFormat("0");

      public CellFormatter() {
        textFormatter = new Format[0x31];

        textFormatter[0x01] = new DecimalFormat("0");
        textFormatter[0x02] = new DecimalFormat("0.00");
        textFormatter[0x03] = new DecimalFormat("#,##0");
        textFormatter[0x04] = new DecimalFormat("#,##0.00");
        textFormatter[0x05] = new DecimalFormat("$#,##0;$#,##0");
        textFormatter[0x06] = new DecimalFormat("$#,##0;$#,##0");
        textFormatter[0x07] = new DecimalFormat("$#,##0.00;$#,##0.00");
        textFormatter[0x08] = new DecimalFormat("$#,##0.00;$#,##0.00");
        textFormatter[0x09] = new DecimalFormat("0%");
        textFormatter[0x0A] = new DecimalFormat("0.00%");
        textFormatter[0x0B] = new DecimalFormat("0.00E0");
        textFormatter[0x0C] = new SVFractionalFormat("# ?/?");
        textFormatter[0x0D] = new SVFractionalFormat("# ??/??");
        textFormatter[0x0E] = new SimpleDateFormat("M/d/yy");
        textFormatter[0x0F] = new SimpleDateFormat("d-MMM-yy");
        textFormatter[0x10] = new SimpleDateFormat("d-MMM");
        textFormatter[0x11] = new SimpleDateFormat("MMM-yy");
        textFormatter[0x12] = new SimpleDateFormat("h:mm a");
        textFormatter[0x13] = new SimpleDateFormat("h:mm:ss a");
        textFormatter[0x14] = new SimpleDateFormat("h:mm");
        textFormatter[0x15] = new SimpleDateFormat("h:mm:ss");
        textFormatter[0x16] = new SimpleDateFormat("M/d/yy h:mm");
        // 0x17 - 0x24 reserved for international and undocumented 0x25, "(#,##0_);(#,##0)"
        //start at 0x26
        //jmh need to do colour
        //"(#,##0_);[Red](#,##0)"
        textFormatter[0x26] = new DecimalFormat("#,##0;#,##0");
        //jmh need to do colour
        //(#,##0.00_);(#,##0.00)
        textFormatter[0x27] = new DecimalFormat("#,##0.00;#,##0.00");
        textFormatter[0x28] = new DecimalFormat("#,##0.00;#,##0.00");
//??        textFormatter[0x29] = new DecimalFormat("_(*#,##0_);_(*(#,##0);_(* \"-\"_);_(@_)");
//??        textFormatter[0x2A] = new DecimalFormat("_($*#,##0_);_($*(#,##0);_($* \"-\"_);_(@_)");
//??        textFormatter[0x2B] = new DecimalFormat("_(*#,##0.00_);_(*(#,##0.00);_(*\"-\"??_);_(@_)");
//??        textFormatter[0x2C] = new DecimalFormat("_($*#,##0.00_);_($*(#,##0.00);_($*\"-\"??_);_(@_)");
        textFormatter[0x2D] = new SimpleDateFormat("mm:ss");
//??        textFormatter[0x2E] = new SimpleDateFormat("[h]:mm:ss");
        textFormatter[0x2F] = new SimpleDateFormat("mm:ss.0");
        textFormatter[0x30] = new DecimalFormat("##0.0E0");
      }

      public String format(short index, Object value) {
        if (index == 0)
          return value.toString();
        if (textFormatter[index] == null)
          throw new RuntimeException("Sorry. I cant handle the format code :"+Integer.toHexString(index));
        return textFormatter[index].format(value);
      }

      public String format(short index, double value) {
        if ( index <= 0 )
          return generalNumberFormat.format(value);
        if (textFormatter[index] == null)
          throw new RuntimeException("Sorry. I cant handle the format code :"+Integer.toHexString(index));
        if (textFormatter[index] instanceof DecimalFormat) {
          return ((DecimalFormat)textFormatter[index]).format(value);
        }
        if (textFormatter[index] instanceof SVFractionalFormat) {
          return ((SVFractionalFormat)textFormatter[index]).format(value);
        }
        throw new RuntimeException("Sorry. I cant handle a non decimal formatter for a decimal value :"+Integer.toHexString(index));
      }

      public boolean useRedColor(short index, double value) {
        return (((index == 0x06)||(index == 0x08)||(index == 0x26) || (index == 0x27)) && (value < 0));
      }
    }

    private final CellFormatter cellFormatter = new CellFormatter();

    public SVTableCellRenderer(HSSFWorkbook wb) {
	super();
	setOpaque(true);
        setBorder(noFocusBorder);
        this.wb = wb;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                          boolean isSelected, boolean hasFocus, int row, int column) {
	boolean isBorderSet = false;

        //If the JTables default cell renderer has been setup correctly the
        //value will be the HSSFCell that we are trying to render
        HSSFCell c = (HSSFCell)value;

        if (c != null) {
          HSSFCellStyle s = c.getCellStyle();
          HSSFFont f = wb.getFontAt(s.getFontIndex());
          setFont(SVTableUtils.makeFont(f));

          if (s.getFillPattern() == HSSFCellStyle.SOLID_FOREGROUND) {
            setBackground(SVTableUtils.getAWTColor(s.getFillForegroundColor(), SVTableUtils.white));
          } else setBackground(SVTableUtils.white);

          setForeground(SVTableUtils.getAWTColor(f.getColor(), SVTableUtils.black));

          cellBorder.setBorder(SVTableUtils.getAWTColor(s.getTopBorderColor(), SVTableUtils.black),
                               SVTableUtils.getAWTColor(s.getRightBorderColor(), SVTableUtils.black),
                               SVTableUtils.getAWTColor(s.getBottomBorderColor(), SVTableUtils.black),
                               SVTableUtils.getAWTColor(s.getLeftBorderColor(), SVTableUtils.black),
                               s.getBorderTop(), s.getBorderRight(),
                               s.getBorderBottom(), s.getBorderLeft(),
                               hasFocus);
            setBorder(cellBorder);
            isBorderSet=true;

            //Set the value that is rendered for the cell
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
              case HSSFCell.CELL_TYPE_NUMERIC:
                short format = s.getDataFormat();
                double numericValue = c.getNumericCellValue();
                if (cellFormatter.useRedColor(format, numericValue))
                  setForeground(Color.red);
                else setForeground(null);
                setValue(cellFormatter.format(format, c.getNumericCellValue()));
              break;
              case HSSFCell.CELL_TYPE_STRING:
                setValue(c.getRichStringCellValue().getString());
              break;
              case HSSFCell.CELL_TYPE_FORMULA:
              default:
                setValue("?");
            }
            //Set the text alignment of the cell
            switch (s.getAlignment()) {
              case HSSFCellStyle.ALIGN_LEFT:
              case HSSFCellStyle.ALIGN_JUSTIFY:
              case HSSFCellStyle.ALIGN_FILL:
                setHorizontalAlignment(SwingConstants.LEFT);
                break;
              case HSSFCellStyle.ALIGN_CENTER:
              case HSSFCellStyle.ALIGN_CENTER_SELECTION:
                setHorizontalAlignment(SwingConstants.CENTER);
                break;
              case HSSFCellStyle.ALIGN_GENERAL:
              case HSSFCellStyle.ALIGN_RIGHT:
                setHorizontalAlignment(SwingConstants.RIGHT);
                break;
              default:
                setHorizontalAlignment(SwingConstants.LEFT);
                break;
            }
        } else {
          setValue("");
          setBackground(SVTableUtils.white);
        }


	if (hasFocus) {
            if (!isBorderSet) {
              //This is the border to paint when there is no border
              //and the cell has focus
              cellBorder.setBorder(SVTableUtils.black,
                                   SVTableUtils.black,
                                   SVTableUtils.black,
                                   SVTableUtils.black,
                                   HSSFCellStyle.BORDER_NONE,
                                   HSSFCellStyle.BORDER_NONE,
                                   HSSFCellStyle.BORDER_NONE,
                                   HSSFCellStyle.BORDER_NONE,
                                   isSelected);
              setBorder(cellBorder);
            }
	    if (table.isCellEditable(row, column)) {
	        setForeground( UIManager.getColor("Table.focusCellForeground") );
	        setBackground( UIManager.getColor("Table.focusCellBackground") );
	    }
	} else if (!isBorderSet) {
	    setBorder(noFocusBorder);
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
}
