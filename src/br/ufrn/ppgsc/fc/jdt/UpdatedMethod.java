package br.ufrn.ppgsc.fc.jdt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdatedMethod {

	private List<UpdatedLine> lines;
	private MethodLimit limit;
	private String klass;

	public UpdatedMethod(MethodLimit limit, String klass) {
		lines = new ArrayList<UpdatedLine>();
		this.limit = limit;
		this.klass = klass;
	}

	public void addUpdatedLine(UpdatedLine line) {
		lines.add(line);
	}

	public List<UpdatedLine> getUpdatedLines() {
		return Collections.unmodifiableList(lines);
	}

	public MethodLimit getMethodLimit() {
		return limit;
	}

	public String getKlass() {
		return klass;
	}
}
