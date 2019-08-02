package org.apache.poi.ss.usermodel;

import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * A read-only Row view of a sheet column
 * 
 * Intended to iterate over cells of a sheet.
 *
 * @author luca vercelli 2018
 */
public class Column implements Row {

	private int colNum;
	private Sheet sheet;

	/**
	 * Constructor. Return a read-only Row view of the given sheet column
	 * @param sheet
	 * @param colNum 0-based
	 */
	public Column(Sheet sheet, int colNum) {
		this.sheet = sheet;
		this.colNum = colNum;
	}

	protected static class ColumnIterator implements Iterator<Cell> {

		private int rowNum = 0;
		private int colNum;
		private Sheet sheet;
		private int lastRowNum;

		public ColumnIterator(Sheet sheet, int colNum) {
			this.sheet = sheet;
			this.colNum = colNum;
			this.lastRowNum = sheet.getLastRowNum();
		}

		@Override
		public boolean hasNext() {
			return rowNum < lastRowNum;
		}

		@Override
		public Cell next() {
			try {
				return sheet.getRow(rowNum++).getCell(colNum);
			} catch (NullPointerException e) {
				return null;
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("A column is a read-only object");
		}

	}

	@Override
	public Iterator<Cell> iterator() {
		return new ColumnIterator(sheet, colNum);
	}

	@Override
	public Iterator<Cell> cellIterator() {
		return iterator();
	}

	@Override
	public Cell createCell(int colnum) {
		throw new UnsupportedOperationException("A column is a read-only object");
	}

	@Override
	public Cell createCell(int arg0, int arg1) {
		throw new UnsupportedOperationException("A column is a read-only object");
	}

	@Override
	public Cell getCell(int rowNum) {
		try {
			return sheet.getRow(rowNum).getCell(colNum);
		} catch (NullPointerException e) {
			return null;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public Cell getCell(int rowNum, MissingCellPolicy policy) {
		try {
			return sheet.getRow(rowNum).getCell(colNum, policy);
		} catch (NullPointerException e) {
			return null;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public short getFirstCellNum() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public short getHeight() {
		throw new UnsupportedOperationException();
	}

	@Override
	public float getHeightInPoints() {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getLastCellNum() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public int getPhysicalNumberOfCells() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public int getRowNum() {
		return colNum;
	}

	@Override
	public CellStyle getRowStyle() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Sheet getSheet() {
		return sheet;
	}

	@Override
	public boolean getZeroHeight() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isFormatted() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeCell(Cell cellnum) {
		throw new UnsupportedOperationException("A column is a read-only object");
	}

	@Override
	public void setHeight(short height) {
		throw new UnsupportedOperationException("A column is a read-only object");
	}

	@Override
	public void setHeightInPoints(float heightInPoints) {
		throw new UnsupportedOperationException("A column is a read-only object");
	}

	@Override
	public void setRowNum(int colNum) {
		throw new UnsupportedOperationException("A column is a read-only object");
	}

	@Override
	public void setRowStyle(CellStyle rowStyle) {
		throw new UnsupportedOperationException("A column is a read-only object");
	}

	@Override
	public void setZeroHeight(boolean v) {
		throw new UnsupportedOperationException("A column is a read-only object");
	}

	/**
	 * This method (try to) understand if the given column does really exist in
	 * the worksheet. We actually only look at row 0, so the result can be
	 * wrong.
	 * 
	 * @return
	 */
	public boolean exists() {
		try {
			getCell(0);
		} catch (Exception exc) {
			return false;
		}
		return true;
	}

	/**
	 * Return the number of columns in the sheet. This implementation is slow.
	 */
	public static int numOfColumns(Sheet sheet) {
		int cols = -1;
		for (Row curRow : sheet) {
			cols = Math.max(cols, curRow.getLastCellNum());
		}
		return cols;

	}
}
