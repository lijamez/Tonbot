package com.tonberry.tonbot.modules.time;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
class Pod {

	private final String title;
	private final String scanner;
	private final String id;
	private final boolean error;
	private final List<Subpod> subpods;

	@JsonCreator
	public Pod(
			@JsonProperty("title") String title,
			@JsonProperty("scanner") String scanner,
			@JsonProperty("id") String id,
			@JsonProperty("error") boolean error,
			@JsonProperty("subpods") List<Subpod> subpods) {

		this.title = title;
		this.scanner = scanner;
		this.id = id;
		this.error = error;
		this.subpods = subpods;
	}

}
