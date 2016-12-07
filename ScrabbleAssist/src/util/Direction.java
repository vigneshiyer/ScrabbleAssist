package util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape= JsonFormat.Shape.OBJECT)
public enum Direction {
	VERTICAL("vertical"),
	HORIZONTAL("horizontal");
	private String direction;

	@JsonValue
	public String getDirection() {
		return direction;
	}

	@JsonCreator
	private Direction(String direction) {
		this.direction = direction;
	}
}
