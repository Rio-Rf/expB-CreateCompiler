package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

class ExpressionAdd extends CParseRule {
	// expressionAdd ::= '+' term
	CToken op;
	CParseRule left, right;

	public ExpressionAdd(CParseContext pcx, CParseRule left) {
		this.left = left;
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_PLUS;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		op = ct.getCurrentToken(pcx);
		// +の次の字句を読む
		CToken tk = ct.getNextToken(pcx);
		if (Term.isFirst(tk)) {
			right = new Term(pcx); //TermクラスはCparseRuleクラスのextend
			right.parse(pcx); // Termのparseチェック
		} else {
			pcx.fatalError(tk.toExplainString() + "+の後ろはtermです");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		// 足し算の型計算規則
		final int s[][] = { // Ctype_hogeはint型 例えばs[0][0]はltが0かつrtが0だからerr
				// T_err T_int T_pint T_int_array T_pint_array
				{ CType.T_err, CType.T_err, CType.T_err, CType.T_err, CType.T_err }, // T_err
				{ CType.T_err, CType.T_int, CType.T_pint, CType.T_err, CType.T_err }, // T_int
				{ CType.T_err, CType.T_pint, CType.T_err, CType.T_err, CType.T_err}, // T_pint
				{ CType.T_err, CType.T_err, CType.T_err, CType.T_err, CType.T_err }, // T_int_array
				{ CType.T_err, CType.T_err, CType.T_err, CType.T_err, CType.T_pint}, // T_pint_array
		};
		if (left != null && right != null) {
			left.semanticCheck(pcx);
			right.semanticCheck(pcx);
			int lt = left.getCType().getType(); // +の左辺の型
			int rt = right.getCType().getType(); // +の右辺の型
			int nt = s[lt][rt]; // 規則による型計算 s[lt][rt] は、左辺が lt 型であり、右辺が rt 型である場合の結果の型を示す
			if (nt == CType.T_err) {
				pcx.fatalError(op.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型["
						+ right.getCType().toString() + "]は足せません");
			}
			this.setCType(CType.getCType(nt));
			this.setConstant(left.isConstant() && right.isConstant()); // +の左右両方が定数のときだけ定数
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		if (left != null && right != null) {
			left.codeGen(pcx); // 左部分木のコード生成を頼む
			right.codeGen(pcx); // 右部分木のコード生成を頼む
			o.println("\tMOV\t-(R6), R0\t; ExpressionAdd: ２数を取り出して、足し、積む<" + op.toExplainString() + ">");
			o.println("\tMOV\t-(R6), R1\t; ExpressionAdd:");
			o.println("\tADD\tR1, R0\t; ExpressionAdd:");
			o.println("\tMOV\tR0, (R6)+\t; ExpressionAdd:");
		}
	}
}