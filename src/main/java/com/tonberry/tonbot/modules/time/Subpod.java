package com.tonberry.tonbot.modules.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
class Subpod {

	private final String title;
	private final Image image;
	private final String plaintext;

	@JsonCreator
	public Subpod(
			@JsonProperty("title") String title,
			@JsonProperty("img") Image image,
			@JsonProperty("plaintext") String plaintext) {

		this.title = title;
		this.image = image;
		this.plaintext = plaintext;
	}

}
