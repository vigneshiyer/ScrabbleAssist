package board;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import dictionary.Dictionary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.Constraint;
import util.Direction;
import util.Suggestion;
import util.Word;

import org.apache.log4j.Logger;

public class ScrabbleBoard {
	
	static Dictionary dict = new Dictionary();
	static final char EMPTY_TILE = '.';
	static final Logger Log = Logger.getLogger(ScrabbleBoard.class);
		
	//TODO: Keep a collection of words in a set to avoid duplicate words on board.
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
		set = findWordWithBestPossibleScore(wordsSet, board, "sayingr");
		int i = 1;
		for (Suggestion s : set) {
			System.out.println(i+". "+s);
			i++;
		}
	}
	
	// check 1
	private static Set<Suggestion> getPossibleWordsUsingOneLetterOfTheWord (char[][] board, int X, int Y, String word,
			String availableLetters, Direction direction, boolean isHorizontalCalled, Set<Suggestion> set) {
			if (direction == Direction.HORIZONTAL) {
				char[][] transpose = getTranspose(board);
				return getPossibleWordsUsingOneLetterOfTheWord(transpose,Y,X,word,availableLetters,Direction.VERTICAL,true,set);
			}		
			char[] arr = word.toCharArray();
			for (int i = 0; i < arr.length; i++) {
				// skipping those letters of the word that have an adjacent tile non-empty
				// left column
				final int posX = X+i;
				if (Y >= 1 && board[posX][Y-1] != EMPTY_TILE) {
					continue;
				}
				// right column
				if (posX < board.length-1 && board[posX][Y+1] != EMPTY_TILE) {
					continue;
				}
				int size = availableLetters.length();
				for (int j = 0; j <= size; j++) {					
					for (int k = 0; k < size - j + 1; k++) {
						Constraint result = generateConstraintForVerticalWord(board, posX, Y, j, k, Direction.HORIZONTAL);
						String constraint = result.getText();
						Log.debug(result.getX() + "," + result.getY()+" "+constraint);
						Set<String> possibleWords = dict.getPossibleDictionaryWords(availableLetters,constraint);						
						for (String ele : possibleWords) {
							boolean isValid = true;
							final int len = ele.length();
							int yindex = result.getY();
							for (int l = 0; l < len; l++) {
								String verticalWord = getWordFormedOnPlacingTheSuggestedWord(board, result.getX(), yindex, 
										ele.charAt(l), Direction.VERTICAL);
								if (verticalWord.length() > 1) {
									if (!dict.search(verticalWord)) {
										Log.debug("For word "+ele+", vertical word is "+verticalWord+": INVALID");
										isValid = false;
										break;
									}
									else {
										Log.debug("For word "+ele+", vertical word is "+verticalWord+": VALID");
									}
								}
								yindex++;								
							}
							if (isValid) {
								Log.debug("Valid word "+ ele);
								// we use !isHorizontalCalled because if the word on board was vertical then the new word formed 
								// using one letter of the given word must be horizontal.
								Suggestion suggest = getSuggestion(ele, result.getY(), result.getX(), 0, !isHorizontalCalled);
								set.add(suggest);
							}
							else {
								Log.debug("Invalid word "+ ele);
							}							
						}
					}
				}
			}
			return set;
	}
	
	// check 3	
	private static Set<Suggestion> getPossibleWordsByPlacingLettersInAdjacentTiles(char[][] board, int X, int Y, String word, String availableLetters,
				boolean isVertical, boolean isHorizontalCalled, Set<Suggestion> set) {
			
		if (!isVertical) {
			char[][] transpose = getTranspose(board);
			getPossibleWordsByPlacingLettersInAdjacentTiles(transpose,Y,X,word,availableLetters,true,true,set);
		}
		
		// The anchor point word here is vertical
		// Check if any letter is present to the word's left column and right column to the length of the word
		/*
		 * ...
		 * .W.
		 * EA.
		 * .R.
		 * .D.
		 * ...
		 */
		// Here the left column can be ignored from this check because E is present. So check 1 would have been already performed on "EA"
		List<Integer> columns = new ArrayList<Integer>();
		// for left column
		if (Y > 0) {
			int size = word.length();
			for (int i = 0; i < size; i++) {
				if (board[X+i][Y-1] != EMPTY_TILE) {
					columns.add(Y-1);
					break;
				}
			}
		}
		// for right column
		if (Y < board.length - 1) {
			int size = word.length();
			for (int i = 0; i < size; i++) {
				if (board[X+i][Y+1] != EMPTY_TILE) {
					columns.add(Y+1);
					break;
				}
			}
		}
		
		for (int col : columns) {
			int size = availableLetters.length();
			int wordSize = word.length();
			for (int i = 0; i < wordSize; i++) {
				StringBuilder constraint = new StringBuilder();
				int xpos = X+i;
				int ypos = col;
				
				// j represents the number of '.' in your constraint string
				for (int j = 2; xpos >= j - 1 && j <= size; j++) {
					int count = 0;
					while (count < j) {
						constraint.insert(0,".");					
						count++;
						xpos--;
					}
					// add letters touching board[xpos][ypos] tile on top
					while (xpos >= 1 && board[xpos-1][ypos] != EMPTY_TILE) {
						constraint.insert(0, board[xpos-1][ypos]);
						xpos--;
					}
					// add letters touching board[xpos][ypos] tile on bottom
					int tmp = X+i;
					while (tmp < board.length - 1 && board[tmp+1][ypos] != EMPTY_TILE) {
						constraint.append(board[tmp+1][ypos]);
						tmp++;
					}
					
					Set<String> words = dict.getPossibleDictionaryWords(availableLetters, constraint.toString());
					for(String w : words) {
						
					}
					
					
				}
				
				
				constraint.append('.');
				//suffix
				while (ypos < board.length - 1 && board[xpos][ypos+1] != EMPTY_TILE) {
					constraint.append(board[xpos][ypos+1]);
					ypos++;					
				}
				ypos = col;
				//prefix
				while (ypos > 0 && board[xpos][ypos-1] != EMPTY_TILE) {
					constraint.insert(0,board[xpos][ypos-1]);
					ypos--;
				}
				
				
				
				
			}
		}
			
			
			
		return set;
	}
	
	
	//check 2
	/**
	 * 
	 * @param board The current board position
	 * @param X co-ordinate of the anchor point
	 * @param Y co-ordinate of the anchor point
	 * @param word starting from the anchor point
	 * @param availableLetters
	 * @param isVertical - is the current word vertical
	 * @param isHorizontalCalled - is solely used while creating suggestion objects
	 * @param set - of suggestions as a result
	 * @return
	 */
	private static Set<Suggestion> getPossibleWordsUsingEntireGivenWord(char[][] board, int X, int Y, String word, String availableLetters,
			Direction direction, boolean isHorizontalCalled, Set<Suggestion> set) {
		if (direction == Direction.HORIZONTAL) {
			char[][] transpose = getTranspose(board);
			// call the same method with the transpose
			return getPossibleWordsUsingEntireGivenWord(transpose,Y,X,word,availableLetters,Direction.VERTICAL,true,set);
		}
		// current word is vertical for sure
		int size = availableLetters.length();
		// i will be the number of '.' as prefix in the constraint
		// j will be the number if '.' as suffix in the constraint
		// in all there will be 'size' no. of '.' in the constraint; so i+j <= size
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size - i + 1; j++) {
				Constraint result = generateConstraintForVerticalWord(board, X, Y, i, j, Direction.VERTICAL);
				String constraint = result.getText();
				Set<String> words = dict.getPossibleDictionaryWords(availableLetters,constraint);
				for (String w : words) {
					// since the word starting from anchor point is always vertical,
					// the new word formed using the entire anchor point word can form only additional horizontal words.
					// lets verify if they are valid
					boolean isValid = true;
					int xindex = result.getX();
					for (int l = 0; l < w.length(); l++) {						
						if (constraint.charAt(l) != EMPTY_TILE) {
							xindex++;
							continue;
						}
						String horizontalWord = getWordFormedOnPlacingTheSuggestedWord(board, xindex,
								result.getY(), board[xindex][result.getY()], Direction.HORIZONTAL);
						if (horizontalWord.length() > 1) {
							if (!dict.search(horizontalWord)) {
								Log.debug("For word "+w+", horizontal word is "+horizontalWord+": INVALID");
								isValid = false;
								break;
							}
							else {
								Log.debug("For word "+w+", horizontal word is "+horizontalWord+": VALID");
							}
						}
						xindex++;
					}
					if (isValid) {
						Log.debug("Valid word "+ w);
						Suggestion suggest = getSuggestion(w, result.getX(), result.getY(), 0, isHorizontalCalled);
						set.add(suggest);
					}
					else {
						Log.debug("Invalid word "+ w);
					}
				}				
			}
		}
		return set;
	}
	
	private static char[][] getTranspose(char[][] board) {
		char[][] transpose = new char[15][15];
		for (int i = 0; i < transpose.length; i++) {
			for (int j = 0; j < transpose.length; j++) {
				transpose[i][j] = board[j][i];
			}
		}
		return transpose;
	}
	
	/**
	 * 
	 * @param board
	 * @param X
	 * @param Y
	 * @param currentChar
	 * @param direction
	 * @return
	 */
	private static String getWordFormedOnPlacingTheSuggestedWord(char[][] board, int X, int Y, char currentChar, Direction direction) {
		StringBuilder word = new StringBuilder();
		word.append(currentChar);
		// prefix to left/top
		int prefixIndex = direction == Direction.HORIZONTAL ? Y-1 : X-1;
		while (direction == Direction.HORIZONTAL && prefixIndex >= 0 && board[X][prefixIndex] != EMPTY_TILE) {
			word.insert(0, board[X][prefixIndex]);
			prefixIndex--;
		}
		while (direction == Direction.VERTICAL && prefixIndex >= 0 && board[prefixIndex][Y] != EMPTY_TILE) {
			word.insert(0, board[prefixIndex][Y]);
			prefixIndex--;
		}
		int suffixIndex = direction == Direction.HORIZONTAL ? Y+1 : X+1;
		while (direction == Direction.HORIZONTAL && suffixIndex < board.length && board[X][suffixIndex] != EMPTY_TILE) {
			word.append(board[X][suffixIndex]);
			suffixIndex++;
		}
		while (direction == Direction.VERTICAL && suffixIndex < board.length && board[suffixIndex][Y] != EMPTY_TILE) {
			word.append(board[suffixIndex][Y]);
			suffixIndex++;
		}
		return word.toString();
	}
	
	/**
	 * 
	 * @param board
	 * @param X
	 * @param Y
	 * @param prefixLength
	 * @param suffixLength
	 * @param isVerticalDirection
	 * @return
	 */
	private static Constraint generateConstraintForVerticalWord(char[][] board, int X, int Y, int prefixLength, int suffixLength, Direction direction) {
		StringBuilder constraint = new StringBuilder();
		int count = 0;
		int prefixIndex = direction == Direction.VERTICAL ? X : Y;
		// prefix						
		while (count < prefixLength && prefixIndex >= 1) {
			char currentTile = direction == Direction.VERTICAL ? board[prefixIndex-1][Y] : board[X][prefixIndex-1];
			if (currentTile == EMPTY_TILE) {
				count++;
			}
			constraint.insert(0, currentTile);
			prefixIndex--;
		}
		// if there is letter touching the new tile
		while (direction == Direction.VERTICAL && prefixIndex >= 1 && board[prefixIndex-1][Y] != EMPTY_TILE) {
			constraint.insert(0, board[prefixIndex-1][Y]);
			prefixIndex--;
		}
		while (direction == Direction.HORIZONTAL && prefixIndex >= 1 && board[X][prefixIndex-1] != EMPTY_TILE) {
			constraint.insert(0, board[X][prefixIndex-1]);
			prefixIndex--;
		}
		
		count = 0;
		int suffixIndex = direction == Direction.VERTICAL ? X : Y;					
		//suffix
		while (count < suffixLength && suffixIndex < board.length) {
			char currentTile = direction == Direction.VERTICAL ? board[suffixIndex][Y] : board[X][suffixIndex];
			if (currentTile == EMPTY_TILE) {
				count++;
			}
			constraint.append(currentTile);
			suffixIndex++;
		}						
		// if there is letter touching the new tile
		while (direction == Direction.VERTICAL && suffixIndex < board.length && board[suffixIndex][Y] != EMPTY_TILE) {
			constraint.append(board[suffixIndex][Y]);
			suffixIndex++;
		}
		while (direction == Direction.HORIZONTAL && suffixIndex < board.length && board[X][suffixIndex] != EMPTY_TILE) {
			constraint.append(board[X][suffixIndex]);
			suffixIndex++;
		}
		Constraint result = direction == Direction.VERTICAL ? 
				new Constraint(constraint.toString(),prefixIndex,Y,Direction.VERTICAL) : new Constraint(constraint.toString(),X,prefixIndex,Direction.HORIZONTAL);		
		return result;
	}
	
	private static Set<Suggestion> findWordWithBestPossibleScore(Set<Word> wordsOnBoard, char[][] board, String availableLetters) {
		Set<Suggestion> suggestions = new HashSet<Suggestion>();
		Set<Suggestion> result = new HashSet<Suggestion>();
		if (wordsOnBoard == null || wordsOnBoard.size() == 0) {
			return suggestions;
		}
		
		for (Word word : wordsOnBoard) {
			Direction direction = word.getDirection();
			if (!word.getText().equals("for")) {
				continue;
			}
			int x = word.getX();
			int y = word.getY();
			result.addAll(getPossibleWordsUsingOneLetterOfTheWord(board, x, y, word.getText(), availableLetters, direction, false, suggestions));
			Log.info("Done with word: "+word.getText());
		}
		return result;
	}
	
	private static Suggestion getSuggestion(String word, int X, int Y, int score, boolean wasBoardTransposed) {
		if (wasBoardTransposed) {
			// this function was called with the current word as horizontal, so the 
			// new word formed must be horizontal : false
			return new Suggestion(word,Y,X,score,false);
		}
		// this function was called with the current word as vertical, so the 
		// new word formed must be vertical : true
		return new Suggestion(word,X,Y,score,true);
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


