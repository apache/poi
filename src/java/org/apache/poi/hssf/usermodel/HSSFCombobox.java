package org.apache.poi.hssf.usermodel;

import org.apache.poi.ddf.*;
import org.apache.poi.hssf.record.*;

/**
 * @author Evgeniy Berlog
 * @date 12.07.12
 */
public class HSSFCombobox extends HSSFSimpleShape {

    public HSSFCombobox(EscherContainerRecord spContainer, ObjRecord objRecord) {
        super(spContainer, objRecord);
    }

    public HSSFCombobox(HSSFShape parent, HSSFAnchor anchor) {
        super(parent, anchor);
        super.setShapeType(OBJECT_TYPE_COMBO_BOX);
    }

    @Override
    protected EscherContainerRecord createSpContainer() {
        EscherContainerRecord spContainer = new EscherContainerRecord();
        EscherSpRecord sp = new EscherSpRecord();
        EscherOptRecord opt = new EscherOptRecord();
        EscherClientDataRecord clientData = new EscherClientDataRecord();

        spContainer.setRecordId(EscherContainerRecord.SP_CONTAINER);
        spContainer.setOptions((short) 0x000F);
        sp.setRecordId(EscherSpRecord.RECORD_ID);
        sp.setOptions((short) ((EscherAggregate.ST_HOSTCONTROL << 4) | 0x2));

        sp.setFlags(EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_HASSHAPETYPE);
        opt.setRecordId(EscherOptRecord.RECORD_ID);
        opt.addEscherProperty(new EscherBoolProperty(EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, 17039620));
        opt.addEscherProperty(new EscherBoolProperty(EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 0x00080008));
        opt.addEscherProperty(new EscherBoolProperty(EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x00080000));
        opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.GROUPSHAPE__PRINT, 0x00020000));

        HSSFClientAnchor userAnchor = (HSSFClientAnchor) getAnchor();
        userAnchor.setAnchorType(1);
        EscherRecord anchor = userAnchor.getEscherAnchor();
        clientData.setRecordId(EscherClientDataRecord.RECORD_ID);
        clientData.setOptions((short) 0x0000);

        spContainer.addChildRecord(sp);
        spContainer.addChildRecord(opt);
        spContainer.addChildRecord(anchor);
        spContainer.addChildRecord(clientData);

        return spContainer;
    }

    @Override
    protected ObjRecord createObjRecord() {
        ObjRecord obj = new ObjRecord();
        CommonObjectDataSubRecord c = new CommonObjectDataSubRecord();
        c.setObjectType(HSSFSimpleShape.OBJECT_TYPE_COMBO_BOX);
        c.setLocked(true);
        c.setPrintable(false);
        c.setAutofill(true);
        c.setAutoline(false);
        FtCblsSubRecord f = new FtCblsSubRecord();
        LbsDataSubRecord l = LbsDataSubRecord.newAutoFilterInstance();
        EndSubRecord e = new EndSubRecord();
        obj.addSubRecord(c);
        obj.addSubRecord(f);
        obj.addSubRecord(l);
        obj.addSubRecord(e);
        return obj;
    }

    @Override
    public void setShapeType(int shapeType) {
        throw new IllegalStateException("Shape type can not be changed in "+this.getClass().getSimpleName());
    }
}
