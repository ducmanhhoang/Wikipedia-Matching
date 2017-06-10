package textsimilarity;

public class Point {
	private int row;
	private int column;
	private double value;

	public Point(int row, int column, double value) {
		this.row = row;
		this.column = column;
		this.value = value;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}
}