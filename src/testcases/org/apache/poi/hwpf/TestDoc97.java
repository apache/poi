package org.apache.poi.hwpf;

import org.apache.poi.hwpf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.util.ArrayList;

public class TestDoc97 {
    private static ArrayList<RawTable> m_RawTables = new ArrayList<RawTable>();

    private static String docTableCellHyperlinkText(String cellText) {
        return cellText.replaceAll("HYPERLINK \"mailto:[^@]+@{1}[^@^\"]+\"{1}",
                "");
    }

    private static String formatToSpace(String s) {
        byte[] bs = s.getBytes();
        for (int i = 0; i < bs.length; ++i) {
            if (bs[i] < 32 && bs[i] > 0)
                bs[i] = 32;
        }
        return new String(bs);
    }

    public static void main(String[] args) {
        POIFSFileSystem pfs;
//			byte[]parray=new byte[64];			 
//	    	 MessageDigest temp=null;
//	    	 try {
//			    temp = MessageDigest.getInstance("MD5");
//			 } catch (NoSuchAlgorithmException e) {
//			 }
//	    	// System.out.println("hhh"+temp.digest()[0]);
//	    	 temp.update("123456".getBytes());
//	    	 byte []yy=temp.digest();
//	    	 for(int i=0;i<yy.length;i++)
//	    	 {
//	    		 //System.out.print(byteHEX(yy[i]));
//	    	 }
        HWPFDocument hwpf = null;
        try {
            pfs = new POIFSFileSystem(new FileInputStream("20030523jm.doc"));
            hwpf = new HWPFDocument(pfs, "111111");
        } catch (Exception ignored) {
        }
        assert hwpf != null;
        Range range = hwpf.getOverallRange();
        TableIterator it = new TableIterator(range);
        while (it.hasNext()) {
            RawTable rawTable = new RawTable();
            Table tb = (Table) it.next();
            for (int i = 0; i < tb.numRows(); i++) {
                ArrayList<CellShape> r = new ArrayList<CellShape>();
                TableRow tr = tb.getRow(i);
                for (int j = 0; j < tr.numCells(); j++) {
                    TableCell tc = tr.getCell(j);
                    StringBuilder sb = new StringBuilder();
                    for (int k = 0; k < tc.numParagraphs(); k++) {
                        Paragraph para = tc.getParagraph(k);
                        sb.append(docTableCellHyperlinkText(
                                formatToSpace(para.text())).trim()).append(" ");
                        System.out.println(sb.toString());
                    }
                    r.add(new CellShape(sb.toString().trim(), tc.getLeftEdge(), -1, -1));
                }
                rawTable.add(r);
                System.out.println("");
            }
            m_RawTables.add(rawTable);
        }
    }
}

