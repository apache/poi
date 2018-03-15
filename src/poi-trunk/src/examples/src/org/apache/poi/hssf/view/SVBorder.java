
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

import java.awt.*;

import javax.swing.border.AbstractBorder;

import org.apache.poi.ss.usermodel.BorderStyle;

/**
 * This is an attempt to implement Excel style borders for the SheetViewer.
 * Mostly just overrides stuff so the javadoc won't appear here but will 
 * appear in the generated stuff.
 * 
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height
 */
public class SVBorder extends AbstractBorder {
  private Color northColor;
  private Color eastColor;
  private Color southColor;
  private Color westColor;
  private BorderStyle northBorderType = BorderStyle.NONE;
  private BorderStyle eastBorderType = BorderStyle.NONE;
  private BorderStyle southBorderType = BorderStyle.NONE;
  private BorderStyle westBorderType = BorderStyle.NONE;
  private boolean northBorder;
  private boolean eastBorder;
  private boolean southBorder;
  private boolean westBorder;
  private boolean selected;

   public void setBorder(Color northColor, Color eastColor,
                         Color southColor, Color westColor,
                         BorderStyle northBorderType, BorderStyle eastBorderType,
                         BorderStyle southBorderType, BorderStyle westBorderType,
                         boolean selected) {
     this.eastColor = eastColor;
     this.southColor = southColor;
     this.westColor = westColor;
     this.northBorderType = northBorderType;
     this.eastBorderType = eastBorderType;
     this.southBorderType = southBorderType;
     this.westBorderType = westBorderType;
     this.northBorder=northBorderType != BorderStyle.NONE;
     this.eastBorder=eastBorderType != BorderStyle.NONE;
     this.southBorder=southBorderType != BorderStyle.NONE;
     this.westBorder=westBorderType != BorderStyle.NONE;
     this.selected = selected;
   }

   @Override
public void paintBorder(Component c, Graphics g, int x, int y, int width,
                           int height) {
      Color oldColor = g.getColor();


     paintSelectedBorder(g, x, y, width, height);
     paintNormalBorders(g, x, y, width, height);
     paintDottedBorders(g, x, y, width, height);
     paintDashedBorders(g, x, y, width, height);
     paintDoubleBorders(g, x, y, width, height);
     paintDashDotDotBorders(g, x, y, width, height);


     g.setColor(oldColor);
   }

   /**
    * Called by paintBorder to paint the border of a selected cell.
    * The paramaters are the Graphics object, location and dimensions of the 
    * cell.
    */
   private void paintSelectedBorder(Graphics g, int x, int y, int width,
                                  int height) {
     if (selected) {
       //Need to setup thickness of 2
       g.setColor(Color.black);
       //paint the border
       g.drawRect(x,y,width-1,height-1);

       //paint the filled rectangle at the bottom left hand position
       g.fillRect(x+width-5, y+height-5, 5, 5);
     }
   }


   /**
    * Called by paintBorder to paint the various versions of normal line
    * borders for a cell.  
    */
   private void paintNormalBorders(Graphics g, int x, int y, int width,
                                  int height) {

      if (northBorder &&
             ((northBorderType == BorderStyle.THIN) ||
              (northBorderType == BorderStyle.MEDIUM) ||
              (northBorderType == BorderStyle.THICK)
             )
         ) {

        int thickness = getThickness(northBorderType);

      	g.setColor(northColor);

        for (int k=0; k < thickness; k++) {
           g.drawLine(x,y+k,width,y+k);
        }
      }

      if (eastBorder &&
             ((eastBorderType == BorderStyle.THIN) ||
              (eastBorderType == BorderStyle.MEDIUM) ||
              (eastBorderType == BorderStyle.THICK)
             )
         ) {

        int thickness = getThickness(eastBorderType);

      	g.setColor(eastColor);

        for (int k=0; k < thickness; k++) {
           g.drawLine(width-k,y,width-k,height);
        }
      }

      if (southBorder &&
              ((southBorderType == BorderStyle.THIN) ||
               (southBorderType == BorderStyle.MEDIUM) ||
               (southBorderType == BorderStyle.THICK)
              )
         ) {

        int thickness = getThickness(southBorderType);

      	g.setColor(southColor);
        for (int k=0; k < thickness; k++) {
           g.drawLine(x,height - k,width,height - k);
        }
      }

      if (westBorder &&
             ((westBorderType == BorderStyle.THIN) ||
              (westBorderType == BorderStyle.MEDIUM) ||
              (westBorderType == BorderStyle.THICK)
             )
         ) {

        int thickness = getThickness(westBorderType);

      	g.setColor(westColor);

        for (int k=0; k < thickness; k++) {
           g.drawLine(x+k,y,x+k,height);
        }
      }
   }

