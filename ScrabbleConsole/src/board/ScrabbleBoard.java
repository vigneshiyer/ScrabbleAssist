package board;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import dictionary.Dictionary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import util.Suggestion;
import util.Word;

import org.apache.log4j.Logger;

public class ScrabbleBoard {
	
	static Dictionary dict = new Dictionary();
	static final char EMPTY_TILE = '.';
	static final Logger Log = Logger.getLogger(ScrabbleBoard.class);
		
	public static void main(String[] args) throws IOException {
		char[][] board = new char[15][15];
		// Build the Trie
		BufferedReader br = new BufferedReader(new FileReader("resources/words.txt"));
		String str;
		while ((str = br.readLine()) != null) {
			dict.insert(str);
		}
		br.close();
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
		set = getSuggestions(wordsSet, board, "sayingr");
		int i = 1;
		for (Suggestion s : set) {
			System.out.println(i+". "+s);
			i++;
		}
	}
	
	// check 1
	private static Set<Suggestion> getPossibleWordsUsingOneLetterOfTheWord (char[][] board, int X, int Y, String word,
			String availableLetters, boolean isVertical, boolean isHorizontalCalled, Set<Suggestion> set) {
			if (!isVertical) {
				char[][] transpose = new char[15][15];
				for (int i = 0; i < transpose.length; i++) {
					for (int j = 0; j < transpose.length; j++) {
						transpose[i][j] = board[j][i];
					}
				}			
				return getPossibleWordsUsingOneLetterOfTheWord(transpose,Y,X,word,availableLetters,true,true,set);
			}		
			char[] arr = word.toCharArray();
			int posY = Y;
			int posX = X;
			for (int i = 0; i < arr.length; i++) {
				if (posY >= 1 && board[posX][posY-1] != EMPTY_TILE) {
					posX++;
					continue;
				}
				if (posX < board.length-1 && board[posX][posY+1] != EMPTY_TILE) {
					posX++;
					continue;
				}
				int size = availableLetters.length();
				for (int j = 0; j < size; j++) {					
					for (int k = 0; k < size - j + 1; k++) {
						StringBuilder constraint = new StringBuilder();
						constraint.append(arr[i]);
						int count = 0;
						int left = Y;						
						// prefix						
						while (count < j && left >= 1) {
							if (board[posX][left-1] == EMPTY_TILE) {
								count++;
							}
							constraint.insert(0, board[posX][left-1]);
							left--;
						}
						// if there is letter touching the new tile
						while (left >= 1 && board[posX][left-1] != EMPTY_TILE) {
							constraint.insert(0, board[posX][left-1]);
							left--;
						}											
						count = 0;
						int right = Y;						
						//suffix
						while (count < k && right < board.length - 1) {
							if (board[posX][right+1] == EMPTY_TILE) {
								count++;
							}
							constraint.append(board[posX][right+1]);
							right++;
						}						
						// if there is letter touching the new tile
						while (right < board.length - 1 && board[posX][right+1] != EMPTY_TILE) {
							constraint.append(board[posX][right+1]);
							right++;
						}						
						// constraint is built
						Log.debug(posX + "," + left+" "+constraint.toString());
						
						Set<String> words = dict.getPossibleDictionaryWords(availableLetters,constraint.toString());
						
						for (String w : words) {
							// verify vertical words if they are valid
							boolean isValid = true;
							for (int l = 0; l < w.length(); l++) {
								
								// skip the current column
								/*if (Y == startY+l+1) {
									continue;
								}*/
								
								// new word formed is horizontal if the current word was vertical.
								// so check if any new tiles are forming vertical words
								StringBuilder sb = new StringBuilder();
								sb.append(w.charAt(l));
								int xindex = posX;
								while (xindex >= 1 && board[xindex-1][left+l] != EMPTY_TILE) {
									sb.insert(0, board[xindex-1][left+l]);
									xindex--;
								}
								xindex = posX;
								while (xindex < board.length - 1 && board[xindex+1][left+l] != EMPTY_TILE) {
									sb.append(board[xindex+1][left+l]);
									xindex++;
								}
								if (sb.length() > 1) {
									if (!dict.search(sb.toString())) {
										Log.debug("For word "+w+", vertical word is "+sb.toString()+": INVALID");
										isValid = false;
										break;
									}
									else {
										Log.debug("For word "+w+", vertical word is "+sb.toString()+": VALID");
									}
								}
							}
							if (isValid) {
								//Log.debug(w);
								Log.debug("Valid word "+ w);
								Suggestion s;
								if (isHorizontalCalled) {
									// this function was called with the current word as horizontal, so the 
									// new word formed must be vertical : true
									s = new Suggestion(w, left, posX, 0, true);
								}
								else {
									// this function was called with the current word as vertical, so the 
									// new word formed must be horizontal : false
									s = new Suggestion(w, posX, left, 0, false);
								}
								
								if (!set.contains(s)) {
									set.add(s);
								}
							}
							else {
								//Log.info("Invalid word "+ w);
								Log.debug("Invalid word "+ w);
							}
							//System.out.println(w);
						}
						if (right > board.length) {
							break;
						}
					}
				}
				posX++;				
			}
			return set;
	}
	
