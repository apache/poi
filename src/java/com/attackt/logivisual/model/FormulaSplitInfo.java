package com.attackt.logivisual.model;

/**
 * 保存单元格信息
 * @author attackt
 *
 */
public class FormulaSplitInfo{
	//id值
	private String id;
	//显示值
	private String showValue;
	//类型 （1为普通，2为数组, 3为引用单元格,4别名,5为跨sheet数组，6excel原生数组, 7为LOOKUPCell）
	private int typeNumber;
	// 所属sheet
	private String sheet;
	//实际值
	private String  factValue;
	public FormulaSplitInfo() {
		super();
	}
	public FormulaSplitInfo(String id, String showValue, int typeNumber, String sheet, String factValue) {
		super();
		this.id = id;
		this.showValue = showValue;
		this.typeNumber = typeNumber;
		this.sheet = sheet;
		this.factValue = factValue;
	}
	public FormulaSplitInfo(int typeNumber, String sheet) {
		super();
		this.typeNumber = typeNumber;
		this.sheet = sheet;
	}
	
	public FormulaSplitInfo(String showValue, String factValue, int typeNumber, String sheet) {
		super();
		this.showValue = showValue;
		this.typeNumber = typeNumber;
		this.sheet = sheet;
		this.factValue = factValue;
	}
	public FormulaSplitInfo(String id,String showValue, String factValue, int typeNumber, String sheet) {
		super();
		this.id=id;
		this.showValue = showValue;
		this.typeNumber = typeNumber;
		this.sheet = sheet;
		this.factValue = factValue;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getShowValue() {
		return showValue;
	}
	public void setShowValue(String showValue) {
		this.showValue = showValue;
	}
	public int getTypeNumber() {
		return typeNumber;
	}
	public void setTypeNumber(int typeNumber) {
		this.typeNumber = typeNumber;
	}
	public String getSheet() {
		return sheet;
	}
	public void setSheet(String sheet) {
		this.sheet = sheet;
	}
	public String getFactValue() {
		return factValue;
	}
	public void setFactValue(String factValue) {
		this.factValue = factValue;
	}
	
    	
}