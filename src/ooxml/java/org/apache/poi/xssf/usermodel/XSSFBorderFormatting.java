package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.BorderFormatting;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;

/**
 * @author Yegor Kozlov
 */
public class XSSFBorderFormatting implements BorderFormatting  {
    CTBorder _border;

    /*package*/ XSSFBorderFormatting(CTBorder border){
        _border = border;
    }

    public short getBorderBottom(){
        STBorderStyle.Enum ptrn = _border.isSetBottom() ? _border.getBottom().getStyle() : null;
        return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
    }

    public short getBorderDiagonal(){
        STBorderStyle.Enum ptrn = _border.isSetDiagonal() ? _border.getDiagonal().getStyle() : null;
        return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
    }

    public short getBorderLeft(){
        STBorderStyle.Enum ptrn = _border.isSetLeft() ? _border.getLeft().getStyle() : null;
        return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
    }

    public short getBorderRight(){
        STBorderStyle.Enum ptrn = _border.isSetRight() ? _border.getRight().getStyle() : null;
        return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
    }

    public short getBorderTop(){
        STBorderStyle.Enum ptrn = _border.isSetTop() ? _border.getTop().getStyle() : null;
        return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
    }

    public short getBottomBorderColor(){
        if(!_border.isSetBottom()) return 0;

        CTBorderPr pr = _border.getBottom();
        return (short)pr.getColor().getIndexed();
    }

    public short getDiagonalBorderColor(){
        if(!_border.isSetDiagonal()) return 0;

        CTBorderPr pr = _border.getDiagonal();
        return (short)pr.getColor().getIndexed();
    }

    public short getLeftBorderColor(){
        if(!_border.isSetLeft()) return 0;

        CTBorderPr pr = _border.getLeft();
        return (short)pr.getColor().getIndexed();
    }

    public short getRightBorderColor(){
        if(!_border.isSetRight()) return 0;

        CTBorderPr pr = _border.getRight();
        return (short)pr.getColor().getIndexed();
    }

    public short getTopBorderColor(){
        if(!_border.isSetTop()) return 0;

        CTBorderPr pr = _border.getTop();
        return (short)pr.getColor().getIndexed();
    }

    public void setBorderBottom(short border){
        CTBorderPr pr = _border.isSetBottom() ? _border.getBottom() : _border.addNewBottom();
        if(border == BORDER_NONE) _border.unsetBottom();
        else pr.setStyle(STBorderStyle.Enum.forInt(border + 1));
    }

    public void setBorderDiagonal(short border){
        CTBorderPr pr = _border.isSetDiagonal() ? _border.getDiagonal() : _border.addNewDiagonal();
        if(border == BORDER_NONE) _border.unsetDiagonal();
        else pr.setStyle(STBorderStyle.Enum.forInt(border + 1));
    }

    public void setBorderLeft(short border){
        CTBorderPr pr = _border.isSetLeft() ? _border.getLeft() : _border.addNewLeft();
        if(border == BORDER_NONE) _border.unsetLeft();
        else pr.setStyle(STBorderStyle.Enum.forInt(border + 1));
    }

    public void setBorderRight(short border){
        CTBorderPr pr = _border.isSetRight() ? _border.getRight() : _border.addNewRight();
        if(border == BORDER_NONE) _border.unsetRight();
        else pr.setStyle(STBorderStyle.Enum.forInt(border + 1));
    }

    public void setBorderTop(short border){
        CTBorderPr pr = _border.isSetTop() ? _border.getTop() : _border.addNewTop();
        if(border == BORDER_NONE) _border.unsetTop();
        else pr.setStyle(STBorderStyle.Enum.forInt(border + 1));
    }

    public void setBottomBorderColor(short color){
        CTBorderPr pr = _border.isSetBottom() ? _border.getBottom() : _border.addNewBottom();

        CTColor ctColor = CTColor.Factory.newInstance();
        ctColor.setIndexed(color);
        pr.setColor(ctColor);
    }

    public void setDiagonalBorderColor(short color){
        CTBorderPr pr = _border.isSetDiagonal() ? _border.getDiagonal() : _border.addNewDiagonal();

        CTColor ctColor = CTColor.Factory.newInstance();
        ctColor.setIndexed(color);
        pr.setColor(ctColor);
    }

    public void setLeftBorderColor(short color){
        CTBorderPr pr = _border.isSetLeft() ? _border.getLeft() : _border.addNewLeft();

        CTColor ctColor = CTColor.Factory.newInstance();
        ctColor.setIndexed(color);
        pr.setColor(ctColor);
    }

    public void setRightBorderColor(short color){
        CTBorderPr pr = _border.isSetRight() ? _border.getRight() : _border.addNewRight();

        CTColor ctColor = CTColor.Factory.newInstance();
        ctColor.setIndexed(color);
        pr.setColor(ctColor);
    }

    public void setTopBorderColor(short color){
        CTBorderPr pr = _border.isSetTop() ? _border.getTop() : _border.addNewTop();

        CTColor ctColor = CTColor.Factory.newInstance();
        ctColor.setIndexed(color);
        pr.setColor(ctColor);
    }
    
}