   /**
    * Called by paintBorder to paint the dotted line
    * borders for a cell.  
    */
   private void paintDottedBorders(Graphics g, int x, int y, int width,
                                  int height) {
      if (northBorder &&
             northBorderType == BorderStyle.DOTTED) {
        int thickness = getThickness(northBorderType);

      	g.setColor(northColor);

        for (int k=0; k < thickness; k++) {
           for (int xc = x; xc < width; xc=xc+2) {
             g.drawLine(xc,y+k,xc,y+k);
           }
        }
      }

      if (eastBorder &&
              eastBorderType == BorderStyle.DOTTED
         ) {

        int thickness = getThickness(eastBorderType);
        thickness++; //need for dotted borders to show up east

      	g.setColor(eastColor);

        for (int k=0; k < thickness; k++) {
           for (int yc=y;yc < height; yc=yc+2) {
                g.drawLine(width-k,yc,width-k,yc);
           }
        }
      }

      if (southBorder &&
              southBorderType == BorderStyle.DOTTED
         ) {

        int thickness = getThickness(southBorderType);
        thickness++;
      	g.setColor(southColor);
        for (int k=0; k < thickness; k++) {
           for (int xc = x; xc < width; xc=xc+2) {
             g.drawLine(xc,height-k,xc,height-k);
           }
        }
      }

      if (westBorder &&
            westBorderType == BorderStyle.DOTTED
         ) {

        int thickness = getThickness(westBorderType);
//        thickness++;

      	g.setColor(westColor);

        for (int k=0; k < thickness; k++) {
           for (int yc=y;yc < height; yc=yc+2) {
                g.drawLine(x+k,yc,x+k,yc);
           }
        }
      }
   }

   /**
    * Called by paintBorder to paint the various versions of dotted line
    * borders for a cell.  
    */
   private void paintDashedBorders(Graphics g, int x, int y, int width,
                                  int height) {
      if (northBorder &&
             ((northBorderType == BorderStyle.DASHED) ||
              (northBorderType == BorderStyle.HAIR))
         ) {
        int thickness = getThickness(northBorderType);

        int dashlength = 1;

        if (northBorderType == BorderStyle.DASHED)
           dashlength = 2;

      	g.setColor(northColor);

        for (int k=0; k < thickness; k++) {
           for (int xc = x; xc < width; xc=xc+5) {
             g.drawLine(xc,y+k,xc+dashlength,y+k);
           }
        }
      }

      if (eastBorder &&
              ((eastBorderType == BorderStyle.DASHED) ||
               (eastBorderType == BorderStyle.HAIR))
         ) {

        int thickness = getThickness(eastBorderType);
        thickness++; //need for dotted borders to show up east


        int dashlength = 1;

        if (eastBorderType == BorderStyle.DASHED)
           dashlength = 2;

      	g.setColor(eastColor);

        for (int k=0; k < thickness; k++) {
           for (int yc=y;yc < height; yc=yc+5) {
                g.drawLine(width-k,yc,width-k,yc+dashlength);
           }
        }
      }

      if (southBorder &&
              ((southBorderType == BorderStyle.DASHED) ||
               (southBorderType == BorderStyle.HAIR))
         ) {

        int thickness = getThickness(southBorderType);
        thickness++;

        int dashlength = 1;

        if (southBorderType == BorderStyle.DASHED)
           dashlength = 2;

      	g.setColor(southColor);
        for (int k=0; k < thickness; k++) {
           for (int xc = x; xc < width; xc=xc+5) {
             g.drawLine(xc,height-k,xc+dashlength,height-k);
           }
        }
      }

      if (westBorder &&
            ((westBorderType == BorderStyle.DASHED) ||
             (westBorderType == BorderStyle.HAIR))
         ) {

        int thickness = getThickness(westBorderType);
//        thickness++;

        int dashlength = 1;

        if (westBorderType == BorderStyle.DASHED)
           dashlength = 2;

      	g.setColor(westColor);

        for (int k=0; k < thickness; k++) {
           for (int yc=y;yc < height; yc=yc+5) {
                g.drawLine(x+k,yc,x+k,yc+dashlength);
           }
        }
      }
   }

