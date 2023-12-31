package lang.c;

import java.util.HashMap;

public class CTokenRule extends HashMap<String, Object> {
	private static final long serialVersionUID = 1139476411716798082L;

	public CTokenRule() {
		// put("int",	new Integer(CToken.TK_INT));
		//put("true", new Integer(CToken.TK_TRUE));
		//put("false", new Integer(CToken.TK_FALSE));
		put("true", Integer.valueOf(CToken.TK_TRUE));
		put("false", Integer.valueOf(CToken.TK_FALSE));
		put("if", Integer.valueOf(CToken.TK_IF));
		put("while", Integer.valueOf(CToken.TK_WHILE));
		put("input", Integer.valueOf(CToken.TK_INPUT));
		put("output", Integer.valueOf(CToken.TK_OUTPUT));
		put("else", Integer.valueOf(CToken.TK_ELSE));
	}
}