	//check 2
	public static Set<Suggestion> getPossibleWordsUsingEntireGivenWord(char[][] board, int X, int Y, String word, String availableLetters,
			boolean isVertical, boolean isHorizontalCalled, Set<Suggestion> set) {
		
		if (!isVertical) {
			char[][] transpose = new char[15][15];
			for (int i = 0; i < transpose.length; i++) {
				for (int j = 0; j < transpose.length; j++) {
					transpose[i][j] = board[j][i];
				}
			}			
			return getPossibleWordsUsingEntireGivenWord(transpose,Y,X,word,availableLetters,true,true,set);
			
		}
		int size = availableLetters.length();
		
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size - i + 1; j++) {
				StringBuilder constraint = new StringBuilder();
				constraint.append(word);
				int count = 0;
				int top = X;						
				// prefix						
				while (count < i && top >= 1) {
					if (board[top-1][Y] == EMPTY_TILE) {
						count++;
					}
					constraint.insert(0, board[top-1][Y]);
					top--;
				}
				// if there is letter touching the new tile
				while (top >= 1 && board[top-1][Y] != EMPTY_TILE) {
					constraint.insert(0, board[top-1][Y]);
					top--;
				}											
				count = 0;
				int down = X+word.length() - 1;						
				//suffix
				while (count < j && down < board.length - 1) {
					if (board[down+1][Y] == EMPTY_TILE) {
						count++;
					}
					constraint.append(board[down+1][Y]);
					down++;
				}						
				// if there is letter touching the new tile
				while (down < board.length - 1 && board[down+1][Y] != EMPTY_TILE) {
					constraint.append(board[down+1][Y]);
					down++;
				}
				
				// constraint is built
				Log.debug(i+","+j+"; "+top + "," + Y+" "+constraint.toString());
				Set<String> words = dict.getPossibleDictionaryWords(availableLetters,constraint.toString());
				
				
				for (String w : words) {
					// verify horizontal words if they are valid
					boolean isValid = true;
					int xindex = top;
					for (int l = 0; l < w.length(); l++) {
						
						if (constraint.charAt(l) != EMPTY_TILE) {
							xindex++;
							continue;
						}
						// skip the current column
						/*if (Y == startY+l+1) {
							continue;
						}*/
						
						// the new word formed is vertical, so check for valid horizontal words
						
						StringBuilder sb = new StringBuilder();
						sb.append(w.charAt(l));
						int yindex = Y;
						while (yindex >= 1 && board[xindex][yindex-1] != EMPTY_TILE) {
							sb.insert(0, board[xindex][yindex-1]);
							yindex--;
						}
						yindex = Y;
						while (yindex < board.length - 1 && board[xindex][yindex+1] != EMPTY_TILE) {
							sb.append(board[xindex][yindex+1]);
							yindex++;
						}
						if (sb.length() > 1) {
							if (!dict.search(sb.toString())) {
								Log.debug("For word "+w+", horizontal word is "+sb.toString()+": INVALID");
								isValid = false;
								break;
							}
							else {
								Log.debug("For word "+w+", horizontal word is "+sb.toString()+": VALID");
							}
						}
						xindex++;
					}
					if (isValid) {
						//Log.debug(w);
						Log.debug("Valid word "+ w);
						Suggestion s;
						if (isHorizontalCalled) {
							// this function was called with the current word as horizontal, so the 
							// new word formed must be horizontal : false
							s = new Suggestion(w, Y, top, 0, false);
						}
						else {
							// this function was called with the current word as vertical, so the 
							// new word formed must be vertical : true
							s = new Suggestion(w, top, Y, 0, true);
						}
						
						if (!set.contains(s)) {
							set.add(s);
						}
					}
					else {
						//Log.info("Invalid word "+ w);
						Log.debug("Invalid word "+ w);
					}
					
					
				}
				
			}
		}
		return set;
	}
	
	public static Set<Suggestion> getSuggestions(Set<Word> wordsOnBoard, char[][] board, String availableLetters) {
		Set<Suggestion> suggestions = new HashSet<Suggestion>();
		Set<Suggestion> result = new HashSet<Suggestion>();
		if (wordsOnBoard == null || wordsOnBoard.size() == 0) {
			return suggestions;
		}
		
		for (Word word : wordsOnBoard) {
			boolean isVertical = word.isVertical();
			int x = word.getX();
			int y = word.getY();
			result.addAll(getPossibleWordsUsingEntireGivenWord(board, x, y, word.getText(), availableLetters, isVertical, false, suggestions));
			Log.info("Done with word: "+word.getText());
		}
		return result;
	}
	
	
	public static char[][] readBoardFromInputFile(String filename) throws IOException {
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
	
	public static Set<Word> getWordsFromBoard(char[][]board, boolean isHorizontal) {
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
							word.setVertical(false);
						}
						else {
							word.setX(y);
							word.setY(x);
							word.setVertical(true);
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
					word.setVertical(false);
				}
				else {
					word.setX(y);
					word.setY(x);
					word.setVertical(true);
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


