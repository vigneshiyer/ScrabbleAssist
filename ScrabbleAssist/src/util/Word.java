package util;

public class Word {
	private String text;
	private int x,y;
	private Direction direction;

	public void setText(String text) {
		this.text = text;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public String getText() {
		return text;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public Direction getDirection() {
		return direction;
	}
	@Override
	public String toString() {
		if (direction == Direction.VERTICAL) {
			return "{"+text+",["+x+","+y+"], Vertical}";
		}
		return "{"+text+",["+x+","+y+"], Horizontal}";
	}
}
