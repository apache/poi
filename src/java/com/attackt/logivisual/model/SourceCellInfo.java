package com.attackt.logivisual.model;

import java.util.List;

public class SourceCellInfo {
	private String id;
	private String cellContent;
	private String sheetName;
	private String cellLocation;
	private List<FormulaSplitInfo> splitInfo;

	public SourceCellInfo() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCellContent() {
		return cellContent;
	}

	public void setCellContent(String cellContent) {
		this.cellContent = cellContent;
	}

	public List<FormulaSplitInfo> getSplitInfo() {
		return splitInfo;
	}

	public void setSplitInfo(List<FormulaSplitInfo> splitInfo) {
		this.splitInfo = splitInfo;
	}

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public SourceCellInfo(String id, String cellContent, String sheetName) {
		super();
		this.id = id;
		this.cellContent = cellContent;
		this.sheetName = sheetName;
	}

	public SourceCellInfo(String cellContent, String sheetName) {
		super();
		this.cellContent = cellContent;
		this.sheetName = sheetName;
	}

	public SourceCellInfo(String cellContent, String sheetName, List<FormulaSplitInfo> splitInfo) {
		super();
		this.cellContent = cellContent;
		this.sheetName = sheetName;
		this.splitInfo = splitInfo;
	}

	public SourceCellInfo(String cellContent, String sheetName, List<FormulaSplitInfo> splitInfo, String cellLocation) {
		super();
		this.cellContent = cellContent;
		this.sheetName = sheetName;
		this.cellLocation = cellLocation;
		this.splitInfo = splitInfo;
	}

	public String getCellLocation() {
		return cellLocation;
	}

	public void setCellLocation(String cellLocation) {
		this.cellLocation = cellLocation;
	}

	public SourceCellInfo(String id, String cellContent, String sheetName, String cellLocation,
			List<FormulaSplitInfo> splitInfo) {
		super();
		this.id = id;
		this.cellContent = cellContent;
		this.sheetName = sheetName;
		this.cellLocation = cellLocation;
		this.splitInfo = splitInfo;
	}

}