   /**
    * Called by paintBorder to paint the double line
    * borders for a cell.  
    */
   private void paintDoubleBorders(Graphics g, int x, int y, int width,
                                  int height) {
      if (northBorder &&
             northBorderType == BorderStyle.DOUBLE) {

      	g.setColor(northColor);

        int leftx=x;
        int rightx=width;

                // if there are borders on the west or east then
                // the second line shouldn't cross them
        if (westBorder)
           leftx = x+3;

        if (eastBorder)
           rightx = width-3;

           g.drawLine(x,y,width,y);
           g.drawLine(leftx,y+2,rightx,y+2);
      }

      if (eastBorder &&
              eastBorderType == BorderStyle.DOUBLE
         ) {

        int thickness = getThickness(eastBorderType);
        thickness++; //need for dotted borders to show up east

      	g.setColor(eastColor);

        int topy=y;
        int bottomy=height;

        if (northBorder)
          topy=y+3;

        if (southBorder)
            bottomy=height-3;

        g.drawLine(width-1,y,width-1,height);
        g.drawLine(width-3,topy,width-3,bottomy);
      }

      if (southBorder &&
              southBorderType == BorderStyle.DOUBLE
         ) {

      	g.setColor(southColor);

        int leftx=y;
        int rightx=width;

        if (westBorder)
           leftx=x+3;

        if (eastBorder)
           rightx=width-3;


        g.drawLine(x,height - 1,width,height - 1);
        g.drawLine(leftx,height - 3,rightx,height - 3);
      }

      if (westBorder &&
            westBorderType == BorderStyle.DOUBLE
         ) {

        int thickness = getThickness(westBorderType);
//        thickness++;

      	g.setColor(westColor);

        int topy=y;
        int bottomy=height-3;

        if (northBorder)
           topy=y+2;

        if (southBorder)
           bottomy=height-3;

        g.drawLine(x,y,x,height);
        g.drawLine(x+2,topy,x+2,bottomy);
      }
   }

   /**
    * Called by paintBorder to paint the various versions of dash dot dot line
    * borders for a cell.  
    */
   private void paintDashDotDotBorders(Graphics g, int x, int y, int width,
                                  int height) {
      if (northBorder &&
             ((northBorderType == BorderStyle.DASH_DOT_DOT) ||
              (northBorderType == BorderStyle.MEDIUM_DASH_DOT_DOT))
         ) {
        int thickness = getThickness(northBorderType);

      	g.setColor(northColor);
        for (int l=x; l < width;) {
          l=l+drawDashDotDot(g, l, y, thickness, true, true);
        }

      }

      if (eastBorder &&
              ((eastBorderType == BorderStyle.DASH_DOT_DOT) ||
               (eastBorderType == BorderStyle.MEDIUM_DASH_DOT_DOT))
         ) {

        int thickness = getThickness(eastBorderType);

      	g.setColor(eastColor);

        for (int l=y;l < height;) {
          //System.err.println("drawing east");
          l=l+drawDashDotDot(g,width-1,l,thickness,false,false);
        }
      }

      if (southBorder &&
              ((southBorderType == BorderStyle.DASH_DOT_DOT) ||
               (southBorderType == BorderStyle.MEDIUM_DASH_DOT_DOT))
         ) {

        int thickness = getThickness(southBorderType);

      	g.setColor(southColor);

        for (int l=x; l < width;) {
          //System.err.println("drawing south");
          l=l+drawDashDotDot(g, l, height-1, thickness, true, false);
        }
      }

      if (westBorder &&
            ((westBorderType == BorderStyle.DASH_DOT_DOT) ||
             (westBorderType == BorderStyle.MEDIUM_DASH_DOT_DOT))
         ) {

        int thickness = getThickness(westBorderType);

      	g.setColor(westColor);

        for (int l=y;l < height;) {
          //System.err.println("drawing west");
          l=l+drawDashDotDot(g,x,l,thickness,false,true);
        }

      }
   }

   /**
    *  Draws one dash dot dot horizontally or vertically with thickness drawn
    *  incrementally to either the right or left.
    *
    *  @param g graphics object for drawing with
    *  @param x the x origin of the line
    *  @param y the y origin of the line
    *  @param thickness the thickness of the line
    *  @param horizontal or vertical (true for horizontal)
    *  @param rightBottom or left/top thickness (true for right or top),
    *         if true then the x or y origin will be incremented to provide
    *         thickness, if false, they'll be decremented.  For vertical
    *         borders, x is incremented or decremented, for horizontal its y.
    *         Just set to true for north and west, and false for east and
    *         south.
    *  @return length - returns the length of the line.
    */
   private int drawDashDotDot(Graphics g,int x, int y, int thickness,
                              boolean horizontal,
                              boolean rightBottom) {

      for (int t=0; t < thickness; t++) {
         if (!rightBottom) {
            t = 0 - t; //add negative thickness so we go the other way
                       //then we'll decrement instead of increment.
         }
         if (horizontal) {
            g.drawLine(x,y+t,x+5,y+t);
            g.drawLine(x+8,y+t,x+10,y+t);
            g.drawLine(x+13,y+t,x+15,y+t);
         } else {
            g.drawLine(x+t,y,x+t,y+5);
            g.drawLine(x+t,y+8,x+t,y+10);
            g.drawLine(x+t,y+13,x+t,y+15);
         }
      }
      return 18;
   }

   /**
    * @return the line thickness for a border based on border type
    */
   private int getThickness(BorderStyle thickness) {
       switch (thickness) {
           case DASH_DOT_DOT:
           case DASHED:
           case HAIR:
             return 1;
           case THIN:
             return 2;
           case MEDIUM:
           case MEDIUM_DASH_DOT_DOT:
             return 3;
           case THICK:
             return 4;
           default:
             return 1;
       }
   }


}
