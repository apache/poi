package org.apache.poi.hssf.usermodel.drawing;

import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFSimpleShape;
import org.apache.poi.hssf.usermodel.HSSFTextbox;

/**
 * @author Evgeniy Berlog
 * date: 08.06.12
 */
public enum HSSFShapeType {
    NOT_PRIMITIVE((short)0x0, null, (short)0),
    RECTANGLE((short)0x1, HSSFSimpleShape.class, HSSFSimpleShape.OBJECT_TYPE_RECTANGLE),
    PICTURE((short)0x004B, HSSFPicture.class, HSSFSimpleShape.OBJECT_TYPE_PICTURE),
    LINE((short)0x14, HSSFSimpleShape.class, HSSFSimpleShape.OBJECT_TYPE_LINE),
    OVAL(EscherAggregate.ST_ELLIPSE, HSSFSimpleShape.class, HSSFSimpleShape.OBJECT_TYPE_OVAL),
    ARC(EscherAggregate.ST_ARC, HSSFSimpleShape.class, HSSFSimpleShape.OBJECT_TYPE_ARC),
    TEXT((short)202, HSSFTextbox.class, HSSFTextbox.OBJECT_TYPE_TEXT),
    ROUND_RECTANGLE((short)0x2, null, null);

    private Short type;
    private Class shape;
    private Short objectType;

    private HSSFShapeType(Short type, Class shape, Short objectType) {
        this.type = type;
        this.shape = shape;
        this.objectType = objectType;
    }

    public Short getType() {
        return type;
    }

    public Class getShape() {
        return shape;
    }

    public Short getObjectType() {
        return objectType;
    }
}
