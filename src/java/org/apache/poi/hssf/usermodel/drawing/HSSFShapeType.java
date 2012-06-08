package org.apache.poi.hssf.usermodel.drawing;

import org.apache.poi.hssf.usermodel.HSSFRectangle;

/**
 * @author Evgeniy Berlog
 * date: 08.06.12
 */
public enum HSSFShapeType {
    NOT_PRIMITIVE(0x0, null),
    RECTANGLE(0x1, HSSFRectangle.class),
    ROUND_RECTANGLE(0x2, null);

    private Short type;
    private Class shape;

    HSSFShapeType(Integer type, Class shape) {
        this.type = type.shortValue();
        this.shape = shape;
    }

    public Short getType() {
        return type;
    }

    public Class getShape() {
        return shape;
    }
}
