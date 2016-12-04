package util;

public class Word {
	private String text;
	private int x,y;
	private Direction direction;

	public Word(String text, int startX, int startY, Direction direction) {
		this.text = text;
		this.x = startX;
		this.y = startY;
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
