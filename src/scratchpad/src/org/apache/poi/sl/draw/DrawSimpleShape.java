package org.apache.poi.sl.draw;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;

import javax.xml.bind.*;
import javax.xml.stream.*;
import javax.xml.stream.EventFilter;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.poi.sl.draw.binding.CTCustomGeometry2D;
import org.apache.poi.sl.draw.geom.*;
import org.apache.poi.sl.usermodel.*;
import org.apache.poi.sl.usermodel.LineDecoration.DecorationSize;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.apache.poi.util.Units;


public class DrawSimpleShape<T extends SimpleShape> extends DrawShape<T> {

    public DrawSimpleShape(T shape) {
        super(shape);
    }

    @Override
    public void draw(Graphics2D graphics) {
//        RenderableShape rShape = new RenderableShape(this);
//        rShape.render(graphics);

        DrawPaint drawPaint = DrawFactory.getInstance(graphics).getPaint(shape);
        Paint fill = drawPaint.getPaint(graphics, shape.getFillStyle().getPaint());
        Paint line = drawPaint.getPaint(graphics, shape.getStrokeStyle().getPaint());
        BasicStroke stroke = getStroke(); // the stroke applies both to the shadow and the shape
        graphics.setStroke(stroke);

        Collection<Outline> elems = computeOutlines(graphics);
        
        // first paint the shadow
        drawShadow(graphics, elems, fill, line);
        
        // then fill the shape interior
        if (fill != null) {
            graphics.setPaint(fill);
            for (Outline o : elems) {
                if (o.getPath().isFilled()){
                    java.awt.Shape s = o.getOutline();
                    graphics.setRenderingHint(Drawable.GRADIENT_SHAPE, s);
                    graphics.fill(s);
                }                
            }
        }
        
        // then draw any content within this shape (text, image, etc.)
        drawContent(graphics);

        // then stroke the shape outline
        if(line != null) {
            graphics.setPaint(line);
            for(Outline o : elems){
                if(o.getPath().isStroked()){
                    java.awt.Shape s = o.getOutline();
                    graphics.setRenderingHint(Drawable.GRADIENT_SHAPE, s);
                    graphics.draw(s);
                }
            }
        }
        
        // draw line decorations
        drawDecoration(graphics, line, stroke);
    }

    protected void drawDecoration(Graphics2D graphics, Paint line, BasicStroke stroke) {
        if(line == null) return;
        graphics.setPaint(line);
        
        List<Outline> lst = new ArrayList<Outline>();
        LineDecoration deco = shape.getLineDecoration();
        Outline head = getHeadDecoration(graphics, deco, stroke);
        if (head != null) lst.add(head);
        Outline tail = getTailDecoration(graphics, deco, stroke);
        if (tail != null) lst.add(tail);
        
        
        for(Outline o : lst){
            java.awt.Shape s = o.getOutline();
            Path p = o.getPath();
            graphics.setRenderingHint(Drawable.GRADIENT_SHAPE, s);
            
            if(p.isFilled()) graphics.fill(s);
            if(p.isStroked()) graphics.draw(s);
        }
    }

    protected Outline getTailDecoration(Graphics2D graphics, LineDecoration deco, BasicStroke stroke) {
        DecorationSize tailLength = deco.getTailLength();
        DecorationSize tailWidth = deco.getTailWidth();
    
        double lineWidth = Math.max(2.5, stroke.getLineWidth());
    
        Rectangle2D anchor = getAnchor(graphics, shape);
        double x2 = anchor.getX() + anchor.getWidth(),
                y2 = anchor.getY() + anchor.getHeight();
    
        double alpha = Math.atan(anchor.getHeight() / anchor.getWidth());
    
        AffineTransform at = new AffineTransform();
        java.awt.Shape shape = null;
        Path p = null;
        Rectangle2D bounds;
        double scaleY = Math.pow(2, tailWidth.ordinal());
        double scaleX = Math.pow(2, tailLength.ordinal());
        switch (deco.getTailShape()) {
            case OVAL:
                p = new Path();
                shape = new Ellipse2D.Double(0, 0, lineWidth * scaleX, lineWidth * scaleY);
                bounds = shape.getBounds2D();
                at.translate(x2 - bounds.getWidth() / 2, y2 - bounds.getHeight() / 2);
                at.rotate(alpha, bounds.getX() + bounds.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2);
                break;
            case ARROW:
                p = new Path();
                GeneralPath arrow = new GeneralPath();
                arrow.moveTo((float) (-lineWidth * 3), (float) (-lineWidth * 2));
                arrow.lineTo(0, 0);
                arrow.lineTo((float) (-lineWidth * 3), (float) (lineWidth * 2));
                shape = arrow;
                at.translate(x2, y2);
                at.rotate(alpha);
                break;
            case TRIANGLE:
                p = new Path();
                scaleY = tailWidth.ordinal() + 1;
                scaleX = tailLength.ordinal() + 1;
                GeneralPath triangle = new GeneralPath();
                triangle.moveTo((float) (-lineWidth * scaleX), (float) (-lineWidth * scaleY / 2));
                triangle.lineTo(0, 0);
                triangle.lineTo((float) (-lineWidth * scaleX), (float) (lineWidth * scaleY / 2));
                triangle.closePath();
                shape = triangle;
                at.translate(x2, y2);
                at.rotate(alpha);
                break;
            default:
                break;
        }
    
        if (shape != null) {
            shape = at.createTransformedShape(shape);
        }
        return shape == null ? null : new Outline(shape, p);
    }
    
