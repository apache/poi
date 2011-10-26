package org.apache.poi.xslf.geom;

import junit.framework.TestCase;
import org.apache.poi.xslf.model.geom.*;

import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Map;

/**
 * Date: 10/24/11
 *
 * @author Yegor Kozlov
 */
public class TestPresetGeometries extends TestCase {
    public void testRead(){

        Map<String, CustomGeometry> shapes = PresetGeometries.getInstance();
        assertEquals(186, shapes.size());


        for(String name : shapes.keySet()) {
            CustomGeometry geom = shapes.get(name);
            Context ctx = new Context(geom, new IAdjustableShape() {
                public Rectangle2D getAnchor() {
                    return new Rectangle2D.Double(0, 0, 100, 100);
                }

                public Guide getAdjustValue(String name) {
                    return null;
                }
            });
            for(Path p : geom){
                GeneralPath path = p.getPath(ctx);
                assertNotNull(path);
            }
        }
    }
}
