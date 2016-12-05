package wordsuggester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import dictionary.Dictionary;
import util.Constants;
import util.Direction;
import util.Suggestion;
import util.Word;

public class WordSuggester {

	static Dictionary dict;
	static final char EMPTY_TILE = Constants.EMPTY_TILE;
	static final char BLANK_TILE = Constants.BLANK_TILE;
	static final Logger Log = Logger.getLogger(WordSuggester.class);
	static char[][] inputBoard, transposedBoard;
	static int[][] tileScoreLetter, tileScoreWord;
	static String availableLetters;
	static final int MAX_SUGGESTIONS = 10;
	static Comparator<Suggestion> byScore = (Suggestion o1, Suggestion o2)-> Integer.compare(o1.getScore(), o2.getScore());

	// a to z letter scores
	static final int[] letterValues = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10};

	public WordSuggester(Dictionary dict) {
		WordSuggester.dict = dict;
	}

	public CompletableFuture<List<Suggestion>> findWordWithBestPossibleScoreAsync(Set<Word> wordsOnBoard, char[][] board, int[][] tileScoreLetter, int[][] tileScoreWord,
			String availableLetters, int noOfSuggestions) throws ExecutionException, InterruptedException {
		final long startTime = System.currentTimeMillis();
		if (wordsOnBoard == null || wordsOnBoard.size() == 0 || noOfSuggestions <= 0) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		WordSuggester.inputBoard = board;
		WordSuggester.tileScoreLetter = tileScoreLetter;
		WordSuggester.tileScoreWord = tileScoreWord;
		WordSuggester.transposedBoard = getTranspose(board);
		WordSuggester.availableLetters = availableLetters;

		List<CompletableFuture<Set<Suggestion>>> list = new ArrayList<CompletableFuture<Set<Suggestion>>>();

		for (Word word : wordsOnBoard) {
			Direction direction = word.getDirection();
			int X = word.getX();
			int Y = word.getY();
			Log.debug("Checking with word: "+word.getText());
			final int startX = direction == Direction.VERTICAL ? X : Y;
			final int startY = direction == Direction.VERTICAL ? Y : X;
			final boolean isTransposed = direction == Direction.HORIZONTAL;
			list.addAll(performAllChecksAroundGivenWord(startX, startY, word.getText(), isTransposed));
			Log.debug("Completed End of Given Word Check");
		}
		@SuppressWarnings("unchecked")
		CompletableFuture<Set<Suggestion>>[] arrayList = list.toArray(new CompletableFuture[list.size()]);
		CompletableFuture<List<Set<Suggestion>>> output = CompletableFuture.allOf(arrayList)
				.thenApply(f -> list.stream().map(v -> v.join()).collect(Collectors.toList()));

		CompletableFuture<List<Suggestion>> resultFuture =  output.thenApply(l -> {
			return l.stream().flatMap(set -> set.stream()).sorted(byScore.reversed())
					.limit(Math.min(noOfSuggestions, MAX_SUGGESTIONS))
					.collect(Collectors.toList());
		});
		final long endTime = System.currentTimeMillis();
		System.out.println("Total execution time of WordSuggester: "+(endTime - startTime));
		return resultFuture;
	}


	private List<CompletableFuture<Set<Suggestion>>> performAllChecksAroundGivenWord(int startX, int startY, String word,	boolean isTransposed) throws ExecutionException, InterruptedException {
		CompletableFuture<Set<Suggestion>> entireWordFuture = CompletableFuture.supplyAsync(() -> getPossibleWordsUsingEntireGivenWord
				(startX, startY, word, isTransposed));
		//System.out.println("Completed Entire Word Check");
		Log.debug("Completed Entire Word Check");

		CompletableFuture<Set<Suggestion>> oneLetterFuture = CompletableFuture.supplyAsync(() -> getPossibleWordsUsingOneLetterOfTheWord
				(startX, startY, word, isTransposed));
		//System.out.println("Completed Per Letter Check");
		Log.debug("Completed Per Letter Check");

		CompletableFuture<Set<Suggestion>> adjacentFuture = CompletableFuture.supplyAsync(() -> getPossibleWordsByPlacingLettersInAdjacentTiles
				(startX, startY, word, isTransposed));
		//System.out.println("Completed Adjacent Tiles Check");
		Log.debug("Completed Adjacent Tiles Check");

		CompletableFuture<Set<Suggestion>> startFuture = CompletableFuture.supplyAsync(() -> getPossibleWordsByPlacingALetterAtStartOfGivenWord
				(startX, startY, word, isTransposed));
		//System.out.println("Completed Start of Given Word Check");
		Log.debug("Completed Start of Given Word Check");

		CompletableFuture<Set<Suggestion>> endFuture = CompletableFuture.supplyAsync(() -> getPossibleWordsByPlacingALetterAtEndOfGivenWord
				(startX, startY, word, isTransposed));
		//System.out.println("Completed End of Given Word Check");
		Log.debug("Completed End of Given Word Check");

		return Arrays.asList(entireWordFuture, oneLetterFuture, adjacentFuture, startFuture, endFuture);

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
	private Set<Suggestion> getPossibleWordsUsingOneLetterOfTheWord (int X, int Y, String word, boolean isTransposed) {
		final char[][] board = isTransposed ? transposedBoard : inputBoard;
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
					Word result = generateConstraintForVerticalWord(posX, Y, board[posX][Y], j, k,
							Direction.HORIZONTAL, isTransposed);
					String constraint = result.getText();
					Log.debug(result.getX() + "," + result.getY()+" "+constraint);
					Set<String> possibleWords = dict.getPossibleDictionaryWords(availableLetters,constraint);
					for (String ele : possibleWords) {
						boolean isValid = true;
						final int len = ele.length();
						int yindex = result.getY();
						int score = 0;
						for (int l = 0; l < len; l++) {
							if (constraint.charAt(l) != EMPTY_TILE) {
								// current letter was already present on board. So skip it!
								yindex++;
								continue;
							}
							Word verticalWordObj = getWordFormedOnPlacingTheSuggestedWord(result.getX(), yindex,
									ele.charAt(l), Direction.VERTICAL, isTransposed);
							String verticalWord = verticalWordObj.getText();
							if (verticalWord.length() > 1) {
								if (!dict.search(verticalWord)) {
									Log.debug("For word "+ele+", vertical word is "+verticalWord+": INVALID");
									isValid = false;
									break;
								}
								else {
									Log.debug("For word "+ele+", vertical word is "+verticalWord+": VALID");
									score += calculateScore(verticalWordObj.getX(), verticalWordObj.getY(), verticalWord, Direction.VERTICAL,
											isTransposed);
								}
							}
							yindex++;
						}
						if (isValid) {
							score += calculateScore(result.getX(), result.getY(), ele, Direction.HORIZONTAL, isTransposed);
							Log.debug("Valid word "+ ele);
							// we use !isHorizontalCalled because if the word on board was vertical
							// then the new word formed using one letter of the given word must be horizontal.
							Suggestion suggest = getSuggestion(ele, result.getY(),
									result.getX(), score, !isTransposed);
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
	private Set<Suggestion> getPossibleWordsUsingEntireGivenWord(int X, int Y, String word,	boolean isTransposed) {
		//System.out.println("Called entire given method");
		final char[][] board = isTransposed ? transposedBoard : inputBoard;
		Set<Suggestion> suggestions = new HashSet<Suggestion>();
		// current word is vertical for sure
		final int size = availableLetters.length();
		// i will be the number of '.' as prefix in the constraint
		// j will be the number if '.' as suffix in the constraint
		// in all there will be 'size' no. of '.' in the constraint; so i+j <= size
		for (int i = 0; i <= size; i++) {
			for (int j = 0; j < size - i + 1; j++) {
				Word result = generateConstraintForVerticalWord(X, Y, board[X][Y], i, j, Direction.VERTICAL,
						isTransposed);
				Log.debug(result);
				String constraint = result.getText();
				Set<String> words = dict.getPossibleDictionaryWords(availableLetters,constraint);
				for (String w : words) {
					// since the word starting from anchor point is always vertical,
					// the new word formed using the entire anchor point word can form only additional horizontal words.
					// lets verify if they are valid
					boolean isValid = true;
					final int xindex = result.getX();
					int score = 0;
					for (int l = 0; l < w.length(); l++) {
						if (constraint.charAt(l) != EMPTY_TILE) {
							// current letter was already present on board. So skip it!
							continue;
						}
						Word horizontalWordObj = getWordFormedOnPlacingTheSuggestedWord(xindex+l, result.getY(),
								w.charAt(l), Direction.HORIZONTAL, isTransposed);
						String horizontalWord = horizontalWordObj.getText();
						if (horizontalWord.length() > 1) {
							if (!dict.search(horizontalWord)) {
								Log.debug("For word "+w+", horizontal word is "+horizontalWord+": INVALID");
								isValid = false;
								break;
							}
							else {
								score += calculateScore(horizontalWordObj.getX(), horizontalWordObj.getY(), horizontalWord, Direction.HORIZONTAL, isTransposed);
								Log.debug("For word "+w+", horizontal word is "+horizontalWord+": VALID");
							}
						}
					}
					if (isValid) {
						Log.debug("Valid word "+ w);
						score += calculateScore(result.getX(), result.getY(), w, Direction.VERTICAL, isTransposed);
						Suggestion suggest = getSuggestion(w, result.getX(), result.getY(), score, isTransposed);
						suggestions.add(suggest);
					}
					else {
						Log.debug("Invalid word "+ w);
					}
				}
			}
		}
		//System.out.println("Returning from entire given method");
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
	private Set<Suggestion> getPossibleWordsByPlacingLettersInAdjacentTiles(int X, int Y, String word,
			boolean isTransposed) {
		//System.out.println("Called adjacent method");
		final char[][] board = isTransposed ? transposedBoard : inputBoard;
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
					Word result = generateConstraintForVerticalWord(xpos, col, board[xpos][col], j, 0, Direction.VERTICAL, isTransposed);
					StringBuilder constraint = new StringBuilder(result.getText());
					Set<String> words = dict.getPossibleDictionaryWords(availableLetters, constraint.toString());
					for(String ele : words) {
						final int len = ele.length();
						boolean isValid = true;
						final int xindex = result.getX();
						int score = 0;
						for (int k = 0; k < len; k++) {
							if (constraint.charAt(k) != EMPTY_TILE) {
								// current letter was already present on board. So skip it!
								continue;
							}
							Word horizontalWordObj = getWordFormedOnPlacingTheSuggestedWord(xindex+k, result.getY(),
									ele.charAt(k), Direction.HORIZONTAL, isTransposed);
							String horizontalWord = horizontalWordObj.getText();
							if (horizontalWord.length() > 1) {
								if (!dict.search(horizontalWord)) {
									Log.debug("For word "+ele+", horizontal word is "+horizontalWord+": INVALID");
									isValid = false;
									break;
								}
								else {
									score += calculateScore(horizontalWordObj.getX(), horizontalWordObj.getY(), horizontalWord, Direction.HORIZONTAL, isTransposed);
									Log.debug("For word "+ele+", horizontal word is "+horizontalWord+": VALID");
								}
							}
						}
						if (isValid) {
							Log.debug("Valid word "+ ele);
							score += calculateScore(result.getX(), result.getY(), ele, Direction.VERTICAL, isTransposed);
							Suggestion suggest = getSuggestion(ele, result.getX(), result.getY(),
									score, isTransposed);
							suggestions.add(suggest);
						}
						else {
							Log.debug("Invalid word "+ ele);
						}
					}
				}

			}
		}
		//System.out.println("Returning from adjacent method");
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
	private Set<Suggestion> getPossibleWordsByPlacingALetterAtEndOfGivenWord(int X, int Y, String word,
			boolean isTransposed) {
		final char[][] board = isTransposed ? transposedBoard : inputBoard;
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
						Word result = generateConstraintForVerticalWord(posX, Y, currentChar, i, j,
								Direction.HORIZONTAL, isTransposed);
						String constraint = result.getText();
						Log.debug(result);
						Set<String> possibleWords = dict.getPossibleDictionaryWords(availableLetters,constraint);
						for (String w : possibleWords) {
							boolean isValid = true;
							final int len = w.length();
							final int yindex = result.getY();
							int score = 0;
							for (int l = 0; l < len; l++) {
								if (constraint.charAt(l) != EMPTY_TILE) {
									// current letter was already present on board. So skip it!
									continue;
								}
								Word verticalWordObj = getWordFormedOnPlacingTheSuggestedWord(result.getX(), yindex+l,
										w.charAt(l), Direction.VERTICAL, isTransposed);
								String verticalWord = verticalWordObj.getText();
								if (verticalWord.length() > 1) {
									if (!dict.search(verticalWord)) {
										Log.debug("For word "+w+", vertical word is "+verticalWord+": INVALID");
										isValid = false;
										break;
									}
									else {
										score += calculateScore(verticalWordObj.getX(), verticalWordObj.getY(), verticalWord, Direction.VERTICAL, isTransposed);
										Log.debug("For word "+w+", vertical word is "+verticalWord+": VALID");
									}
								}
							}
							if (isValid) {
								Log.debug("Valid word "+ w);
								score += calculateScore(result.getX(), result.getY(), w, Direction.HORIZONTAL, isTransposed);
								// we use !isHorizontalCalled because if the word on board was vertical
								// then the new word formed using one letter of the given word must be horizontal.
								Suggestion suggest = getSuggestion(w, result.getY(),
										result.getX(), score, !isTransposed);
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
	private Set<Suggestion> getPossibleWordsByPlacingALetterAtStartOfGivenWord(int X, int Y, String word,
			boolean isTransposed) {
		final char[][] board = isTransposed ? transposedBoard : inputBoard;
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
						Word result = generateConstraintForVerticalWord(posX, Y, currentChar, i, j,
								Direction.HORIZONTAL, isTransposed);
						String constraint = result.getText();
						Log.debug(result);
						Set<String> possibleWords = dict.getPossibleDictionaryWords(availableLetters,constraint);
						for (String w : possibleWords) {
							boolean isValid = true;
							final int len = w.length();
							final int yindex = result.getY();
							int score = 0;
							for (int l = 0; l < len; l++) {
								if (constraint.charAt(l) != EMPTY_TILE) {
									continue;
								}
								Word verticalWordObj = getWordFormedOnPlacingTheSuggestedWord(result.getX(), yindex+l,
										w.charAt(l), Direction.VERTICAL, isTransposed);
								String verticalWord = verticalWordObj.getText();
								if (verticalWord.length() > 1) {
									if (!dict.search(verticalWord)) {
										Log.debug("For word "+w+", vertical word is "+verticalWord+": INVALID");
										isValid = false;
										break;
									}
									else {
										score += calculateScore(verticalWordObj.getX(), verticalWordObj.getY(), verticalWord, Direction.VERTICAL, isTransposed);
										Log.debug("For word "+w+", vertical word is "+verticalWord+": VALID");
									}
								}
							}
							if (isValid) {
								Log.debug("Valid word "+ w);
								score += calculateScore(result.getX(), result.getY(), w, Direction.HORIZONTAL, isTransposed);
								// we use !isHorizontalCalled because if the word on board was vertical
								// then the new word formed using one letter of the given word must be horizontal.
								Suggestion suggest = getSuggestion(w, result.getY(),
										result.getX(), score, !isTransposed);
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
	private Word getWordFormedOnPlacingTheSuggestedWord(int X, int Y, char currentChar, Direction direction,
			boolean isTransposed) {
		final char[][] board = isTransposed ? transposedBoard : inputBoard;
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
		if (direction == Direction.HORIZONTAL) {
			return new Word(word.toString(), X, prefixIndex, direction);
		}
		return new Word(word.toString(), prefixIndex, Y, direction);
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
	private Word generateConstraintForVerticalWord(int X, int Y, char currentChar,
			int prefixLength, int suffixLength, Direction direction, boolean isTransposed) {
		final char[][] board = isTransposed ? transposedBoard : inputBoard;
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
		Word result = direction == Direction.VERTICAL ?
				new Word(constraint.toString(),prefixIndex,Y,Direction.VERTICAL) :
					new Word(constraint.toString(),X,prefixIndex,Direction.HORIZONTAL);
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

	private int calculateScore(int X, int Y, String word, Direction direction, boolean isTransposed) {
		final char[][] board = isTransposed ? transposedBoard : inputBoard;
		int score = 0;
		char[] arr = word.toCharArray();
		// calculate double/triple letter scores here
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
			boolean isBlankTileUsed = false;
			final int charScore = letterValues[arr[i]-97];
			Log.debug(word + ", "+X+", "+Y+" "+ch+" "+arr[i]);
			if (ch == EMPTY_TILE) {
				// ch will be placed at this tile
				if (availableLetters.indexOf(arr[i]) == -1) {
					// this means blank tile was used to form this word.
					isBlankTileUsed = true;
				}
				if (!isBlankTileUsed) {
					score += charScore * tileScoreLetter[posX][posY];
				}
			}
			else {
				score += charScore;
			}
		}

		//calculate double/triple word scores here
		int multiplier = 1;
		for (int i = 0; i < arr.length; i++) {
			int posX, posY;
			if (direction == Direction.VERTICAL) {
				posX = X+i;
				posY = Y;
			}
			else {
				posX = X;
				posY = Y+i;
			}
			final int value = tileScoreWord[posX][posY];
			if (value > 0) {
				multiplier *= value;
			}
		}

		return score*multiplier;
	}
}
