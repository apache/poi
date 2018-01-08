/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.xwpf.usermodel;

import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Beta;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xddf.usermodel.chart.XDDFChart;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;

/**
 * Represents a Chart in a .docx file
 */
@Beta
public class XWPFChart extends XDDFChart {

	/**
	 * default width of chart in emu
	 */
    public static final int WIDTH 	= 500000;
    
    /**
	 * default height of chart in emu
	 */
	public static final int HEIGHT 	= 500000;
    // lazy initialization
    private Long checksum;
    /**
	 * this object is used to write embedded part of chart i.e. xlsx file in docx
	 */
    private OutputStream sheet;
    /**
     * this object is used to modify drawing properties
     */
	private CTInline ctInline;
	
	/**
	 * constructor to
	 * Create a new chart in document
	 * 
	 * @since POI 4.0
	 */
	protected XWPFChart() {
		super();
	}
    /**
     * Construct a chart from a package part.
     *
     * @param part the package part holding the chart data,
     * the content type must be <code>application/vnd.openxmlformats-officedocument.drawingml.chart+xml</code>
     *
     * @since POI 4.0
     */
    protected XWPFChart(PackagePart part) throws IOException, XmlException {
        super(part);
    }

    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTChartSpace.type.getName().getNamespaceURI(), "chartSpace", "c"));

        try (OutputStream out = getPackagePart().getOutputStream()) {
            chartSpace.save(out, xmlOptions);
        }
    }

    public Long getChecksum() {
        if (this.checksum == null) {
            InputStream is = null;
            byte[] data;
            try {
                is = getPackagePart().getInputStream();
                data = IOUtils.toByteArray(is);
            } catch (IOException e) {
                throw new POIXMLException(e);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    throw new POIXMLException(e);
                }
            }
            this.checksum = IOUtils.calculateChecksum(data);
        }
        return this.checksum;
    }

    @Override
    public boolean equals(Object obj) {
        /**
         * In case two objects ARE equal, but its not the same instance, this
         * implementation will always run through the whole
         * byte-array-comparison before returning true. If this will turn into a
         * performance issue, two possible approaches are available:<br>
         * a) Use the checksum only and take the risk that two images might have
         * the same CRC32 sum, although they are not the same.<br>
         * b) Use a second (or third) checksum algorithm to minimise the chance
         * that two images have the same checksums but are not equal (e.g.
         * CRC32, MD5 and SHA-1 checksums, additionally compare the
         * data-byte-array lengths).
         */
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof XWPFChart)) {
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getChecksum().hashCode();
    }
    
    /**
     * method to create relationship with embedded part
     * for example writing xlsx file stream into output stream
     * @param chartSheet
     * @return return relation part which used to write relation in .rels file and get relation id
     * @since POI 4.0
     */
	public RelationPart createRelationship(XWPFRelation chartRelation,int index) {
		return createRelationship(chartRelation, XWPFFactory.getInstance(), index, false);
	}
	
	/**
	 * protected method which used to initialization of sheet output stream 
	 * @param sheet
	 * @since POI 4.0
	 */
	protected void addEmbeddedWorkSheet(OutputStream sheet) {
			this.sheet=sheet;
	}
	
	/**
	 * this method is used to write workbook object in embedded part of chart
	 * return's true in case of successfully write work book in embedded part or return's false
	 * @param wb
	 * @return return's true in case of successfully write work book in embedded part or return's false
	 * @throws IOException
	 * @since POI 4.0
	 */
	public boolean writeEmbeddedWorkSheet(Workbook wb) throws IOException
	{
		if(this.sheet!=null && wb!=null)
		{
			wb.write(this.sheet);
			return true;
		}
		return false;
	}
	
	/**
	 * initialize in line object
	 * @param inline
	 * @since POI 4.0
	 */
	protected void setInLine(CTInline ctInline) {
		this.ctInline=ctInline;
	}
	
	/**
	 * set chart height 
	 * @param height
	 * @since POI 4.0
	 */
	public void setChartHeight(long height)
	{
		ctInline.getExtent().setCy(height);
	}
	
	/**
	 * set chart width 
	 * @param width
	 * @since POI 4.0
	 */
	public void setChartWidth(long width)
	{
		ctInline.getExtent().setCx(width);
	}
	/**
	 * get chart height 
	 * @since POI 4.0
	 */
	public long getChartHeight()
	{
		return ctInline.getExtent().getCy();
	}
	
	/**
	 * get chart width 
	 * @since POI 4.0
	 */
	public long getChartWidth()
	{
		return ctInline.getExtent().getCx();
	}
	
	/**
	 * set chart height and width
	 * @param width
	 * @param height 
	 * @since POI 4.0
	 */
	public void setChartWidthHeight(long width,long height)
	{
		this.setChartWidth(width);
		this.setChartHeight(height);
	}
	
	/**
	 * set margin from top
	 * @param height
	 * @since POI 4.0
	 */
	public void setChartTopMargin(long margin)
	{
		ctInline.setDistT(margin);
	}
	
	/**
	 * get margin from Top
	 * @param margin
	 * @since POI 4.0
	 */
	public long getChartTopMargin(long margin)
	{
		return ctInline.getDistT();
	}
	
	/**
	 * set margin from bottom
	 * @param height
	 * @since POI 4.0
	 */
	public void setChartBottomMargin(long margin)
	{
		ctInline.setDistB(margin);
	}
	
	/**
	 * get margin from Bottom
	 * @param margin
	 * @since POI 4.0
	 */
	public long getChartBottomMargin(long margin)
	{
		return ctInline.getDistB();
	}
	
	/**
	 * set margin from left
	 * @param margin
	 * @since POI 4.0
	 */
	public void setChartLeftMargin(long margin)
	{
		ctInline.setDistL(margin);
	}
	
	/**
	 * get margin from left
	 * @param margin
	 * @since POI 4.0
	 */
	public long getChartLeftMargin(long margin)
	{
		return ctInline.getDistL();
	}
	
	/**
	 * set margin from Right
	 * @param margin
	 * @since POI 4.0
	 */
	public void setChartRightMargin(long margin)
	{
		ctInline.setDistR(margin);
	}
	
	/**
	 * get margin from Right
	 * @param margin
	 * @since POI 4.0
	 */
	public long getChartRightMargin(long margin)
	{
		return ctInline.getDistR();
	}
	
	/**
	 * set chart margin
	 * @param top
	 * @param right
	 * @param bottom
	 * @param left 
	 * @since POI 4.0
	 */
	public void setChartMargin(long top,long right,long bottom,long left)
	{
		this.setChartBottomMargin(bottom);
		this.setChartRightMargin(right);
		this.setChartLeftMargin(left);
		this.setChartRightMargin(right);
	}
}
