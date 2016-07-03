package dictionary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class TrieNode {
	private char data;
	public char getData() {
		return data;
	}
	private Map<Character,TrieNode> map = new HashMap<Character,TrieNode>(26);
	private boolean wordEndsHere = false;

	public boolean isWordEndsHere() {
		return wordEndsHere;
	}
	public void setWordEndsHere(boolean wordEndsHere) {
		this.wordEndsHere = wordEndsHere;
	}
	public Map<Character, TrieNode> getMap() {
		return map;
	}
	public TrieNode(char data) {
		this.data = data;
	}
}

public class Dictionary {
	private TrieNode root;
	private final char BLANK_LETTER = ' ';

	public Dictionary() {
		// root is null node
		root = new TrieNode('\0');
	}
	
	public Set<String> getPossibleDictionaryWords(String availableLetters) {
		return getPossibleDictionaryWords(availableLetters,"");
	}
	
	/*
		availableLetters - a,b,c,d,e,+,g is passed as "abcde g", <space> is a blank letter
		constraint - ...test... - . represents any available letter with the player, test represents the letters 
		present on board
	*/
	public Set<String> getPossibleDictionaryWords(String availableLetters, String constraint) {
		Set<String> result = new HashSet<String>();
		if (availableLetters == null || availableLetters.trim().length() == 0) {
			return result;
		}
		
		char[] input = availableLetters.toCharArray();
		// this set maintains a collection of letters which have been considered as the first letter
		Set<Character> set = new HashSet<Character>();		
		int size = input.length;
		
		Map<Integer,Character> fixedLetters = new HashMap<Integer,Character>();
		char[] fstrarr = constraint.toCharArray();
		
		for (int i = 0; i < fstrarr.length; i++) {
			if (fstrarr[i] != '.') {
				fixedLetters.put(i, fstrarr[i]);
			}
		}

		for (int i = 0; i < size; i++) {
			char ch = input[i];
			if (!set.contains(ch)) {
				set.add(ch);
				StringBuilder s = new StringBuilder();
				Set<Integer> setLetters = new HashSet<Integer>();
				findWordsRecursive(s,i,setLetters,input,fixedLetters,result,constraint.length());
			}
		}
		return result;
	}

	private Set<String> findWordsRecursive (StringBuilder s, int startCharIndex, Set<Integer> letters, 
			char[] input, Map<Integer,Character> fixedLetters, Set<String> result, int requiredLength) {
		if (s == null) {
			return result;
		}
		
		// empty stringbuilder
		if (s.length() == 0) {
			// append the fixed letters in appropriate positions
			int stringSize = s.length();			
			while (fixedLetters.containsKey(stringSize)) {
				s.append(fixedLetters.get(stringSize));
				stringSize = s.length();
			}
			
			char lt = input[startCharIndex];
			if (lt == BLANK_LETTER) {
				for (char ch = 'a'; ch <= 'z' ; ch++) {
					s.append(ch);
					Set<Integer> set = new HashSet<Integer>(letters);
					set.add(startCharIndex);
					findWordsRecursive(s,startCharIndex+1,set,input,fixedLetters,result,requiredLength);
					s.setLength(s.length()-1);
				}
				return result;
			}
			else {
				s.append(input[startCharIndex]);
				letters.add(startCharIndex);
			}			
		}

		if (s.length() > 0 && !startsWith(s.toString())) {
			return result;
		}
		
		// append the fixed letters in appropriate positions
		int stringSize = s.length();
		
		while (fixedLetters.containsKey(stringSize)) {
			s.append(fixedLetters.get(stringSize));
			stringSize = s.length();
		}

		if (s.length() > 0 && !startsWith(s.toString())) {
			return result;
		}

		// valid dictionary word
		if (search(s.toString()) && (requiredLength == 0 || s.length() == requiredLength)){
			int len = s.length();
			result.add(s.toString());
		}
		
		int size = input.length;
		
		for (int i = 0; i < size; i++) {
			char ch = input[i];
			if (!letters.contains(i)){				
				//check for blank letter
				if (ch == BLANK_LETTER) {
					for (char j = 'a'; j <= 'z'; j++) {
						Set<Integer> set = new HashSet<Integer>(letters);
						set.add(i);
						StringBuilder st = new StringBuilder(s);
						st.append(j);
						findWordsRecursive(st, startCharIndex, set, input, fixedLetters, result,requiredLength);
					}
				}
				else {
					Set<Integer> set = new HashSet<Integer>(letters);
					set.add(i);
					StringBuilder st = new StringBuilder(s);
					st.append(ch);
					findWordsRecursive(st, startCharIndex, set, input, fixedLetters, result,requiredLength);
					if (s.length() == 0) {
						break;
					}
				}
			}
		}

		return result;

	}

	public void insert(String word) {
		if (word == null || word.trim().length() == 0) {
			return;
		}

		char[] arr = word.toCharArray();
		TrieNode itr = root;
		Map<Character,TrieNode> map = itr.getMap();

		for (int i = 0; i < arr.length - 1; i++) {
			if (!map.containsKey(arr[i])) {
				map.put(arr[i], new TrieNode(arr[i]));
			}
			itr = map.get(arr[i]);
			map = itr.getMap();			
		}

		if (map.containsKey(arr[arr.length-1])) {
			map.get(arr[arr.length-1]).setWordEndsHere(true);
		}
		else {
			TrieNode node = new TrieNode(arr[arr.length-1]);
			node.setWordEndsHere(true);
			map.put(arr[arr.length-1], node);
		}
	}

	public boolean startsWith(String prefix) {
		if (prefix == null || prefix.trim().length() == 0) {
			return false;
		}
		TrieNode itr = root;
		Map<Character,TrieNode> map = itr.getMap();
		char[] arr = prefix.toCharArray();

		for (int i = 0; i < arr.length - 1; i++) {
			if (map.containsKey(arr[i])) {
				itr = map.get(arr[i]);
			}
			else {
				return false;
			}
			map = itr.getMap();
		}

		char last = arr[arr.length-1];
		if (map.containsKey(last)) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean search(String word) {
		if (word == null || word.trim().length() == 0) {
			return false;
		}
		TrieNode itr = root;
		Map<Character,TrieNode> map = itr.getMap();
		char[] arr = word.toCharArray();

		for (int i = 0; i < arr.length - 1; i++) {
			if (map.containsKey(arr[i])) {
				itr = map.get(arr[i]);
			}
			else {
				return false;
			}
			map = itr.getMap();
		}

		char last = arr[arr.length-1];
		if (map.containsKey(last)) {
			TrieNode node = map.get(last);
			return node.isWordEndsHere();
		}
		else {
			return false;
		}
	}
}