    Outline getHeadDecoration(Graphics2D graphics, LineDecoration deco, BasicStroke stroke) {
        DecorationSize headLength = deco.getHeadLength();
        DecorationSize headWidth = deco.getHeadWidth();
    
        double lineWidth = Math.max(2.5, stroke.getLineWidth());
    
        Rectangle2D anchor = getAnchor(graphics, shape);
        double x1 = anchor.getX(),
                y1 = anchor.getY();
    
        double alpha = Math.atan(anchor.getHeight() / anchor.getWidth());
    
        AffineTransform at = new AffineTransform();
        java.awt.Shape shape = null;
        Path p = null;
        Rectangle2D bounds;
        double scaleY = 1;
        double scaleX = 1;
        switch (deco.getHeadShape()) {
            case OVAL:
                p = new Path();
                shape = new Ellipse2D.Double(0, 0, lineWidth * scaleX, lineWidth * scaleY);
                bounds = shape.getBounds2D();
                at.translate(x1 - bounds.getWidth() / 2, y1 - bounds.getHeight() / 2);
                at.rotate(alpha, bounds.getX() + bounds.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2);
                break;
            case STEALTH:
            case ARROW:
                p = new Path(false, true);
                GeneralPath arrow = new GeneralPath();
                arrow.moveTo((float) (lineWidth * 3 * scaleX), (float) (-lineWidth * scaleY * 2));
                arrow.lineTo(0, 0);
                arrow.lineTo((float) (lineWidth * 3 * scaleX), (float) (lineWidth * scaleY * 2));
                shape = arrow;
                at.translate(x1, y1);
                at.rotate(alpha);
                break;
            case TRIANGLE:
                p = new Path();
                scaleY = headWidth.ordinal() + 1;
                scaleX = headLength.ordinal() + 1;
                GeneralPath triangle = new GeneralPath();
                triangle.moveTo((float) (lineWidth * scaleX), (float) (-lineWidth * scaleY / 2));
                triangle.lineTo(0, 0);
                triangle.lineTo((float) (lineWidth * scaleX), (float) (lineWidth * scaleY / 2));
                triangle.closePath();
                shape = triangle;
                at.translate(x1, y1);
                at.rotate(alpha);
                break;
            default:
                break;
        }
    
        if (shape != null) {
            shape = at.createTransformedShape(shape);
        }
        return shape == null ? null : new Outline(shape, p);
    }
    
    public BasicStroke getStroke() {
        StrokeStyle strokeStyle = shape.getStrokeStyle();
        
        float lineWidth = (float) strokeStyle.getLineWidth();
        if (lineWidth == 0.0f) lineWidth = 0.25f; // Both PowerPoint and OOo draw zero-length lines as 0.25pt

        LineDash lineDash = strokeStyle.getLineDash();
        int dashPatI[] = lineDash.pattern;
        float[] dashPatF = new float[dashPatI.length];
        final float dash_phase = 0;
        for (int i=0; i<dashPatI.length; i++) {
            dashPatF[i] = dashPatI[i]*lineWidth;
        }

        int lineCap;
        switch (strokeStyle.getLineCap()) {
            case ROUND:
                lineCap = BasicStroke.CAP_ROUND;
                break;
            case SQUARE:
                lineCap = BasicStroke.CAP_SQUARE;
                break;
            default:
            case FLAT:
                lineCap = BasicStroke.CAP_BUTT;
                break;
        }

        int lineJoin = BasicStroke.JOIN_ROUND;

        return new BasicStroke(lineWidth, lineCap, lineJoin, Math.max(1, lineWidth), dashPatF, dash_phase);
    }

