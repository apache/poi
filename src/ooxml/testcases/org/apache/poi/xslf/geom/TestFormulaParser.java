package org.apache.poi.xslf.geom;

import junit.framework.TestCase;
import org.apache.poi.xslf.model.geom.*;
import org.openxmlformats.schemas.drawingml.x2006.main.CTCustomGeometry2D;

/**
 * Date: 10/24/11
 *
 * @author Yegor Kozlov
 */
public class TestFormulaParser extends TestCase {
    public void testParse(){

        Formula[] ops = {
            new Guide("adj1", "val 100"),
            new Guide("adj2", "val 200"),
            new Guide("adj3", "val -1"),
            new Guide("a1", "*/ adj1 2 adj2"), // a1 = 100*2 / 200
            new Guide("a2", "+- adj2 a1 adj1"), // a2 = 200 + a1 - 100
            new Guide("a3", "+/ adj1 adj2 adj2"), // a3 = (100 + 200) / 200
            new Guide("a4", "?: adj3 adj1 adj2"), // a4 = adj3 > 0 ? adj1 : adj2
            new Guide("a5", "abs -2"),
        };

        CustomGeometry geom = new CustomGeometry(CTCustomGeometry2D.Factory.newInstance());
        Context ctx = new Context(geom, null);
        for(Formula fmla : ops) {
            ctx.evaluate(fmla);
        }

        assertEquals(100.0, ctx.getValue("adj1"));
        assertEquals(200.0, ctx.getValue("adj2"));
        assertEquals(1.0, ctx.getValue("a1"));
        assertEquals(101.0, ctx.getValue("a2"));
        assertEquals(1.5, ctx.getValue("a3"));
        assertEquals(200.0, ctx.getValue("a4"));
        assertEquals(2.0, ctx.getValue("a5"));
    }
}
