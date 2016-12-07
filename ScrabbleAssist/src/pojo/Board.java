package pojo;

import util.Word;

public class Board {
	private Word[] wordsOnBoard;
	private String availableLetters;
	private int numberOfSuggestions;
	private SortType sortType;

	public Word[] getWordsOnBoard() {
		return wordsOnBoard;
	}
	public void setWordsOnBoard(Word[] wordsOnBoard) {
		this.wordsOnBoard = wordsOnBoard;
	}
	public int getNumberOfSuggestions() {
		return numberOfSuggestions;
	}
	public void setNumberOfSuggestions(int numberOfSuggestions) {
		this.numberOfSuggestions = numberOfSuggestions;
	}
	public SortType getSortType() {
		return sortType;
	}
	public void setSortType(SortType sortType) {
		this.sortType = sortType;
	}
	public String getAvailableLetters() {
		return availableLetters;
	}
	public void setAvailableLetters(String availableLetters) {
		this.availableLetters = availableLetters;
	}
}