    protected void drawShadow(
            Graphics2D graphics
          , Collection<Outline> outlines
          , Paint fill
          , Paint line
    ) {
          Shadow shadow = shape.getShadow();
          if (shadow == null || (fill == null && line == null)) return;

          SolidPaint shadowPaint = shadow.getFillStyle();
          Color shadowColor = DrawPaint.applyColorTransform(shadowPaint.getSolidColor());
          
          double shapeRotation = shape.getRotation();
          if(shape.getFlipVertical()) {
              shapeRotation += 180;
          }
          double angle = shadow.getAngle() - shapeRotation;
          double dist = shadow.getDistance();
          double dx = dist * Math.cos(Math.toRadians(angle));
          double dy = dist * Math.sin(Math.toRadians(angle));
          
          graphics.translate(dx, dy);
          
          for(Outline o : outlines){
              java.awt.Shape s = o.getOutline();
              Path p = o.getPath();
              graphics.setRenderingHint(Drawable.GRADIENT_SHAPE, s);
              graphics.setPaint(shadowColor);
              
              if(fill != null && p.isFilled()){
                  graphics.fill(s);
              } else if (line != null && p.isStroked()) {
                  graphics.draw(s);
              }
          }

          graphics.translate(-dx, -dy);
      }
      
    protected static CustomGeometry getCustomGeometry(String name) {
        return getCustomGeometry(name, null);
    }
    
    protected static CustomGeometry getCustomGeometry(String name, Graphics2D graphics) {
        @SuppressWarnings("unchecked")
        Map<String, CustomGeometry> presets = (graphics == null)
            ? null
            : (Map<String, CustomGeometry>)graphics.getRenderingHint(Drawable.PRESET_GEOMETRY_CACHE);
        
        if (presets == null) {
            presets = new HashMap<String,CustomGeometry>();
            if (graphics != null) {
                graphics.setRenderingHint(Drawable.PRESET_GEOMETRY_CACHE, presets);
            }
            
            String packageName = "org.apache.poi.sl.draw.binding";
            InputStream presetIS = Drawable.class.getResourceAsStream("presetShapeDefinitions.xml");
            Reader xml = new InputStreamReader( presetIS, Charset.forName("UTF-8") );
    
            // StAX:
            EventFilter startElementFilter = new EventFilter() {
                @Override
                public boolean accept(XMLEvent event) {
                    return event.isStartElement();
                }
            };
            
            try {
                XMLInputFactory staxFactory = XMLInputFactory.newInstance();
                XMLEventReader staxReader = staxFactory.createXMLEventReader(xml);
                XMLEventReader staxFiltRd = staxFactory.createFilteredReader(staxReader, startElementFilter);
                // Ignore StartElement:
                staxFiltRd.nextEvent();
                // JAXB:
                JAXBContext jaxbContext = JAXBContext.newInstance(packageName);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        
                while (staxFiltRd.peek() != null) {
                    StartElement evRoot = (StartElement)staxFiltRd.peek();
                    String cusName = evRoot.getName().getLocalPart();
                    // XMLEvent ev = staxReader.nextEvent();
                    JAXBElement<org.apache.poi.sl.draw.binding.CTCustomGeometry2D> el = unmarshaller.unmarshal(staxReader, CTCustomGeometry2D.class);
                    CTCustomGeometry2D cusGeom = el.getValue();
                    
                    presets.put(cusName, new CustomGeometry(cusGeom));
                }
            } catch (Exception e) {
                throw new RuntimeException("Unable to load preset geometries.", e);
            }
        }
        
        return presets.get(name);
    }
    
    protected Collection<Outline> computeOutlines(Graphics2D graphics) {

        List<Outline> lst = new ArrayList<Outline>();
        CustomGeometry geom = shape.getGeometry();
        if(geom == null) {
            return lst;
        }

        Rectangle2D anchor = getAnchor(graphics, shape);
        for (Path p : geom) {

            double w = p.getW() == -1 ? anchor.getWidth() * Units.EMU_PER_POINT : p.getW();
            double h = p.getH() == -1 ? anchor.getHeight() * Units.EMU_PER_POINT : p.getH();

            // the guides in the shape definitions are all defined relative to each other,
            // so we build the path starting from (0,0).
            final Rectangle2D pathAnchor = new Rectangle2D.Double(0,0,w,h);

            Context ctx = new Context(geom, pathAnchor, shape);

            java.awt.Shape gp = p.getPath(ctx);

            // translate the result to the canvas coordinates in points
            AffineTransform at = new AffineTransform();
            at.translate(anchor.getX(), anchor.getY());

            double scaleX, scaleY;
            if (p.getW() != -1) {
                scaleX = anchor.getWidth() / p.getW();
            } else {
                scaleX = 1.0 / Units.EMU_PER_POINT;
            }
            if (p.getH() != -1) {
                scaleY = anchor.getHeight() / p.getH();
            } else {
                scaleY = 1.0 / Units.EMU_PER_POINT;
            }

            at.scale(scaleX, scaleY);

            java.awt.Shape canvasShape = at.createTransformedShape(gp);

            lst.add(new Outline(canvasShape, p));
        }

        return lst;
    }

}
