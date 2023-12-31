package lang.c;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import lang.*;

public class CTokenizer extends Tokenizer<CToken, CParseContext> {
	//@SuppressWarnings("unused")
	private CTokenRule rule;
	private int lineNo, colNo;
	private char backCh;
	private boolean backChExist = false;

	private boolean isMinus; //追加
	private boolean isAddress; //追加

	public CTokenizer(CTokenRule rule) {
		this.rule = rule;
		lineNo = 1;
		colNo = 1;
	}

	private InputStream in;
	private PrintStream err;

	private char readChar() {
		char ch;
		if (backChExist) {
			ch = backCh;
			backChExist = false;
		} else {
			try {
				ch = (char) in.read();
			} catch (IOException e) {
				e.printStackTrace(err);
				ch = (char) -1;
			}
		}
		++colNo;
		if (ch == '\n') {
			colNo = 1;
			++lineNo;
		}
		// System.out.print("'"+ch+"'("+(int)ch+")");
		return ch;
	}

	private void backChar(char c) {
		backCh = c;
		backChExist = true;
		--colNo;
		if (c == '\n') {
			--lineNo;
		}
	}

	// 現在読み込まれているトークンを返す
	private CToken currentTk = null;

	public CToken getCurrentToken(CParseContext pctx) {
		return currentTk;
	}

	// 次のトークンを読んで返す
	public CToken getNextToken(CParseContext pctx) {
		in = pctx.getIOContext().getInStream();
		err = pctx.getIOContext().getErrStream();
		currentTk = readToken();
		return currentTk;
	}

