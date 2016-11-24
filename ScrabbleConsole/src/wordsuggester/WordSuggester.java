package wordsuggester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import dictionary.Dictionary;
import util.Constants;
import util.Constraint;
import util.Direction;
import util.Suggestion;
import util.Word;

public class WordSuggester {

	static Dictionary dict;
	static final char EMPTY_TILE = Constants.EMPTY_TILE;
	static final char BLANK_TILE = Constants.BLANK_TILE;
	static final Logger Log = Logger.getLogger(WordSuggester.class);
	// a to z followed by blank tile
	static final int[] letterValues = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10, 0};

	public WordSuggester(Dictionary dict) {
		WordSuggester.dict = dict;
	}

	public Set<Suggestion> findWordWithBestPossibleScore(Set<Word> wordsOnBoard, char[][] board, 
			String availableLetters) {
		Set<Suggestion> suggestions = new HashSet<Suggestion>();
		if (wordsOnBoard == null || wordsOnBoard.size() == 0) {
			return suggestions;
		}
		for (Word word : wordsOnBoard) {
			Direction direction = word.getDirection();
			int X = word.getX();
			int Y = word.getY();
			Log.info("Checking with word: "+word.getText());
			suggestions.addAll(getPossibleWordsUsingEntireGivenWord(board, X, Y, word.getText(), 
					availableLetters, direction, false));
			Log.debug("Completed Entire Word Check");
			suggestions.addAll(getPossibleWordsUsingOneLetterOfTheWord(board, X, Y, word.getText(), 
					availableLetters, direction, false));
			Log.debug("Completed Per Letter Check");
			suggestions.addAll(getPossibleWordsByPlacingLettersInAdjacentTiles(board, X, Y, word.getText(), 
					availableLetters, direction, false));
			Log.debug("Completed Adjacent Tiles Check");
			suggestions.addAll(getPossibleWordsByPlacingALetterAtStartOfGivenWord(board, X, Y, word.getText(), 
					availableLetters, direction, false));
			Log.debug("Completed Start of Given Word Check");
			suggestions.addAll(getPossibleWordsByPlacingALetterAtEndOfGivenWord(board, X, Y, word.getText(), 
					availableLetters, direction, false));
			Log.debug("Completed End of Given Word Check");
		}
		return suggestions;
	}

	/**
	 * 
	 * @param board
	 * @param X
	 * @param Y
	 * @param word
	 * @param availableLetters
	 * @param direction
	 * @param isTransposed
	 * @return
	 */
	// check 1
	private Set<Suggestion> getPossibleWordsUsingOneLetterOfTheWord (char[][] board, int X, int Y, String word,
			String availableLetters, Direction direction, boolean isTransposed) {
		if (direction == Direction.HORIZONTAL) {
			char[][] transpose = getTranspose(board);
			return getPossibleWordsUsingOneLetterOfTheWord(transpose, Y, X, word, availableLetters,
					Direction.VERTICAL, true);
		}
		Set<Suggestion> suggestions = new HashSet<Suggestion>();
		char[] arr = word.toCharArray();
		for (int i = 0; i < arr.length; i++) {
			// skipping those letters of the word that have an adjacent tile non-empty
			// left column
			final int posX = X+i;
			if (posX >= board.length || posX < 0) {
				continue;
			}
			if (Y >= 1 && board[posX][Y-1] != EMPTY_TILE) {
				continue;
			}
			// right column
			if (Y < board.length - 1 && board[posX][Y+1] != EMPTY_TILE) {
				continue;
			}

			final int size = availableLetters.length();
			for (int j = 0; j <= size; j++) {					
				for (int k = 0; k < size - j + 1; k++) {
					Constraint result = generateConstraintForVerticalWord(board, posX, Y, board[posX][Y], j, k,
							Direction.HORIZONTAL);
					String constraint = result.getText();
					Log.debug(result.getX() + "," + result.getY()+" "+constraint);
					Set<String> possibleWords = dict.getPossibleDictionaryWords(availableLetters,constraint);						
					for (String ele : possibleWords) {
						boolean isValid = true;
						final int len = ele.length();
						int yindex = result.getY();
						for (int l = 0; l < len; l++) {
							if (constraint.charAt(l) != EMPTY_TILE) {
								// current letter was already present on board. So skip it!
								yindex++;
								continue;
							}
							String verticalWord = getWordFormedOnPlacingTheSuggestedWord(board, 
									result.getX(), yindex, ele.charAt(l), Direction.VERTICAL);
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
							// we use !isHorizontalCalled because if the word on board was vertical 
							// then the new word formed using one letter of the given word must be horizontal.
							Suggestion suggest = getSuggestion(ele, result.getY(), 
									result.getX(), 0, !isTransposed);
							suggestions.add(suggest);
						}
						else {
							Log.debug("Invalid word "+ ele);
						}
					}
				}
			}
		}
		return suggestions;
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
	 * @param isTransposed - is solely used while creating suggestion objects
	 * @return
	 */
	private Set<Suggestion> getPossibleWordsUsingEntireGivenWord(char[][] board, int X, int Y, 
			String word, String availableLetters, Direction direction, boolean isTransposed) {
		if (direction == Direction.HORIZONTAL) {
			char[][] transpose = getTranspose(board);
			// call the same method with the transpose
			return getPossibleWordsUsingEntireGivenWord(transpose, Y, X, word, availableLetters,
					Direction.VERTICAL, true);
		}
		Set<Suggestion> suggestions = new HashSet<Suggestion>();
		// current word is vertical for sure
		final int size = availableLetters.length();
		// i will be the number of '.' as prefix in the constraint
		// j will be the number if '.' as suffix in the constraint
		// in all there will be 'size' no. of '.' in the constraint; so i+j <= size
		for (int i = 0; i <= size; i++) {
			for (int j = 0; j < size - i + 1; j++) {
				Constraint result = generateConstraintForVerticalWord(board, X, Y, board[X][Y], i, j, Direction.VERTICAL);
				Log.debug(result);
				String constraint = result.getText();
				Set<String> words = dict.getPossibleDictionaryWords(availableLetters,constraint);
				for (String w : words) {
					// since the word starting from anchor point is always vertical,
					// the new word formed using the entire anchor point word can form only additional horizontal words.
					// lets verify if they are valid
					boolean isValid = true;
					final int xindex = result.getX();
					for (int l = 0; l < w.length(); l++) {						
						if (constraint.charAt(l) != EMPTY_TILE) {
							// current letter was already present on board. So skip it!
							continue;
						}
						String horizontalWord = getWordFormedOnPlacingTheSuggestedWord(board, xindex+l,
								result.getY(), w.charAt(l), Direction.HORIZONTAL);
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
					}
					if (isValid) {
						Log.debug("Valid word "+ w);
						Suggestion suggest = getSuggestion(w, result.getX(), result.getY(), 0, isTransposed);
						suggestions.add(suggest);
					}
					else {
						Log.debug("Invalid word "+ w);
					}
				}				
			}
		}
		return suggestions;
	}

	/**
	 * 
	 * @param board
	 * @param X
	 * @param Y
	 * @param word
	 * @param availableLetters
	 * @param direction
	 * @param isTransposed
	 * @return
	 */
	// check 3	
	private Set<Suggestion> getPossibleWordsByPlacingLettersInAdjacentTiles(char[][] board, int X, int Y, 
			String word, String availableLetters, Direction direction, boolean isTransposed) {

		if (direction == Direction.HORIZONTAL) {
			char[][] transpose = getTranspose(board);
			return getPossibleWordsByPlacingLettersInAdjacentTiles(transpose, Y, X, word, availableLetters,
					Direction.VERTICAL, true);
		}
		Set<Suggestion> suggestions = new HashSet<Suggestion>();
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
		// Here the left column can be ignored from this check because E is present. So check 1 would have 
		// been already performed on "EA"
		List<Integer> columns = new ArrayList<Integer>();				
		final int wordSize = word.length();
		boolean needToCheckLeftColumn = true, needToCheckRightColumn = true;
		for (int i = 0; i < wordSize; i++) {
			if (Y > 0 && board[X+i][Y-1] != EMPTY_TILE) {
				needToCheckLeftColumn = false;
			}
			if (Y < board.length - 1 && board[X+i][Y+1] != EMPTY_TILE) {
				needToCheckRightColumn = false;
			}
		}
		if (Y > 0 && needToCheckLeftColumn) {
			columns.add(Y-1);
		}
		if (Y < board.length - 1 && needToCheckRightColumn) {
			columns.add(Y+1);
		}
		for (int col : columns) {
			final int size = availableLetters.length();
			for (int i = 0; i < wordSize; i++) {
				int xpos = X+i+1; // this is to include the starting char index. These constraints will commonly 
				// have '.' j represents the number of '.' in your constraint string
				if (xpos >= board.length || xpos < 0) {
					continue;
				}
				for (int j = 2; xpos >= j - 1 && j <= size; j++) {
					Constraint result = generateConstraintForVerticalWord(board, xpos, col, board[xpos][col], j, 0, Direction.VERTICAL);
					StringBuilder constraint = new StringBuilder(result.getText());
					Set<String> words = dict.getPossibleDictionaryWords(availableLetters, constraint.toString());
					for(String ele : words) {
						final int len = ele.length();
						boolean isValid = true;
						final int xindex = result.getX();
						for (int k = 0; k < len; k++) {
							if (constraint.charAt(k) != EMPTY_TILE) {
								// current letter was already present on board. So skip it!
								continue;
							}
							String horizontalWord = getWordFormedOnPlacingTheSuggestedWord(board, xindex+k, 
									result.getY(), ele.charAt(k), Direction.HORIZONTAL);							
							if (horizontalWord.length() > 1) {
								if (!dict.search(horizontalWord)) {
									Log.debug("For word "+ele+", horizontal word is "+horizontalWord+": INVALID");
									isValid = false;
									break;
								}
								else {
									Log.debug("For word "+ele+", horizontal word is "+horizontalWord+": VALID");
								}
							}
						}
						if (isValid) {
							Log.debug("Valid word "+ ele);
							Suggestion suggest = getSuggestion(ele, result.getX(), result.getY(), 
									0, isTransposed);
							suggestions.add(suggest);
						}
						else {
							Log.debug("Invalid word "+ ele);
						}
					}
				}

			}
		}
		return suggestions;
	}

	/**
	 * 
	 * @param board
	 * @param X
	 * @param Y
	 * @param word
	 * @param availableLetters
	 * @param direction
	 * @param isTransposed
	 * @return
	 */
	private Set<Suggestion> getPossibleWordsByPlacingALetterAtEndOfGivenWord(char[][] board, int X, 
			int Y, String word, String availableLetters, Direction direction, boolean isTransposed) {
		if (direction == Direction.HORIZONTAL) {
			char[][] transpose = getTranspose(board);
			return getPossibleWordsByPlacingALetterAtEndOfGivenWord(transpose, Y, X, word, availableLetters,
					Direction.VERTICAL, true);
		}
		Set<Suggestion> suggestions = new HashSet<Suggestion>();
		if (X <  board.length - 1) {
			// find all possible words with "<word>."
			Set<String> words = dict.getPossibleDictionaryWords(availableLetters, word+".");
			for (String ele : words) {
				Log.debug("Ending with "+ele.charAt(ele.length()-1)+" for word "+ele);
				final int size = availableLetters.length();
				final int eleLength = ele.length();
				final char currentChar = ele.charAt(ele.length()-1);
				final int posX = X + eleLength - 1;
				if (posX >= board.length || posX < 0) {
					continue;
				}
				for (int i = 0; i <= size; i++) {
					for (int j = 0; j < size - i + 1; j++) {
						Constraint result = generateConstraintForVerticalWord(board, posX, Y, currentChar, i, j, 
								Direction.HORIZONTAL);
						String constraint = result.getText();
						Log.debug(result);
						Set<String> possibleWords = dict.getPossibleDictionaryWords(availableLetters,constraint);						
						for (String w : possibleWords) {
							boolean isValid = true;
							final int len = w.length();
							final int yindex = result.getY();
							for (int l = 0; l < len; l++) {
								if (constraint.charAt(l) != EMPTY_TILE) {
									// current letter was already present on board. So skip it!
									continue;
								}
								String verticalWord = getWordFormedOnPlacingTheSuggestedWord(board,
										result.getX(), yindex+l, w.charAt(l), Direction.VERTICAL);
								if (verticalWord.length() > 1) {
									if (!dict.search(verticalWord)) {
										Log.debug("For word "+w+", vertical word is "+verticalWord+": INVALID");
										isValid = false;
										break;
									}
									else {
										Log.debug("For word "+w+", vertical word is "+verticalWord+": VALID");
									}
								}
							}
							if (isValid) {
								Log.debug("Valid word "+ w);
								// we use !isHorizontalCalled because if the word on board was vertical 
								// then the new word formed using one letter of the given word must be horizontal.
								Suggestion suggest = getSuggestion(w, result.getY(), 
										result.getX(), 0, !isTransposed);
								suggestions.add(suggest);
							}
							else {
								Log.debug("Invalid word "+ w);
							}
						}
					}
				}
			}
		}
		return suggestions;
	}

	/**
	 * 
	 * @param board
	 * @param X
	 * @param Y
	 * @param word
	 * @param availableLetters
	 * @param direction
	 * @param isTransposed
	 * @return
	 */
	private Set<Suggestion> getPossibleWordsByPlacingALetterAtStartOfGivenWord(char[][] board, int X, 
			int Y, String word, String availableLetters, Direction direction, boolean isTransposed) {
		if (direction == Direction.HORIZONTAL) {
			char[][] transpose = getTranspose(board);
			return getPossibleWordsByPlacingALetterAtStartOfGivenWord(transpose, Y, X, word, availableLetters,
					Direction.VERTICAL, true);
		}
		Set<Suggestion> suggestions = new HashSet<Suggestion>();
		if (X <  board.length - 1) {
			// find all possible words with ".<word>"
			Set<String> words = dict.getPossibleDictionaryWords(availableLetters, "."+word);
			for (String ele : words) {
				Log.debug("Starting with "+ele.charAt(0)+" for word "+ele);
				final int size = availableLetters.length();
				final char currentChar = ele.charAt(0);
				final int posX = X-1;
				if (posX >= board.length || posX < 0) {
					continue;
				}
				for (int i = 0; i <= size; i++) {
					for (int j = 0; j < size - i + 1; j++) {
						Constraint result = generateConstraintForVerticalWord(board, posX, Y, currentChar, i, j, 
								Direction.HORIZONTAL);
						String constraint = result.getText();
						Log.debug(result);
						Set<String> possibleWords = dict.getPossibleDictionaryWords(availableLetters,constraint);						
						for (String w : possibleWords) {
							boolean isValid = true;
							final int len = w.length();
							final int yindex = result.getY();
							for (int l = 0; l < len; l++) {
								if (constraint.charAt(l) != EMPTY_TILE) {
									continue;
								}
								String verticalWord = getWordFormedOnPlacingTheSuggestedWord(board,
										result.getX(), yindex+l, w.charAt(l), Direction.VERTICAL);
								if (verticalWord.length() > 1) {
									if (!dict.search(verticalWord)) {
										Log.debug("For word "+w+", vertical word is "+verticalWord+": INVALID");
										isValid = false;
										break;
									}
									else {
										Log.debug("For word "+w+", vertical word is "+verticalWord+": VALID");
									}
								}
							}
							if (isValid) {
								Log.debug("Valid word "+ w);
								// we use !isHorizontalCalled because if the word on board was vertical 
								// then the new word formed using one letter of the given word must be horizontal.
								Suggestion suggest = getSuggestion(w, result.getY(), 
										result.getX(), 0, !isTransposed);
								suggestions.add(suggest);
							}
							else {
								Log.debug("Invalid word "+ w);
							}
						}
					}
				}
			}
		}
		return suggestions;
	}

	/**
	 * 
	 * @param board
	 * @return
	 */
	private char[][] getTranspose(char[][] board) {
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
	private String getWordFormedOnPlacingTheSuggestedWord(char[][] board, int X, int Y, char currentChar, 
			Direction direction) {
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
	private Constraint generateConstraintForVerticalWord(char[][] board, int X, int Y, char currentChar, 
			int prefixLength, int suffixLength, Direction direction) {
		StringBuilder constraint = new StringBuilder();
		constraint.append(currentChar);
		int count = 0;
		int prefixIndex, suffixIndex;
		prefixIndex = direction == Direction.VERTICAL ? X-1 : Y-1;
		// prefix						
		while (count < prefixLength && prefixIndex >= 0) {
			char currentTile = direction == Direction.VERTICAL ? board[prefixIndex][Y] : board[X][prefixIndex];
			if (currentTile == EMPTY_TILE) {
				count++;
			}
			constraint.insert(0, currentTile);
			prefixIndex--;
		}
		// if there is letter touching the new tile
		while (direction == Direction.VERTICAL && prefixIndex >= 0 && board[prefixIndex][Y] != EMPTY_TILE) {
			constraint.insert(0, board[prefixIndex][Y]);
			prefixIndex--;
		}
		while (direction == Direction.HORIZONTAL && prefixIndex >= 0 && board[X][prefixIndex] != EMPTY_TILE) {
			constraint.insert(0, board[X][prefixIndex]);
			prefixIndex--;
		}

		count = 0;
		suffixIndex = direction == Direction.VERTICAL ? X+1 : Y+1;
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

		if (prefixLength == 0) {
			prefixIndex = direction == Direction.VERTICAL ? X : Y;
		}
		else {
			// because prefixIndex starts one row/column previous to the currentChar.
			prefixIndex++;
		}
		Constraint result = direction == Direction.VERTICAL ? 
				new Constraint(constraint.toString(),prefixIndex,Y,Direction.VERTICAL) : 
					new Constraint(constraint.toString(),X,prefixIndex,Direction.HORIZONTAL);		
				return result;
	}

	/**
	 * 
	 * @param word
	 * @param X
	 * @param Y
	 * @param score
	 * @param isTransposed
	 * @return
	 */
	private Suggestion getSuggestion(String word, int X, int Y, int score, boolean isTransposed) {
		if (isTransposed) {
			// this function was called with the current word as horizontal, so the 
			// new word formed must be horizontal : false
			return new Suggestion(word,Y,X,score,false);
		}
		// this function was called with the current word as vertical, so the 
		// new word formed must be vertical : true
		return new Suggestion(word,X,Y,score,true);
	}
	
	private int calculateScore(char[][] board, char[][] letterScore, char[][] wordScore, int X, int Y, String word, 
			String availableLetters, Direction direction) {
		int score = 0;
		final int size = availableLetters.length();
		Map<Character, Integer> lettersMap = new HashMap<Character, Integer>();
		for (int i = 0; i < size; i++) {
			char ch = availableLetters.charAt(i);
			if (lettersMap.containsKey(ch)) {
				lettersMap.put(ch, lettersMap.get(ch)+1);
			}
			else {
				lettersMap.put(ch, 1);
			}
		}
		
		
		char[] arr = word.toCharArray();
		for (int i = 0; i < arr.length; i++) {
			char ch;
			int posX, posY;
			if (direction == Direction.VERTICAL) {
				posX = X+i;
				posY = Y;				
			}
			else {
				posX = X;
				posY = Y+i;
			}
			ch = board[posX][posY];
			if (ch != EMPTY_TILE) {
				
			}
		}
		
		
		return score;
	}
}
