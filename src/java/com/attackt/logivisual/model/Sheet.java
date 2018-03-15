package com.attackt.logivisual.model;

import java.util.List;

public class Sheet {
	int index;
	String name;
	List<FormulaCell> formulacells;
	
	public Sheet() {
		
	}
	
	public Sheet(int index, String name) {
		super();
		this.index = index;
		this.name = name;
	}

	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<FormulaCell> getFormulacells() {
		return formulacells;
	}
	public void setFormulacells(List<FormulaCell> formulacells) {
		this.formulacells = formulacells;
	}
	
}
