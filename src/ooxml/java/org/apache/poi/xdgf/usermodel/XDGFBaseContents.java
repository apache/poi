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
package org.apache.poi.xdgf.usermodel;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Internal;
import org.apache.poi.xdgf.exceptions.XDGFException;
import org.apache.poi.xdgf.usermodel.shape.ShapeRenderer;
import org.apache.poi.xdgf.usermodel.shape.ShapeVisitor;
import org.apache.poi.xdgf.usermodel.shape.exceptions.StopVisiting;
import org.apache.poi.xdgf.xml.XDGFXMLDocumentPart;

import com.microsoft.schemas.office.visio.x2012.main.ConnectType;
import com.microsoft.schemas.office.visio.x2012.main.PageContentsType;
import com.microsoft.schemas.office.visio.x2012.main.ShapeSheetType;

/**
 * Container of shapes for a page in a Visio diagram. Shapes are not
 * necessarily literal shapes in the diagram, but is the term that is
 * used to describe the basic elements that make up a Visio diagram.
 */
public class XDGFBaseContents extends XDGFXMLDocumentPart {

    protected PageContentsType _pageContents;

    // shapes without parents
    protected List<XDGFShape> _toplevelShapes = new ArrayList<>();
    protected Map<Long, XDGFShape> _shapes = new HashMap<>();
    protected List<XDGFConnection> _connections = new ArrayList<>();

    /**
     * @since POI 3.14-Beta1
     */
    public XDGFBaseContents(PackagePart part, XDGFDocument document) {
        super(part, document);
    }
    
    @Internal
    public PageContentsType getXmlObject() {
        return _pageContents;
    }


    @Override
    protected void onDocumentRead() {

        if (_pageContents.isSetShapes()) {
            for (ShapeSheetType shapeSheet: _pageContents.getShapes().getShapeArray()) {
                XDGFShape shape = new XDGFShape(shapeSheet, this, _document);
                _toplevelShapes.add(shape);
                addToShapeIndex(shape);
            }
        }

        if (_pageContents.isSetConnects()) {
            for (ConnectType connect: _pageContents.getConnects().getConnectArray()) {

                XDGFShape from = _shapes.get(connect.getFromSheet());
                XDGFShape to = _shapes.get(connect.getToSheet());

                if (from == null)
                    throw new POIXMLException(this + "; Connect; Invalid from id: " + connect.getFromSheet());

                if (to == null)
                    throw new POIXMLException(this + "; Connect; Invalid to id: " + connect.getToSheet());

                _connections.add(new XDGFConnection(connect, from, to));
            }
        }
    }

    protected void addToShapeIndex(XDGFShape shape) {
        _shapes.put(shape.getID(), shape);

        List<XDGFShape> shapes = shape.getShapes();
        if (shapes == null)
            return;

        for (XDGFShape subshape: shapes)
            addToShapeIndex(subshape);
    }

    //
    // API
    //

    /**
     * Draws the contents of a page onto a Graphics2D object
     *
     * @param graphics The context to draw on.
     */
    public void draw(Graphics2D graphics) {
        visitShapes(new ShapeRenderer(graphics));
    }


    public XDGFShape getShapeById(long id) {
        return _shapes.get(id);
    }

    public Map<Long, XDGFShape> getShapesMap() {
        return Collections.unmodifiableMap(_shapes);
    }

    public Collection<XDGFShape> getShapes() {
        return _shapes.values();
    }

    public List<XDGFShape> getTopLevelShapes() {
        return Collections.unmodifiableList(_toplevelShapes);
    }
    
    public List<XDGFConnection> getConnections() {
        return Collections.unmodifiableList(_connections);
    }

    @Override
    public String toString() {
        return getPackagePart().getPartName().toString();
    }


    /**
     * Provides iteration over the shapes using the visitor pattern, and provides
     * an easy way to convert shape coordinates into global coordinates
     */
    public void visitShapes(ShapeVisitor visitor) {
        try {
            for (XDGFShape shape: _toplevelShapes) {
                shape.visitShapes(visitor, new AffineTransform(), 0);
            }
        } catch (StopVisiting e) {
            // intentionally empty
        } catch (POIXMLException e) {
            throw XDGFException.wrap(this, e);
        }
    }

}
