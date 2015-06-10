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

package org.apache.poi.hslf.model;

import org.apache.poi.ddf.*;
import org.apache.poi.hslf.usermodel.*;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;

import java.awt.geom.*;
import java.util.ArrayList;

/**
 * Represents a line in a PowerPoint drawing
 *
 *  @author Yegor Kozlov
 */
public final class Line extends HSLFSimpleShape {
    public Line(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape> parent){
        super(escherRecord, parent);
    }

    public Line(ShapeContainer<HSLFShape> parent){
        super(null, parent);
        _escherContainer = createSpContainer(parent instanceof HSLFGroupShape);
    }

    public Line(){
        this(null);
    }

    protected EscherContainerRecord createSpContainer(boolean isChild){
        _escherContainer = super.createSpContainer(isChild);

        EscherSpRecord spRecord = _escherContainer.getChildById(EscherSpRecord.RECORD_ID);
        short type = (short)((ShapeType.LINE.nativeId << 4) | 0x2);
        spRecord.setOptions(type);

        //set default properties for a line
        EscherOptRecord opt = getEscherOptRecord();

        //default line properties
        setEscherProperty(opt, EscherProperties.GEOMETRY__SHAPEPATH, 4);
        setEscherProperty(opt, EscherProperties.GEOMETRY__FILLOK, 0x10000);
        setEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST, 0x100000);
        setEscherProperty(opt, EscherProperties.LINESTYLE__COLOR, 0x8000001);
        setEscherProperty(opt, EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0xA0008);
        setEscherProperty(opt, EscherProperties.SHADOWSTYLE__COLOR, 0x8000002);

        return _escherContainer;
    }

    public java.awt.Shape getOutline(){
        Rectangle2D anchor = getLogicalAnchor2D();
        return new Line2D.Double(anchor.getX(), anchor.getY(), anchor.getX() + anchor.getWidth(), anchor.getY() + anchor.getHeight());
    }
    
    /**
    *
    * @return 'absolute' anchor of this shape relative to the parent sheet
    * 
    * @deprecated TODO: remove the whole class, should work with preset geometries instead
    */
   public Rectangle2D getLogicalAnchor2D(){
       Rectangle2D anchor = getAnchor2D();

       //if it is a groupped shape see if we need to transform the coordinates
       if (getParent() != null){
           ArrayList<HSLFGroupShape> lst = new ArrayList<HSLFGroupShape>();
           for (ShapeContainer<HSLFShape> parent=this.getParent();
               parent instanceof HSLFGroupShape;
               parent = ((HSLFGroupShape)parent).getParent()) {
               lst.add(0, (HSLFGroupShape)parent);
           }
           
           AffineTransform tx = new AffineTransform();
           for(HSLFGroupShape prnt : lst) {
               Rectangle2D exterior = prnt.getAnchor2D();
               Rectangle2D interior = prnt.getInteriorAnchor();

               double scaleX =  exterior.getWidth() / interior.getWidth();
               double scaleY = exterior.getHeight() / interior.getHeight();

               tx.translate(exterior.getX(), exterior.getY());
               tx.scale(scaleX, scaleY);
               tx.translate(-interior.getX(), -interior.getY());
               
           }
           anchor = tx.createTransformedShape(anchor).getBounds2D();
       }

       double angle = getRotation();
       if(angle != 0.){
           double centerX = anchor.getX() + anchor.getWidth()/2;
           double centerY = anchor.getY() + anchor.getHeight()/2;

           AffineTransform trans = new AffineTransform();
           trans.translate(centerX, centerY);
           trans.rotate(Math.toRadians(angle));
           trans.translate(-centerX, -centerY);

           Rectangle2D rect = trans.createTransformedShape(anchor).getBounds2D();
           if((anchor.getWidth() < anchor.getHeight() && rect.getWidth() > rect.getHeight()) ||
               (anchor.getWidth() > anchor.getHeight() && rect.getWidth() < rect.getHeight())    ){
               trans = new AffineTransform();
               trans.translate(centerX, centerY);
               trans.rotate(Math.PI/2);
               trans.translate(-centerX, -centerY);
               anchor = trans.createTransformedShape(anchor).getBounds2D();
           }
       }
       return anchor;
   }


}