	private CToken readToken() {
		CToken tk = null;
		char ch;
		int startCol = colNo;
		StringBuffer text = new StringBuffer();

		int state = 0;
		boolean accept = false;
		int digit_count = 0;
		while (!accept) {
			switch (state) {
				case 0: // 初期状態
					ch = readChar();
					if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
					} else if (ch == (char) -1) { // EOF
						startCol = colNo - 1;
						state = 1;
					} else if (ch == '0') { // 16 or 8
						startCol = colNo - 1;
						text.append(ch);
						state = 201;
					} else if (ch >= '1' && ch <= '9') { // 10
						startCol = colNo - 1;
						text.append(ch);
						state = 3;
					} else if (ch == '+') {
						startCol = colNo - 1;
						text.append(ch);
						state = 4;
					} else if (ch == '-') {
						startCol = colNo - 1;
						text.append(ch);
						state = 5;
					} else if (ch == '*') {
						startCol = colNo - 1;
						text.append(ch);
						state = 6;
					} else if (ch == '/') {
						startCol = colNo - 1;
						state = 7;
					} else if (ch == '(') {
						startCol = colNo - 1;
						state = 8;
					} else if (ch == ')') {
						startCol = colNo - 1;
						state = 9;
					} else if (ch == '&') {
						startCol = colNo - 1;
						state = 10;
					} else if (ch == '[') {
						startCol = colNo - 1;
						state = 11;
					} else if (ch == ']') {
						startCol = colNo - 1;
						state = 12;
					} else if (ch == '=') {
						startCol = colNo - 1;
						state = 13;
					} else if (ch == ';') {
						startCol = colNo - 1;
						state = 14;
					} else if (ch == '<') {
						startCol = colNo - 1;
						state = 15;
					} else if (ch == '>') {
						startCol = colNo - 1;
						state = 16;
					} else if (ch == '!') {
						text.append(ch);
						startCol = colNo - 1;
						state = 17;
					} else if (ch == '{') {
						text.append(ch);
						startCol = colNo - 1;
						state = 18;
					} else if (ch == '}') {
						text.append(ch);
						startCol = colNo - 1;
						state = 19;
					} else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') { // ident
						startCol = colNo - 1;
						text.append(ch);
						state = 301;
					} else { // ヘンな文字を読んだ
						startCol = colNo - 1;
						text.append(ch);
						state = 2;
					}
					break;
				case 1: // EOFを読んだ
					tk = new CToken(CToken.TK_EOF, lineNo, startCol, "end_of_file");
					accept = true;
					break;
				case 2: // ヘンな文字を読んだ
					tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
					accept = true;
					break;
				case 3: // 10進数
					ch = readChar();
					if (ch >= '0' && ch <= '9') {
						text.append(ch);
					} else if (ch >= 'a' && ch <= 'f') { // 例えば123a4のとき
						backChar(ch); // 数を表さない文字は戻す（読まなかったことにする）// これを入れることでaがappendされるようになった
						tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
						accept = true;
						text.append(ch);
						state = 2;
					}else {
						// 数の終わり
						String text_string = text.toString();
						int text_int = Integer.parseInt(text_string);
						if(isMinus){
							if(text_int <= 32768){
								backChar(ch); // 数を表さない文字は戻す（読まなかったことにする）
								tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
								accept = true;
							} else {
								state = 2;
							}
						}else if(isAddress){
							if(text_int <= 65535){
								backChar(ch); // 数を表さない文字は戻す（読まなかったことにする）
								tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
								accept = true;
							} else {
								state = 2;
							}
						}else{
							if(text_int <= 32768){
								backChar(ch); // 数を表さない文字は戻す（読まなかったことにする）
								tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
								isMinus = false;
								accept = true;
							} else {
								state = 2;
							}
						}
					}
					break;
				case 4: // +を読んだ
					tk = new CToken(CToken.TK_PLUS, lineNo, startCol, "+");
					accept = true;
					break;
				case 5: // -を読んだ
					tk = new CToken(CToken.TK_MINUS, lineNo, startCol, "-");
					isMinus = true;
					accept = true;
					break;
				case 6: // *を読んだ
					tk = new CToken(CToken.TK_MULT, lineNo, startCol, "*");
					accept = true;
					break;
				case 7: // /を読んだ
					ch = readChar();
					if (ch == '/') {
						state = 101;
					} else if (ch == '*') {
						state = 102;
					}else{
						backChar(ch);
						tk = new CToken(CToken.TK_DIV, lineNo, startCol, "/");
						accept = true;
						break;
					}
					break;
				case 8: // (を読んだ
					tk = new CToken(CToken.TK_LPAR, lineNo, startCol, "(");
					accept = true;
					break;
				case 9: // )を読んだ
					tk = new CToken(CToken.TK_RPAR, lineNo, startCol, ")");
					accept = true;
					break;
				case 10: // &を読んだ
					tk = new CToken(CToken.TK_AMP, lineNo, startCol, "&");
					isAddress = true;
					accept = true;
					break;
				case 11: // [を読んだ
					tk = new CToken(CToken.TK_LBRA, lineNo, startCol, "[");
					isAddress = true;
					accept = true;
					break;
				case 12: // ]を読んだ
					tk = new CToken(CToken.TK_RBRA, lineNo, startCol, "]");
					isAddress = true;
					accept = true;
					break;
				case 13: // =を読んだ
					ch = readChar();
					if (ch == '=') {
						state = 403;
					} else {
						backChar(ch);
						tk = new CToken(CToken.TK_ASSIGN, lineNo, startCol, "=");
						isAddress = true;
						accept = true;
						break;
					}
					break;
				case 14: // ;を読んだ
					tk = new CToken(CToken.TK_SEMI, lineNo, startCol, ";");
					isAddress = true;
					accept = true;
					break;
				case 15: // <を読んだ
					ch = readChar();
					if (ch == '=') {
						state = 401;
					} else {
						backChar(ch);
						tk = new CToken(CToken.TK_LT, lineNo, startCol, "<");
						accept = true;
						break;
					}
					break;
				case 16: // >を読んだ
					ch = readChar();
					if (ch == '=') {
						state = 402;
					} else {
						backChar(ch);
						tk = new CToken(CToken.TK_GT, lineNo, startCol, ">");
						accept = true;
						break;
					}
					break;
				case 17: // !を読んだ
					ch = readChar();
					if (ch == '=') {
						state = 404;
					} else {
						backChar(ch);
						state = 2;
					}
					break;
				case 18: // {を読んだ
					tk = new CToken(CToken.TK_LCUR, lineNo, startCol, "{");
					isAddress = true;
					accept = true;
					break;
				case 19: // }を読んだ
					tk = new CToken(CToken.TK_RCUR, lineNo, startCol, "}");
					isAddress = true;
					accept = true;
					break;
				case 101: // /を2連続で読んだ
					ch = readChar();
					if (ch == (char) -1) { // EOF
						state = 1;
					}else if (ch == '\n') { //改行
						state = 0;
					}else{
						state = 101;
					}
					break;
				case 102: // /*を読んだ
					ch = readChar();
					if (ch == (char) -1) { // EOF
						state = 1;
					}else if (ch == '*') { 
						state = 103;
					}else{
						state = 102;
					}
					break;
				case 103: // /**を読んだ
					ch = readChar();
					if (ch == (char) -1) { // EOF
						state = 1;
					}else if (ch == '/') { 
						state = 0;
					}else if (ch == '*') { 
						state = 103;
					}else{ // 閉じていないコメントはEOFが出るはず
						state = 1;
					}
					break;
				case 201: // 16進数 or 8進数
					ch = readChar();
					digit_count = 0; // 8進数のために桁数リセット
					if (ch == 'x') { // 16
						text.append(ch);
						state = 204;
					}else if (ch == '1') { // 8
						digit_count++;
						text.append(ch);
						state = 202;
					}else if (ch >= '2' && ch <= '7') { // 8
						digit_count++;
						text.append(ch);
						state = 203;
					} else { // 0
						backChar(ch); // 数を表さない文字は戻す（読まなかったことにする）
						tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
						accept = true;
					}
					break;
				case 202: // 8進数 最上位が1 0177777が最大
					ch = readChar();
					if(digit_count <= 6){
						if (ch >= '0' && ch <= '7') {
							digit_count++;
							text.append(ch);
						} else {
							// 数の終わり
							backChar(ch); // 数を表さない文字は戻す（読まなかったことにする）
							tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
							accept = true;
						}
				    }else{
						state = 2;
					}
					break;
				case 203: // 8進数 最上位が1 0177777が最大
					ch = readChar();
					if(digit_count <= 5){
						if (ch >= '0' && ch <= '7') {
							digit_count++;
							text.append(ch);
						} else {
							// 数の終わり
							backChar(ch); // 数を表さない文字は戻す（読まなかったことにする）
							tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
							accept = true;
						}
				    }else{
						state = 2;
					}
					break;
				case 204: // 16進数
					ch = readChar();
					digit_count = 0;
					if (ch >= '1' && ch <= '9' || ch >= 'a' && ch <= 'f') {
						digit_count++;
						text.append(ch);
						state = 205;
					} else if(ch == '0') { //0x0
						text.append(ch);
						state = 206;
					} else { // 想定外
						state = 2;
					}
					break;
				case 205: // 16進数
					ch = readChar();
					if(digit_count <= 4){
						if (ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'f') {
							digit_count++;
							text.append(ch);
						} else {
							// 数の終わり
							backChar(ch); // 数を表さない文字は戻す（読まなかったことにする）
							tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
							accept = true;
						}
				    }else{
						state = 2;
					}	
					break;
				case 206: // 16進数
					ch = readChar();
					if (ch >= '1' && ch <= '9' || ch >= 'a' && ch <= 'f') { //0x0...
						state = 1;
				    }else{ // 0x0
						backChar(ch); // 数を表さない文字は戻す（読まなかったことにする）
						tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
						accept = true;
					}	
					break;
				case 301: // ident 二文字目以降
					ch = readChar();
					if ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') {
						text.append(ch);
				    }else{
						// identの終わり
						backChar(ch); // 読まなかったことにする
						String s = text.toString();
						Integer i = (Integer) rule.get(s);
						// 切り出した字句が登録済みキーワードかどうかは i が null かどうかで判定する
						tk = new CToken(((i == null) ? CToken.TK_IDENT : i.intValue()), lineNo, startCol, s);
						// tk = new CToken(CToken.TK_IDENT, lineNo, startCol, text.toString());
						accept = true;
					}	
					break;
				case 401: // <= を読んだ	
					tk = new CToken(CToken.TK_LE, lineNo, startCol, "<=");
					accept = true;
					break;
				case 402: // >= を読んだ	
					tk = new CToken(CToken.TK_GE, lineNo, startCol, ">=");
					accept = true;
					break;
				case 403: // == を読んだ	
					tk = new CToken(CToken.TK_EQ, lineNo, startCol, "==");
					accept = true;
					break;	
				case 404: // != を読んだ	
					tk = new CToken(CToken.TK_NE, lineNo, startCol, "!=");
					accept = true;
					break;	
			}
		}
		return tk;
	}
}
