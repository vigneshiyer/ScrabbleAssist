package board;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import dictionary.Dictionary;

public class ScrabbleBoard {
	public static void main(String[] args) throws IOException {
		char[][] board = new char[15][15];
		//Tile[][] board = new Tile[15][15];
		BufferedReader br = new BufferedReader(new FileReader("resources/words.txt"));
		Dictionary dict = new Dictionary();
		String str;
		while ((str = br.readLine()) != null) {
			dict.insert(str);
		}
		br.close();
	}
}

class Tile {
	private String horizontalWord, verticalWord;
	public String getHorizontalWord() {
		return horizontalWord;
	}
	public void setHorizontalWord(String horizontalWord) {
		this.horizontalWord = horizontalWord;
	}
	public String getVerticalWord() {
		return verticalWord;
	}
	public void setVerticalWord(String verticalWord) {
		this.verticalWord = verticalWord;
	}
	public int getPoint() {
		return point;
	}
	public void setPoint(int point) {
		this.point = point;
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
	public char getLetter() {
		return letter;
	}
	public void setLetter(char letter) {
		this.letter = letter;
	}
	private int point = 0;
	private int x,y;
	private char letter;	
}
