package org.apache.poi.xdgf.usermodel.section.geometry;

import com.microsoft.schemas.office.visio.x2012.main.CellType;
import com.microsoft.schemas.office.visio.x2012.main.RowType;
import com.microsoft.schemas.office.visio.x2012.main.SectionType;
import org.apache.poi.xdgf.usermodel.section.GeometrySection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestGeometry {
    @Test
    void testGeometryMock() {
        //test is designed to force extra classes to be added to poi-ooxml-lite
        SectionType sectionType = mock(SectionType.class);
        RowType rowType = mock(RowType.class);

        when(sectionType.getCellArray()).thenReturn(new CellType[0]);
        when(sectionType.getRowArray()).thenReturn(new RowType[] {
                rowType
        });
        when(rowType.getIX()).thenReturn(0L);
        when(rowType.getT()).thenReturn("ArcTo");
        when(rowType.getCellArray()).thenReturn(new CellType[0]);

        GeometrySection section = new GeometrySection(sectionType, null);
        assertNotNull(section);
    }
}
