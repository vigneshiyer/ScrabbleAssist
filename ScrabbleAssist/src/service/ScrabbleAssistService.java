package service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dictionary.Dictionary;
import pojo.Board;
import pojo.SortType;
import pojo.SuggestionResult;
import util.Constants;
import util.Direction;
import util.Suggestion;
import util.Word;
import wordsuggester.WordSuggester;

@Path("/")
public class ScrabbleAssistService implements ServletContextListener {

	private static Dictionary dict;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public SortType getHtml() {
		return SortType.BEST_SCORE;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public SuggestionResult getSuggestions(Board input) {
		// read default letter score matrix
		int[][] tileScoreLetter = generateScoreMatrix("letterScore.txt");
		int[][] tileScoreWord = generateScoreMatrix("wordScore.txt");

		Word[] wordsOnBoard = input.getWordsOnBoard();

		// create board
		char[][] board = new char[15][15];

		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				board[i][j] = Constants.EMPTY_TILE;
			}
		}

		for (int i = 0; i < wordsOnBoard.length; i++) {
			Word w = wordsOnBoard[i];
			String text = w.getText();
			final int length = text.length();
			final int row = w.getX();
			final int col = w.getY();
			for (int j = 0; j < length; j++) {
				if (w.getDirection() == Direction.VERTICAL) {
					board[row+j][col] = text.charAt(j);
					tileScoreLetter[row+j][col] = 1;
					tileScoreWord[row+j][col] = 0;
				}
				else {
					board[row][col+j] = text.charAt(j);
					tileScoreLetter[row][col+j] = 1;
					tileScoreWord[row][col+j] = 0;
				}
			}
		}
		WordSuggester wordSuggester = new WordSuggester(dict);
		SuggestionResult output = new SuggestionResult();
		try {
			List<Suggestion> result = wordSuggester
					.findWordWithBestPossibleScore(wordsOnBoard, board, tileScoreLetter, tileScoreWord,
							input.getAvailableLetters(), input.getNumberOfSuggestions());
			System.out.println(result.size());
			Suggestion[] suggestions = new Suggestion[result.size()];
			for (int i = 0; i < suggestions.length; i++) {
				suggestions[i] = result.get(i);
			}
			output.setSuggestions(suggestions);
			output.setStatus("SUCCESS");
			return output;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		output.setStatus("FAILED");
		return output;
	}

	private int[][] generateScoreMatrix(String fileName) {
		int[][] input = new int[15][15];
		try {
			BufferedReader br = new BufferedReader(new FileReader(getClass().getClassLoader()
					.getResource(fileName).getFile()));
			int row = 0;
			String str;
			while ((str = br.readLine()) != null && row < 15) {
				char[] arr = str.toLowerCase().toCharArray();
				for (int j = 0; j < arr.length; j++) {
					input[row][j] = arr[j] - '0';
				}
				row++;
			}
			br.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return input;
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(getClass().getClassLoader()
					.getResource("words.txt").getFile()));
			String str;
			dict = new Dictionary();
			while ((str = br.readLine()) != null) {
				dict.insert(str);
			}
			br.close();
			System.out.println("Called");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
