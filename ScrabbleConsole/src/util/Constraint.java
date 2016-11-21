package util;

public class Constraint {
	String text;
	int startX;
	int startY;
	Direction direction;
	public Direction getDirection() {
		return direction;
	}
	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getX() {
		return startX;
	}
	public void setX(int x) {
		startX = x;
	}
	public int getY() {
		return startY;
	}
	public void setY(int y) {
		startY = y;
	}
	public Constraint(String text, int startX, int startY, Direction direction) {
		this.text = text;
		this.startX = startX;
		this.startY = startY;
		this.direction = direction;
	}
	public String toString() {
		return "{Text = "+text+";X = "+startX+";Y = "+startY+"}";
	}
	
}
