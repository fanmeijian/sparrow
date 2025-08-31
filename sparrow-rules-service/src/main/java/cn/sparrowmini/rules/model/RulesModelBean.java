package cn.sparrowmini.rules.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RulesModelBean {
	private String name;
	private String className;
	private List<Attribute> attributes = new ArrayList<>();
	
	public RulesModelBean(String name, String className) {
		this.name = name;
		this.className = className;
	}

	@AllArgsConstructor
	@Data
	@NoArgsConstructor
	public static class Attribute {
		private String name;
		private String field;
	}
}
