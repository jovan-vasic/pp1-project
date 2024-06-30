package rs.ac.bg.etf.pp1;

import java_cup.runtime.Symbol;

%%

%{

	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type) {
		return new Symbol(type, yyline+1, yycolumn);
	}
	
	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type, Object value) {
		return new Symbol(type, yyline+1, yycolumn, value);
	}

%}

%cup
%line
%column

%xstate COMMENT

%eofval{
	return new_symbol(sym.EOF);
%eofval}

%%

" " 						{ }
"\b" 						{ }
"\t" 						{ }
"\r\n" 						{ }
"\f" 						{ }

"program"   				{ return new_symbol(sym.PROG, yytext());}
"const"   					{ return new_symbol(sym.CONST, yytext());}
"new"   					{ return new_symbol(sym.NEW, yytext());}
"print" 					{ return new_symbol(sym.PRINT, yytext()); }
"read"   					{ return new_symbol(sym.READ, yytext());}
"return" 					{ return new_symbol(sym.RETURN, yytext()); }
"void" 						{ return new_symbol(sym.VOID, yytext()); }
"range" 					{ return new_symbol(sym.RANGE, yytext()); }

"'"."'"						{ return new_symbol(sym.CHARCONST, Character.valueOf(yytext().charAt(1))); }
"true" | "false"			{ return new_symbol(sym.BOOLCONST, Boolean.valueOf(yytext())); }
([a-z]|[A-Z])[a-zA-Z0-9_]* 	{ return new_symbol(sym.IDENT, yytext()); }
[0-9]+  					{ return new_symbol(sym.NUMCONST, Integer.valueOf(yytext())); }

"+" 						{ return new_symbol(sym.PLUS, yytext()); }
"-"							{ return new_symbol(sym.MINUS, yytext()); }
"*"							{ return new_symbol(sym.MUL, yytext()); }
"/"							{ return new_symbol(sym.DIV, yytext()); }
"%"							{ return new_symbol(sym.MOD, yytext()); }
"=="						{ return new_symbol(sym.EQ, yytext()); }
"!="						{ return new_symbol(sym.NEQ, yytext()); }
">"							{ return new_symbol(sym.GT, yytext()); }
">="						{ return new_symbol(sym.GEQ, yytext()); }
"<"							{ return new_symbol(sym.LT, yytext()); }
"<="						{ return new_symbol(sym.LEQ, yytext()); }
"&&"						{ return new_symbol(sym.AND, yytext()); }
"||"						{ return new_symbol(sym.OR, yytext()); }
"=" 						{ return new_symbol(sym.EQUAL, yytext()); }
"++"						{ return new_symbol(sym.INC, yytext()); }
"--"						{ return new_symbol(sym.DEC, yytext()); }
";" 						{ return new_symbol(sym.SEMI, yytext()); }
"::"						{ return new_symbol(sym.DCOLON, yytext()); }
":"							{ return new_symbol(sym.COLON, yytext()); }
"," 						{ return new_symbol(sym.COMMA, yytext()); }
"."							{ return new_symbol(sym.DOT, yytext()); }
"(" 						{ return new_symbol(sym.LPAREN, yytext()); }
")" 						{ return new_symbol(sym.RPAREN, yytext()); }
"["							{ return new_symbol(sym.LANGLE, yytext()); }
"]"							{ return new_symbol(sym.RANGLE, yytext()); }
"{" 						{ return new_symbol(sym.LBRACE, yytext()); }
"}"							{ return new_symbol(sym.RBRACE, yytext()); }
"=>"						{ return new_symbol(sym.LAMBDA, yytext()); }

"//" 		   				{ yybegin(COMMENT); }
<COMMENT> .      			{ yybegin(COMMENT); }
<COMMENT> "\r\n" 			{ yybegin(YYINITIAL); }

. 							{ System.err.println("Leksicka greska ("+yytext()+") u liniji "+(yyline+1)); }