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
package org.apache.poi.xslf.usermodel;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageNamespaces;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawPictureShape;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.util.Beta;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColorMapping;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnector;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTOleObject;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

@Beta
public abstract class XSLFSheet extends POIXMLDocumentPart
implements XSLFShapeContainer, Sheet<XSLFShape,XSLFTextParagraph> {
    private static POILogger LOG = POILogFactory.getLogger(XSLFSheet.class);

    private XSLFDrawing _drawing;
    private List<XSLFShape> _shapes;
    private CTGroupShape _spTree;
    private XSLFTheme _theme;

    private List<XSLFTextShape>_placeholders;
    private Map<Integer, XSLFSimpleShape> _placeholderByIdMap;
    private Map<Integer, XSLFSimpleShape> _placeholderByTypeMap;

    private final BitSet shapeIds = new BitSet();

    protected XSLFSheet() {
        super();
    }

    /**
     * @since POI 3.14-Beta1
     */
    protected XSLFSheet(PackagePart part) {
        super(part);
    }

    /**
     * @return the XMLSlideShow this sheet belongs to
     */
    @Override
    public XMLSlideShow getSlideShow() {
        POIXMLDocumentPart p = getParent();
        while(p != null) {
            if(p instanceof XMLSlideShow){
                return (XMLSlideShow)p;
            }
            p = p.getParent();
        }
        throw new IllegalStateException("SlideShow was not found");
    }

    @SuppressWarnings("WeakerAccess")
    protected int allocateShapeId() {
        final int nextId = shapeIds.nextClearBit(1);
        shapeIds.set(nextId);
        return nextId;
    }

    @SuppressWarnings("WeakerAccess")
    protected void registerShapeId(final int shapeId) {
        if (shapeIds.get(shapeId)) {
            LOG.log(POILogger.WARN, "shape id "+shapeId+" has been already used.");
        }
        shapeIds.set(shapeId);
    }

    @SuppressWarnings("WeakerAccess")
    protected void deregisterShapeId(final int shapeId) {
        if (!shapeIds.get(shapeId)) {
            LOG.log(POILogger.WARN, "shape id "+shapeId+" hasn't been registered.");
        }
        shapeIds.clear(shapeId);
    }

    @SuppressWarnings("WeakerAccess")
    protected static List<XSLFShape> buildShapes(CTGroupShape spTree, XSLFShapeContainer parent){
        final XSLFSheet sheet = (parent instanceof XSLFSheet) ? (XSLFSheet)parent : ((XSLFShape)parent).getSheet();

        List<XSLFShape> shapes = new ArrayList<>();
        XmlCursor cur = spTree.newCursor();
        try {
            for (boolean b=cur.toFirstChild();b;b=cur.toNextSibling()) {
                XmlObject ch = cur.getObject();
                if(ch instanceof CTShape){
                    // simple shape
                    XSLFAutoShape shape = XSLFAutoShape.create((CTShape)ch, sheet);
                    shapes.add(shape);
                } else if (ch instanceof CTGroupShape){
                    shapes.add(new XSLFGroupShape((CTGroupShape)ch, sheet));
                } else if (ch instanceof CTConnector){
                    shapes.add(new XSLFConnectorShape((CTConnector)ch, sheet));
                } else if (ch instanceof CTPicture){
                    shapes.add(new XSLFPictureShape((CTPicture)ch, sheet));
                } else if (ch instanceof CTGraphicalObjectFrame){
                    XSLFGraphicFrame shape = XSLFGraphicFrame.create((CTGraphicalObjectFrame)ch, sheet);
                    shapes.add(shape);
                } else if (ch instanceof XmlAnyTypeImpl) {
                    // TODO: the link of the XLSF classes to the xml beans objects will
                    // be broken, when the elements are parsed a second time.
                    // Unfortunately, the xml schema type can't be set of the alternate
                    // content element
                    cur.push();
                    if (cur.toChild(PackageNamespaces.MARKUP_COMPATIBILITY, "Choice") && cur.toFirstChild()) {
                        try {
                            CTGroupShape grp = CTGroupShape.Factory.parse(cur.newXMLStreamReader());
                            shapes.addAll(buildShapes(grp, parent));
                        } catch (XmlException e) {
                            LOG.log(POILogger.DEBUG, "unparsable alternate content", e);
                        }
                    }
                    cur.pop();
                }
            }
        } finally {
            cur.dispose();
        }

        for (final XSLFShape s : shapes) {
            s.setParent(parent);
        }

        return shapes;
    }

    /**
     * @return top-level Xml bean representing this sheet
     */
    public abstract XmlObject getXmlObject();

    private XSLFDrawing getDrawing(){
        initDrawingAndShapes();
        return _drawing;
    }

    /**
     * Returns an array containing all of the shapes in this sheet
     *
     * @return an array of all shapes in this sheet
     */
    @Override
    public List<XSLFShape> getShapes(){
        initDrawingAndShapes();
        return _shapes;
    }

    /**
     * Helper method for initializing drawing and shapes in one go.
     * If they are initialized separately, there's a risk that shapes
     * get added twice, e.g. a shape is added to the drawing, then
     * buildShapes is called and at last the shape is added to shape list
     */
    private void initDrawingAndShapes() {
        CTGroupShape cgs = getSpTree();
        if(_drawing == null) {
            _drawing = new XSLFDrawing(this, cgs);
        }
        if (_shapes == null) {
            _shapes = buildShapes(cgs, this);
        }
    }

    // shape factory methods

    @Override
    public XSLFAutoShape createAutoShape(){
        XSLFAutoShape sh = getDrawing().createAutoShape();
        getShapes().add(sh);
        sh.setParent(this);
        return sh;
    }

    @Override
    public XSLFFreeformShape createFreeform(){
        XSLFFreeformShape sh = getDrawing().createFreeform();
        getShapes().add(sh);
        sh.setParent(this);
        return sh;
    }

    @Override
    public XSLFTextBox createTextBox(){
        XSLFTextBox sh = getDrawing().createTextBox();
        getShapes().add(sh);
        sh.setParent(this);
        return sh;
    }

    @Override
    public XSLFConnectorShape createConnector(){
        XSLFConnectorShape sh = getDrawing().createConnector();
        getShapes().add(sh);
        sh.setParent(this);
        return sh;
    }

    @Override
    public XSLFGroupShape createGroup(){
        XSLFGroupShape sh = getDrawing().createGroup();
        getShapes().add(sh);
        sh.setParent(this);
        return sh;
    }

    @Override
    public XSLFPictureShape createPicture(PictureData pictureData){
        if (!(pictureData instanceof XSLFPictureData)) {
            throw new IllegalArgumentException("pictureData needs to be of type XSLFPictureData");
        }

        RelationPart rp = addRelation(null, XSLFRelation.IMAGES, (XSLFPictureData)pictureData);

        XSLFPictureShape sh = getDrawing().createPicture(rp.getRelationship().getId());
        new DrawPictureShape(sh).resize();
        getShapes().add(sh);
        sh.setParent(this);
        return sh;
    }

    public XSLFTable createTable(){
        XSLFTable sh = getDrawing().createTable();
        getShapes().add(sh);
        sh.setParent(this);
        return sh;
    }

    @Override
    public XSLFTable createTable(int numRows, int numCols){
        if (numRows < 1 || numCols < 1) {
            throw new IllegalArgumentException("numRows and numCols must be greater than 0");
        }
        XSLFTable sh = getDrawing().createTable();
        getShapes().add(sh);
        sh.setParent(this);
        for (int r=0; r<numRows; r++) {
            XSLFTableRow row = sh.addRow();
            for (int c=0; c<numCols; c++) {
                row.addCell();
            }
        }
        return sh;
    }


    @Override
    public XSLFObjectShape createOleShape(PictureData pictureData) {
        if (!(pictureData instanceof XSLFPictureData)) {
            throw new IllegalArgumentException("pictureData needs to be of type XSLFPictureData");
        }
        RelationPart rp = addRelation(null, XSLFRelation.IMAGES, (XSLFPictureData)pictureData);
        
        XSLFObjectShape sh = getDrawing().createOleShape(rp.getRelationship().getId());
        CTOleObject oleObj = sh.getCTOleObject();
        Dimension dim = pictureData.getImageDimension();
        oleObj.setImgW(Units.toEMU(dim.getWidth()));
        oleObj.setImgH(Units.toEMU(dim.getHeight()));
        
        
        getShapes().add(sh);
        sh.setParent(this);
        return sh;
    }

    /**
     * Returns an iterator over the shapes in this sheet
     *
     * @return an iterator over the shapes in this sheet
     */
    @Override
    public Iterator<XSLFShape> iterator(){
        return getShapes().iterator();
    }

    @Override
    public void addShape(XSLFShape shape) {
        throw new UnsupportedOperationException(
            "Adding a shape from a different container is not supported -"
            + " create it from scratch witht XSLFSheet.create* methods");
    }

    /**
     * Removes the specified shape from this sheet, if it is present
     * (optional operation).  If this sheet does not contain the element,
     * it is unchanged.
     *
     * @param xShape shape to be removed from this sheet, if present
     * @return <tt>true</tt> if this sheet contained the specified element
     * @throws IllegalArgumentException if the type of the specified shape
     *         is incompatible with this sheet (optional)
     */
    @Override
    public boolean removeShape(XSLFShape xShape) {
        XmlObject obj = xShape.getXmlObject();
        CTGroupShape spTree = getSpTree();
        deregisterShapeId(xShape.getShapeId());
        if(obj instanceof CTShape){
            spTree.getSpList().remove(obj);
        } else if (obj instanceof CTGroupShape) {
            XSLFGroupShape gs = (XSLFGroupShape)xShape;
            new ArrayList<>(gs.getShapes()).forEach(gs::removeShape);
            spTree.getGrpSpList().remove(obj);
        } else if (obj instanceof CTConnector) {
            spTree.getCxnSpList().remove(obj);
        } else if (obj instanceof CTGraphicalObjectFrame) {
            spTree.getGraphicFrameList().remove(obj);
        } else if (obj instanceof CTPicture) {
            XSLFPictureShape ps = (XSLFPictureShape)xShape;
            removePictureRelation(ps);
            spTree.getPicList().remove(obj);
        } else {
            throw new IllegalArgumentException("Unsupported shape: " + xShape);
        }
        return getShapes().remove(xShape);
    }

    /**
     * Removes all of the elements from this container (optional operation).
     * The container will be empty after this call returns.
     */
    @Override
    public void clear() {
        List<XSLFShape> shapes = new ArrayList<>(getShapes());
        for(XSLFShape shape : shapes){
            removeShape(shape);
        }
    }

    protected abstract String getRootElementName();

    @SuppressWarnings("WeakerAccess")
    protected CTGroupShape getSpTree(){
        if(_spTree == null) {
            XmlObject root = getXmlObject();
            XmlObject[] sp = root.selectPath(
                    "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:spTree");
            if(sp.length == 0) {
                throw new IllegalStateException("CTGroupShape was not found");
            }
            _spTree = (CTGroupShape)sp[0];
        }
        return _spTree;
    }

    @Override
    protected final void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        String docName = getRootElementName();
        if(docName != null) {
            xmlOptions.setSaveSyntheticDocumentElement(
                    new QName("http://schemas.openxmlformats.org/presentationml/2006/main", docName));
        }

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        getXmlObject().save(out, xmlOptions);
        out.close();
    }

    /**
     * Set the contents of this sheet to be a copy of the source sheet.
     * This method erases any existing shapes and replaces them with
     * object from the source sheet.
     *
     * @param src the source sheet to copy data from
     * @return modified 'this'
     */
    public XSLFSheet importContent(XSLFSheet src){
        _spTree = null;

        // first copy the source xml
        getSpTree().set(src.getSpTree().copy());

        wipeAndReinitialize(src, 0);

        return this;
    }

    private void wipeAndReinitialize(XSLFSheet src, int offset) {
        // explicitly initialize drawing and shapes from _spTree
        _shapes = null;
        _drawing = null;
        initDrawingAndShapes();

        // placeholders will be implicitly initialized when requested
        _placeholders = null;

        // update each shape according to its own additional copy rules
        List<XSLFShape> tgtShapes = getShapes();
        List<XSLFShape> srcShapes = src.getShapes();
        for(int i = 0; i < srcShapes.size(); i++){
            XSLFShape s1 = srcShapes.get(i);
            XSLFShape s2 = tgtShapes.get(offset + i);

            s2.copy(s1);
        }

    }

    /**
     * Append content to this sheet.
     *
     * @param src the source sheet
     * @return modified <code>this</code>.
     */
    @SuppressWarnings("unused")
    public XSLFSheet appendContent(XSLFSheet src){
        int numShapes = getShapes().size();
        CTGroupShape spTree = getSpTree();
        CTGroupShape srcTree = src.getSpTree();

        for(XmlObject ch : srcTree.selectPath("*")){
            if(ch instanceof CTShape){ // simple shape
                spTree.addNewSp().set(ch.copy());
            } else if (ch instanceof CTGroupShape){
                spTree.addNewGrpSp().set(ch.copy());
            } else if (ch instanceof CTConnector){
                spTree.addNewCxnSp().set(ch.copy());
            } else if (ch instanceof CTPicture){
                spTree.addNewPic().set(ch.copy());
            } else if (ch instanceof CTGraphicalObjectFrame){
                spTree.addNewGraphicFrame().set(ch.copy());
            }
        }

        wipeAndReinitialize(src, numShapes);

        return this;
    }

   /**
     * @return theme (shared styles) associated with this theme.
     *  By default returns <code>null</code> which means that this sheet is theme-less.
     *  Sheets that support the notion of themes (slides, masters, layouts, etc.) should override this
     *  method and return the corresponding package part.
     */
    public XSLFTheme getTheme() {
        if (_theme != null || !isSupportTheme()) {
            return _theme;
        }

        final Optional<XSLFTheme> t =
                getRelations().stream().filter((p) -> p instanceof XSLFTheme).map((p) -> (XSLFTheme) p).findAny();
        if (t.isPresent()) {
            _theme = t.get();
            final CTColorMapping cmap = getColorMapping();
            if (cmap != null) {
                _theme.initColorMap(cmap);
            }
        }
        return _theme;
    }



    /**
     * @return {@code true} if this class supports themes
     */
    boolean isSupportTheme() {
        return false;
    }

    /**
     * @return the color mapping for this slide type
     */
    CTColorMapping getColorMapping() {
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    protected XSLFTextShape getTextShapeByType(Placeholder type){
        for(XSLFShape shape : this.getShapes()){
            if(shape instanceof XSLFTextShape) {
               XSLFTextShape txt = (XSLFTextShape)shape;
                if(txt.getTextType() == type) {
                    return txt;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    public XSLFSimpleShape getPlaceholder(Placeholder ph) {
        return getPlaceholderByType(ph.ooxmlId);
    }

    XSLFSimpleShape getPlaceholder(CTPlaceholder ph) {
        XSLFSimpleShape shape = null;
        if(ph.isSetIdx()) {
            shape = getPlaceholderById((int)ph.getIdx());
        }

        if (shape == null && ph.isSetType()) {
            shape = getPlaceholderByType(ph.getType().intValue());
        }
        return shape;
    }

    private void initPlaceholders() {
        if(_placeholders == null) {
            _placeholders = new ArrayList<>();
            _placeholderByIdMap = new HashMap<>();
            _placeholderByTypeMap = new HashMap<>();

            for(final XSLFShape sh : getShapes()){
                if(sh instanceof XSLFTextShape){
                    final XSLFTextShape sShape = (XSLFTextShape)sh;
                    final CTPlaceholder ph = sShape.getPlaceholderDetails().getCTPlaceholder(false);
                    if(ph != null) {
                        _placeholders.add(sShape);
                        if(ph.isSetIdx()) {
                            int idx = (int)ph.getIdx();
                            _placeholderByIdMap.put(idx, sShape);
                        }
                        if(ph.isSetType()){
                            _placeholderByTypeMap.put(ph.getType().intValue(), sShape);
                        }
                    }
                }
            }
        }
    }

    private XSLFSimpleShape getPlaceholderById(int id) {
        initPlaceholders();
        return _placeholderByIdMap.get(id);
    }

    XSLFSimpleShape getPlaceholderByType(int ordinal) {
        initPlaceholders();
        return _placeholderByTypeMap.get(ordinal);
    }

    /**
     *
     * @param idx 0-based index of a placeholder in the sheet
     * @return placeholder
     */
    public XSLFTextShape getPlaceholder(int idx) {
        initPlaceholders();
        return _placeholders.get(idx);
    }

    /**
     *
     * @return all placeholder shapes in this sheet
     */
    @SuppressWarnings("WeakerAccess")
    public XSLFTextShape[] getPlaceholders() {
        initPlaceholders();
        return _placeholders.toArray(new XSLFTextShape[0]);
    }

    /**
     *
     * @return whether shapes on the master sheet should be shown. By default master graphics is turned off.
     * Sheets that support the notion of master (slide, slideLayout) should override it and
     * check this setting in the sheet XML
     */
    @Override
    public boolean getFollowMasterGraphics(){
        return false;
    }

    /**
     * @return  background for this sheet
     */
    @Override
    public XSLFBackground getBackground() {
        return null;
    }

    /**
     * Render this sheet into the supplied graphics object
     *
     * @param graphics the graphics context to draw to
     */
    @Override
    public void draw(Graphics2D graphics){
        DrawFactory drawFact = DrawFactory.getInstance(graphics);
        Drawable draw = drawFact.getDrawable(this);
        draw.draw(graphics);
    }

    /**
     * Import a picture data from another document.
     *
     * @param blipId        ID of the package relationship to retrieve.
     * @param parent        parent document containing the data to import
     * @return ID of the created relationship
     */
    String importBlip(String blipId, POIXMLDocumentPart parent) {
        final XSLFPictureData parData = parent.getRelationPartById(blipId).getDocumentPart();
        final XSLFPictureData pictureData;
        if (getPackagePart().getPackage() == parent.getPackagePart().getPackage()) {
            // handle ref counter correct, if the parent document is the same as this
            pictureData = parData;
        } else {
            XMLSlideShow ppt = getSlideShow();
            pictureData = ppt.addPicture(parData.getData(), parData.getType());
        }

        RelationPart rp = addRelation(blipId, XSLFRelation.IMAGES, pictureData);
        return rp.getRelationship().getId();
    }

    /**
     * Import a package part into this sheet.
     */
    void importPart(PackageRelationship srcRel, PackagePart srcPafrt) {
        PackagePart destPP = getPackagePart();
        PackagePartName srcPPName = srcPafrt.getPartName();

        OPCPackage pkg = destPP.getPackage();
        if(pkg.containPart(srcPPName)){
            // already exists
            return;
        }

        destPP.addRelationship(srcPPName, TargetMode.INTERNAL, srcRel.getRelationshipType());

        PackagePart part = pkg.createPart(srcPPName, srcPafrt.getContentType());
        try {
            OutputStream out = part.getOutputStream();
            InputStream is = srcPafrt.getInputStream();
            IOUtils.copy(is, out);
            is.close();
            out.close();
        } catch (IOException e){
            throw new POIXMLException(e);
        }
    }

    /**
     * Helper method for sheet and group shapes
     *
     * @param pictureShape the picture shapes whose relation is to be removed
     */
    void removePictureRelation(XSLFPictureShape pictureShape) {
        removeRelation(pictureShape.getBlipId());
    }


    @Override
    public XSLFPlaceholderDetails getPlaceholderDetails(Placeholder placeholder) {
        final XSLFSimpleShape ph = getPlaceholder(placeholder);
        return (ph == null) ? null : new XSLFPlaceholderDetails(ph);
    }

}
