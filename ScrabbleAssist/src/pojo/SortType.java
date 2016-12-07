package pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape= JsonFormat.Shape.OBJECT)
public enum SortType {
	BEST_SCORE("best_score"), LONGEST_WORD("longest_word");
	private String type;

	@JsonCreator
	private SortType(String type) {
		this.type = type;
	}
	@JsonValue
	private String getType() {
		return type;
	}

}
