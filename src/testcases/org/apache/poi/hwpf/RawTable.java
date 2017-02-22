package org.apache.poi.hwpf;

import java.util.ArrayList;

public class RawTable {

    private ArrayList<ArrayList<CellShape>> m_table;
    private String m_title;

    RawTable() {
        m_table = new ArrayList<ArrayList<CellShape>>();
    }

    public ArrayList<ArrayList<CellShape>> getData() {
        return m_table;
    }

    public String getTitle() {
        return m_title;
    }

    public void setTitle(String title) {
        m_title = title;
    }

    public void add(ArrayList<CellShape> row) {
        if (m_table.size() != 0) {
            ArrayList<CellShape> rowNew = new ArrayList<CellShape>();
            int m = m_table.get(m_table.size() - 1).size();
            int k = row.size();
            int j;
            if (m == k) {
                m_table.add(row);
                return;
            }
            for (int i = 0; i < m; i++) {
                int left = m_table.get(m_table.size() - 1).get(i).getLeft();
                for (j = 0; j < k; j++) {
                    if (left == row.get(j).getLeft() && left != -1 && Math.abs(i - j) < 2) {
                        rowNew.add(row.get(j));
                        break;
                    }
                }
                if (j == k && left != -1) {
                    rowNew.add(new CellShape("", left, -1, -1));
                }
            }
            if (rowNew.size() > 0) m_table.add(rowNew);
            else m_table.add(row);
        } else {
            m_table.add(row);
        }
    }

    public void clear() {
        for (ArrayList<CellShape> as : m_table) {
            as.clear();
        }
        m_table.clear();
    }
}