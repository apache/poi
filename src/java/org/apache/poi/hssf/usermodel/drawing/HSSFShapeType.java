package org.apache.poi.hssf.usermodel.drawing;

import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFSimpleShape;

/**
 * @author Evgeniy Berlog
 * date: 08.06.12
 */
public enum HSSFShapeType {
    NOT_PRIMITIVE((short)0x0, null, (short)0),
    RECTANGLE((short)0x1, HSSFSimpleShape.class, HSSFSimpleShape.OBJECT_TYPE_RECTANGLE),
    PICTURE((short)0x004B, HSSFPicture.class, HSSFSimpleShape.OBJECT_TYPE_PICTURE),
    ROUND_RECTANGLE((short)0x2, null, null);

    private Short type;
    private Class shape;
    private Short objectId;

    private HSSFShapeType(Short type, Class shape, Short objectId) {
        this.type = type;
        this.shape = shape;
        this.objectId = objectId;
    }

    public Short getType() {
        return type;
    }

    public Class getShape() {
        return shape;
    }
}
