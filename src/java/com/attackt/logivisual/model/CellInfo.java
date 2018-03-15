package com.attackt.logivisual.model;

public class CellInfo {
	int id;
	String sheetName;
	int sheetIndex;
	String address;
	String cellValue;

	public CellInfo(String sheetName, int sheetIndex, String address, String cellValue) {
		super();
		this.sheetName = sheetName;
		this.sheetIndex = sheetIndex;
		this.address = address;
		this.cellValue = cellValue;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public int getSheetIndex() {
		return sheetIndex;
	}

	public void setSheetIndex(int sheetIndex) {
		this.sheetIndex = sheetIndex;
	}

	public String temp_index() {
		return address;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public CellInfo() {
		super();
	}

	public String getCellValue() {
		return cellValue;
	}

	public void setCellValue(String cellValue) {
		this.cellValue = cellValue;
	}

}
