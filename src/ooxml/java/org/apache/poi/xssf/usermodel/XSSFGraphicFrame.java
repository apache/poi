/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xssf.usermodel;

import javax.xml.namespace.QName;

import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTGraphicalObjectFrameNonVisual;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObjectData;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.officeDocument.x2006.relationships.STRelationshipId;

/**
 * Represents DrawingML GraphicalObjectFrame.
 *
 * @author Roman Kashitsyn
 */
public final class XSSFGraphicFrame {

	private static CTGraphicalObjectFrame prototype = null;

	private CTGraphicalObjectFrame graphicFrame;
	private XSSFDrawing drawing;
	private XSSFClientAnchor anchor;

	/**
	 * Construct a new XSSFGraphicFrame object.
	 *
	 * @param drawing the XSSFDrawing that owns this frame
	 * @param ctGraphicFrame the XML bean that stores this frame content
	 */
	protected XSSFGraphicFrame(XSSFDrawing drawing, CTGraphicalObjectFrame ctGraphicFrame) {
		this.drawing = drawing;
		this.graphicFrame = ctGraphicFrame;
	}

	@Internal
	public CTGraphicalObjectFrame getCTGraphicalObjectFrame() {
		return graphicFrame;
	}

	/**
	 * Initialize default structure of a new graphic frame
	 */
	protected static CTGraphicalObjectFrame prototype() {
		if (prototype == null) {
			CTGraphicalObjectFrame graphicFrame = CTGraphicalObjectFrame.Factory.newInstance();

			CTGraphicalObjectFrameNonVisual nvGraphic = graphicFrame.addNewNvGraphicFramePr();
			CTNonVisualDrawingProps props = nvGraphic.addNewCNvPr();
			props.setId(0);
			props.setName("Diagramm 1");
			nvGraphic.addNewCNvGraphicFramePr();

			CTTransform2D transform = graphicFrame.addNewXfrm();
			CTPositiveSize2D extPoint = transform.addNewExt();
			CTPoint2D offPoint = transform.addNewOff();

			extPoint.setCx(0);
			extPoint.setCy(0);
			offPoint.setX(0);
			offPoint.setY(0);

			CTGraphicalObject graphic = graphicFrame.addNewGraphic();

			prototype = graphicFrame;
		}
		return prototype;
	}

	/**
	 * Sets the frame macro.
	 */
	public void setMacro(String macro) {
		graphicFrame.setMacro(macro);
	}

	/**
	 * Sets the frame name.
	 */
	public void setName(String name) {
		getNonVisualProperties().setName(name);
	}

	/**
	 * Returns the frame name.
	 * @return name of the frame
	 */
	public String getName() {
		return getNonVisualProperties().getName();
	}

	private CTNonVisualDrawingProps getNonVisualProperties() {
		CTGraphicalObjectFrameNonVisual nvGraphic = graphicFrame.getNvGraphicFramePr();
		return nvGraphic.getCNvPr();
	}

	/**
	 * Attaches frame to an anchor.
	 */
	protected void setAnchor(XSSFClientAnchor anchor) {
		this.anchor = anchor;
	}

	/**
	 * Returns the frame anchor.
	 * @return the anchor this frame is attached to
	 */
	public XSSFClientAnchor getAnchor() {
		return anchor;
	}

	/**
	 * Assign a DrawingML chart to the graphic frame.
	 */
	protected void setChart(XSSFChart chart, String relId) {
		CTGraphicalObjectData data = graphicFrame.getGraphic().addNewGraphicData();
		appendChartElement(data, relId);
		chart.setGraphicFrame(this);
		return;
	}

	/**
	 * Gets the frame id.
	 */
	public long getId() {
		return graphicFrame.getNvGraphicFramePr().getCNvPr().getId();
	}

	/**
	 * Sets the frame id.
	 */
	protected void setId(long id) {
		graphicFrame.getNvGraphicFramePr().getCNvPr().setId(id);
	}

	/**
	 * The low level code to insert {@code <c:chart>} tag into
	 * {@code<a:graphicData>}.
	 *
	 * Here is the schema (ECMA-376):
	 * <pre>
	 * {@code
	 * <complexType name="CT_GraphicalObjectData">
	 *   <sequence>
	 *     <any minOccurs="0" maxOccurs="unbounded" processContents="strict"/>
	 *   </sequence>
	 *   <attribute name="uri" type="xsd:token"/>
	 * </complexType>
	 * }
	 * </pre>
	 */
	private void appendChartElement(CTGraphicalObjectData data, String id) {
		String r_namespaceUri = STRelationshipId.type.getName().getNamespaceURI();
		String c_namespaceUri = XSSFDrawing.NAMESPACE_C;
		XmlCursor cursor = data.newCursor();
		cursor.toNextToken();
		cursor.beginElement(new QName(c_namespaceUri, "chart", "c"));
		cursor.insertAttributeWithValue(new QName(r_namespaceUri, "id", "r"), id);
		cursor.dispose();
		data.setUri(c_namespaceUri);
	}

}
