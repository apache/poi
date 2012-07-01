package org.apache.poi.hssf.model;

import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFPolygon;
import org.apache.poi.hssf.usermodel.HSSFTextbox;

/**
 * @author Evgeniy Berlog
 * @date 25.06.12
 */
public class HSSFTestModelHelper {
    public static TextboxShape createTextboxShape(int shapeId, HSSFTextbox textbox){
        return new TextboxShape(textbox, shapeId);
    }

    public static CommentShape createCommentShape(int shapeId, HSSFComment comment){
        return new CommentShape(comment, shapeId);
    }

    public static PolygonShape createPolygonShape(int shapeId, HSSFPolygon polygon){
        return new PolygonShape(polygon, shapeId);
    }
}
