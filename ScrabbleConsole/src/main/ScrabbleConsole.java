package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import dictionary.Dictionary;
import util.Direction;
import util.Suggestion;
import util.Word;
import wordsuggester.WordSuggester;

//TODO: Keep a collection of words in a set to avoid duplicate words on board.
public class ScrabbleConsole {
	
	static final char EMPTY_TILE = '.';
	static final Logger Log = Logger.getLogger(ScrabbleConsole.class);
	static final String INPUT = "cursayi";
	static WordSuggester wordSuggester;
	
	public static void main(String[] args) throws IOException {
		char[][] board = new char[15][15];
		// Build the Trie
		BufferedReader br = new BufferedReader(new FileReader("resources/words.txt"));
		String str;
		Dictionary dict = new Dictionary();
		while ((str = br.readLine()) != null) {
			dict.insert(str);
		}
		br.close();
		wordSuggester = new WordSuggester(dict);
		
		Set<Word> wordsSet = new HashSet<Word>();
		board = readBoardFromInputFile("resources/input.txt");
		// load horizontal words
		Set<Word> horizontalWords = getWordsFromBoard(board, true);			
		wordsSet.addAll(horizontalWords);		
		// load vertical words
		Set<Word> verticalWords = getWordsFromBoard(board, false);
		wordsSet.addAll(verticalWords);		
		
		Log.info("Found Horizontal And Vertical Words");
		
		for (Word word : wordsSet) {
			System.out.println(word);
		}
		Set<Suggestion> set = new HashSet<Suggestion>();		
		set = wordSuggester.findWordWithBestPossibleScore(wordsSet, board, INPUT);
		int i = 1;
		for (Suggestion s : set) {
			System.out.println(i+". "+s);
			i++;
		}
	}
	private static char[][] readBoardFromInputFile(String filename) throws IOException {
		char[][] input = new char[15][15];
		BufferedReader br = new BufferedReader(new FileReader(filename));
		int row = 0;
		String str;
		while ((str = br.readLine()) != null && row < 15) {
			char[] arr = str.toLowerCase().toCharArray();
			for (int j = 0; j < arr.length; j++) {
				input[row][j] = arr[j];
			}
			row++;
		}
		br.close();
		return input;
	}
	
	private static Set<Word> getWordsFromBoard(char[][]board, boolean isHorizontal) {
		Set<Word> wordSet = new HashSet<Word>();
		StringBuilder sb = new StringBuilder();
		int x = -1, y = -1;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				char ch;
				if (isHorizontal) {
					ch = board[i][j];
				}
				else {
					ch = board[j][i];
				}
				
				// tile is not empty
				if (ch != '.') {
					if (x == -1 && y == -1) {
						// word starts here
						x = i;
						y = j;
					}
					sb.append(ch);				
				}
				else {
					if (sb.length() > 1) {
						String text = sb.toString();
						Word word = new Word();
						word.setText(text);
						if (isHorizontal) {
							word.setX(x);
							word.setY(y);
							word.setDirection(Direction.HORIZONTAL);
						}
						else {
							word.setX(y);
							word.setY(x);
							word.setDirection(Direction.VERTICAL);
						}
						wordSet.add(word);
					}	
					x = -1;
					y = -1;
					sb = new StringBuilder();
				}
			}
			if (sb.length() > 1) {
				String text = sb.toString();
				Word word = new Word();
				word.setText(text);
				if (isHorizontal) {
					word.setX(x);
					word.setY(y);
					word.setDirection(Direction.HORIZONTAL);
				}
				else {
					word.setX(y);
					word.setY(x);
					word.setDirection(Direction.VERTICAL);
				}
				wordSet.add(word);
			}
			x = -1;
			y = -1;
			sb = new StringBuilder();
		}
		return wordSet;
	}
}
