package org.apache.poi.hssf.contrib.view;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Component;

import javax.swing.border.AbstractBorder;

/**
 * This is an attempt to implement Excel style borders for the SuckyViewer
 *
 */
public class SVBorder extends AbstractBorder {
   Color northColor = null;
   Color eastColor = null;
   Color southColor = null; 
   Color westColor = null;
   int northThickness;
   int eastThickness;
   int southThickness;
   int westThickness;
   boolean northBorder=false;
   boolean eastBorder=false;
   boolean southBorder=false;
   boolean westBorder=false;

   public SVBorder(Color northColor, Color eastColor, 
                   Color southColor, Color westColor,
                   int northThickness, int eastThickness,
                   int southThickness, int westThickness, 
                   boolean northBorder, boolean eastBorder,
                   boolean southBorder, boolean westBorder) {
     this.northColor = northColor;
     this.eastColor = eastColor;
     this.southColor = southColor;
     this.westColor = westColor;
     this.northThickness = northThickness;
     this.eastThickness = eastThickness;
     this.southThickness = southThickness;
     this.westThickness = westThickness; 
     this.northBorder=northBorder;
     this.eastBorder=eastBorder;
     this.southBorder=southBorder;
     this.westBorder=westBorder;
   }

   public void paintBorder(Component c, Graphics g, int x, int y, int width,
                           int height) {
      Color oldColor = g.getColor();
      int i;
 
      if (northBorder) {
        System.out.println("NorthBorder x="+x+",y="+y+"x1="+width+"y1="+y);
      	g.setColor(northColor); 

        for (int k=0; k < northThickness; k++) {
           g.drawLine(x,y+k,width,y+k);
        }
      }


      if (eastBorder) {
        System.out.println("EastBorder x="+x+",y="+y+"x1="+width+"y1="+y);
      	g.setColor(eastColor); 

        for (int k=0; k < eastThickness; k++) {
           g.drawLine(width-k,y,width-k,height);
        }
      }

      if (southBorder) {
        System.out.println("SouthBorder x="+x+",y="+height+"x1="+width+"y1="+height);
      	g.setColor(southColor); 
        for (int k=0; k < southThickness; k++) {
           g.drawLine(x,height - k,width,height - k);
        }
      }

      if (westBorder) {
        System.out.println("WestBorder x="+x+",y="+y+"x1="+width+"y1="+y);
      	g.setColor(westColor); 

        for (int k=0; k < westThickness; k++) {
           g.drawLine(x+k,y,x+k,height);
        }
      }

     g.setColor(oldColor);    
   }


}
