package board;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import dictionary.Dictionary;
import java.util.Map;
import java.util.HashMap;

public class ScrabbleBoard {
	public static void main(String[] args) throws IOException {
		char[][] input = new char[15][15];
		Tile[][] board = new Tile[15][15];
		
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				Tile tile = new Tile();
				tile.setX(i);
				tile.setY(j);
				board[i][j] = tile;
			}
		}
		
		// key is a word on board, value is the Tile object of the starting letter of the word
		Map<String,Tile> wordsOnBoard = new HashMap<String,Tile>();
		
		BufferedReader br = new BufferedReader(new FileReader("resources/words.txt"));
		Dictionary dict = new Dictionary();
		String str;
		while ((str = br.readLine()) != null) {
			dict.insert(str);
		}
		br.close();
		
		br = new BufferedReader(new FileReader("resources/input.txt"));
		int row = 0;
		while ((str = br.readLine()) != null) {
			char[] arr = str.toCharArray();
			for (int j = 0; j < arr.length; j++) {
				input[row][j] = arr[j];
			}
			row++;
		}
		
		// load horizontal words
		/*StringBuilder sb = new StringBuilder();
		int x = -1, y = -1;
		for (int i = 0; i < input.length; i++) {
			for (int j = 0; j < input.length; j++) {
				if (input[i][j] != '.') {
					if (x == -1 && y == -1) {
						// word starts here
						x = i;
						y = j;
					}
					sb.append(input[i][j]);									
				}
				else {
					if (sb.length() > 1) {
						String word = sb.toString();
						for (int k = 0; k < word.length(); k++) {
							Tile tile = board[x][y+k];
							tile.setHorizontalWord(word);
						}
					}
					x = -1;
					y = -1;
					sb = new StringBuilder();
				}
			}
			if (sb.length() > 1) {
				String word = sb.toString();
				for (int k = 0; k < word.length(); k++) {
					Tile tile = board[x][y+k];
					tile.setHorizontalWord(word);
				}
			}
			x = -1;
			y = -1;
			sb = new StringBuilder();			
		}*/
		
	}
}

class Tile {
	private String horizontalWord, verticalWord;
	private boolean isEmpty = true;
	
	public boolean isEmpty() {
		return isEmpty;
	}
	
	public String getHorizontalWord() {
		return horizontalWord;
	}
	public void setHorizontalWord(String horizontalWord) {
		this.horizontalWord = horizontalWord;
		isEmpty = false;
	}
	public String getVerticalWord() {
		return verticalWord;
	}
	public void setVerticalWord(String verticalWord) {
		isEmpty = false;
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
