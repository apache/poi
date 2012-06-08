package org.apache.poi.hssf.usermodel;

import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.hssf.record.ObjRecord;

/**
 * @author Evgeniy Berlog
 * @date 08.06.12
 */
public class HSSFRectangle extends HSSFShape{

    public HSSFRectangle(EscherContainerRecord spContainer, ObjRecord objRecord) {
        super(spContainer, objRecord);
    }
}
