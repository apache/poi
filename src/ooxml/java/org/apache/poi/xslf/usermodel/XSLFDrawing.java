package org.apache.poi.xslf.usermodel;

import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.util.Beta;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnector;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

import java.awt.*;


/**
 * @author Yegor Kozlov
 */
@Beta
public class XSLFDrawing {
    private XSLFSheet _sheet;
    private int _shapeId = 1;
    private CTGroupShape _spTree;

    /*package*/ XSLFDrawing(XSLFSheet sheet, CTGroupShape spTree){
        _sheet = sheet;
        _spTree = spTree;
        XmlObject[] cNvPr = sheet.getSpTree().selectPath(
                "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:cNvPr");
        for(XmlObject o : cNvPr) {
            CTNonVisualDrawingProps p = (CTNonVisualDrawingProps)o;
            _shapeId = (int)Math.max(_shapeId, p.getId());
        }
    }

    public XSLFAutoShape createAutoShape(){
        CTShape sp = _spTree.addNewSp();
        sp.set(XSLFAutoShape.prototype(_shapeId++));
        XSLFAutoShape shape = new XSLFAutoShape(sp, _sheet);
        shape.setAnchor(new Rectangle());
        return shape;
    }

    public XSLFFreeformShape createFreeform(){
        CTShape sp = _spTree.addNewSp();
        sp.set(XSLFFreeformShape.prototype(_shapeId++));
        XSLFFreeformShape shape = new XSLFFreeformShape(sp, _sheet);
        shape.setAnchor(new Rectangle());
        return shape;
    }

    public XSLFTextBox createTextBox(){
        CTShape sp = _spTree.addNewSp();
        sp.set(XSLFTextBox.prototype(_shapeId++));
        XSLFTextBox shape = new XSLFTextBox(sp, _sheet);
        shape.setAnchor(new Rectangle());
        return shape;
    }

    public XSLFConnectorShape createConnector(){
        CTConnector sp = _spTree.addNewCxnSp();
        sp.set(XSLFConnectorShape.prototype(_shapeId++));
        XSLFConnectorShape shape = new XSLFConnectorShape(sp, _sheet);
        shape.setAnchor(new Rectangle());
        shape.setLineColor(Color.black);
        shape.setLineWidth(0.75);
        return shape;
    }

    public XSLFGroupShape createGroup(){
        CTGroupShape obj = _spTree.addNewGrpSp();
        obj.set(XSLFGroupShape.prototype(_shapeId++));
        XSLFGroupShape shape = new XSLFGroupShape(obj, _sheet);
        shape.setAnchor(new Rectangle());
        return shape;
    }

    public XSLFPictureShape createPicture(String rel){
        CTPicture obj = _spTree.addNewPic();
        obj.set(XSLFPictureShape.prototype(_shapeId++, rel));
        XSLFPictureShape shape = new XSLFPictureShape(obj, _sheet);
        shape.setAnchor(new Rectangle());
        return shape;
    }
}
