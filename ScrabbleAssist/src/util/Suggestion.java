package util;

public class Suggestion {
	String word;
	int x,y;
	int score;
	Direction direction;

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

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Suggestion(String text, int x, int y, int score, Direction direction) {
		this.word = text;
		this.x = x;
		this.y = y;
		this.score = score;
		this.direction = direction;
	}

	public String getText() {
		return word;
	}
	public void setText(String text) {
		this.word = text;
	}

	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}


	@Override
	public int hashCode() {
		int hash = 31;
		int one = (this.word != null ? this.word.hashCode() : 0);
		int two = hash * this.x;
		int three = hash * this.y;
		return (hash * (one + two + three));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		Suggestion s = (Suggestion)obj;
		if (this.word == null) {
			if (s.word != null) {
				return false;
			}
			return this.x == s.x && this.y == s.y && this.direction == s.direction;
		}

		return this.word.equals(s.word) && this.x == s.x
				&& this.y == s.y && this.direction == s.direction;

	}

	@Override
	public String toString() {
		return "{Word: "+word+", X: "+x+", Y: "+y+", Score: "+score+", Direction: "+direction+"}";
	}

}
