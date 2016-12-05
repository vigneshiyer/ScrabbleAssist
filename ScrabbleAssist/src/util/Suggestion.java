package util;

public class Suggestion {
	String text;
	int startX,startY;
	int score;
	boolean isVertical;
	
	public Suggestion(String text, int startX, int startY, int score, boolean isVertical) {
		this.text = text;
		this.startX = startX;
		this.startY = startY;
		this.score = score;
		this.isVertical = isVertical;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getStartX() {
		return startX;
	}
	public void setStartX(int startX) {
		this.startX = startX;
	}
	public int getStartY() {
		return startY;
	}
	public void setStartY(int startY) {
		this.startY = startY;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public boolean isVertical() {
		return isVertical;
	}
	public void setVertical(boolean isVertical) {
		this.isVertical = isVertical;
	}
	
	@Override
	public int hashCode() {
		int hash = 31;
		int one = (this.text != null ? this.text.hashCode() : 0);
		int two = hash * this.startX;
		int three = hash * this.startY;
		return (hash * (one + two + three));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		Suggestion s = (Suggestion)obj;
		if (this.text == null) {
			if (s.text != null) {
				return false;
			}
			return this.startX == s.startX && this.startY == s.startY && this.isVertical == s.isVertical;
		}
		
		return this.text.equals(s.text) && this.startX == s.startX
				&& this.startY == s.startY && this.isVertical == s.isVertical;
		
	}
	
	@Override
	public String toString() {
		if (isVertical) {
			return "{Word: "+text+", X: "+startX+", Y: "+startY+", Score: "+score+", Direction: Vertical}";
		}
		return "{Word: "+text+", X: "+startX+", Y: "+startY+", Score: "+score+", Direction: Horizontal}";
	}
	
}
