package util;

public class Word {
	private String text;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public boolean isVertical() {
		return isVertical;
	}
	public void setVertical(boolean isVertical) {
		this.isVertical = isVertical;
	}
	private int x,y;
	boolean isVertical;
	
	public String toString() {
		if (isVertical) {
			return "{"+text+",["+x+","+y+"], Vertical}";
		}
		return "{"+text+",["+x+","+y+"], Horizontal}";
	}	
	
}
