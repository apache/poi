package org.apache.poi.hwpf;

public class CellShape {

    private String m_text;
    private int m_left;
    private int m_right;
    private int m_colm;

    public CellShape() {
        m_text = "";
        m_left = -1;
        m_right = -1;
        m_colm = -1;
    }

    public CellShape(String text) {
        m_text = text;
        m_left = -1;
        m_right = -1;
        m_colm = -1;
    }

    CellShape(String text, int left, int right, int col) {
        m_text = text;
        m_left = left;
        m_right = right;
        m_colm = col;
    }

    public int getColm() {
        return m_colm;
    }

    public String getText() {
        return m_text;
    }

    public void setText(String text) {
        m_text = text;
    }

    public int getLeft() {
        return m_left;
    }

    public void setLeft(int left) {
        m_left = left;
    }

    public int getRight() {
        return m_right;
    }

    public void setRight(int right) {
        m_right = right;
    }
}