package org.apache.poi.xssf.usermodel;

import java.util.EnumMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.DifferentialStyleProvider;
import org.apache.poi.ss.usermodel.TableStyle;
import org.apache.poi.ss.usermodel.TableStyleType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxfs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleElement;

/**
 * {@link TableStyle} implementation for styles defined in the OOXML styles.xml.
 * Also used for built-in styles via dummy XML generated from presetTableStyles.xml.
 */
public class XSSFTableStyle implements TableStyle {

    private final String name;
    private final Map<TableStyleType, DifferentialStyleProvider> elementMap = new EnumMap<TableStyleType, DifferentialStyleProvider>(TableStyleType.class);

    /**
     * @param dxfs
     * @param tableStyle
     */
    public XSSFTableStyle(CTDxfs dxfs, CTTableStyle tableStyle) {
        this.name = tableStyle.getName();
        for (CTTableStyleElement element : tableStyle.getTableStyleElementList()) {
            TableStyleType type = TableStyleType.valueOf(element.getType().toString());
            DifferentialStyleProvider dstyle = null;
            if (element.isSetDxfId()) {
                int idx = (int) element.getDxfId() -1;
                CTDxf dxf;
                if (idx >= 0 && idx < dxfs.getCount()) {
                    dxf = dxfs.getDxfArray(idx);
                } else {
                    dxf = null;
                }
                if (dxf != null) dstyle = new XSSFDxfStyleProvider(dxf);
            }
            elementMap.put(type, dstyle);
        }
    }
    
    public String getName() {
        return name;
    }

    public DifferentialStyleProvider getStyle(TableStyleType type) {
        return elementMap.get(type);
    }
    
}