package org.apache.poi.hssf.usermodel;

import java.util.List;

/**
 * An interface that indicates whether a class can contain children.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public interface HSSFShapeContainer
{
    /**
     * @return  Any children contained by this shape.
     */
    List getChildren();

}
