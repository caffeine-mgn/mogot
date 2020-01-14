package pw.binom.material.psi;

import pw.binom.material.psi.TokenType;
import pw.binom.material.psi.IElementType;


%%
%public
%class GLSLLexer
%unicode
%line
%column
%function advance
%type TokenType
%eof{  return;
%eof}
%{
public int getLine(){return yyline; }
public int getColumn(){return yycolumn; }
public String getText(){return yytext(); }
%}
%{
      private int readed = 0;
      private int startString;
      public int getPosition(){return readed-yylength(); }
      TokenType next() throws java.io.IOException{
          TokenType out = advance();
          if (out !=null)
              readed += yylength();
          return out;
      }

      private StringBuffer string = new StringBuffer();

      public String stringLiteral(){
            return string.toString();
      }
%}

CRLF=[\r\n]
WHITE_SPACE=[\ \t\f]
FIRST_VALUE_CHARACTER=[^ '\"\r\n\f\\{};\/\d\(\)=\+\-*\/\^%,!<>\.\[\]]|[a-zA-Z]
VALUE_CHARACTER=[^ '\"\r\n\f\\{};\/\(\)=\+\-*\/\^%,!<>\.\[\]]|[a-zA-Z0-9]
END_OF_LINE_COMMENT=("//")[^\r\n]*
DERECTIVE=("#")[^\r\n]*
COMMENT_BLOCK=\/\*[\s\S]*\*\/
SEPARATOR=[:=]
KEY_CHARACTER=[^:=\ \n\t\f\\0123456789,\.] | "\\ "
ID={FIRST_VALUE_CHARACTER}{VALUE_CHARACTER}*

mDIGIT = [0-9]*
mEXPONENT = (e | E)("+" | "-")? [0-9] ("_"? [0-9])*
mFLOAT_SUFFIX = f | F
mDOUBLE_SUFFIX = d | D
mNUM_FLOAT = {mDIGIT} ("." {mDIGIT})? {mEXPONENT}? {mFLOAT_SUFFIX}
mNUM_DOUBLE = {mDIGIT} ("." {mDIGIT})? {mEXPONENT}? {mDOUBLE_SUFFIX}
mNUM_ALL = ((\d+)(\.\d+)?[fd]?)|((\.\d+)[fd]?)
QUOTE = \"
%state WAITING_VALUE
DOT = \.
%state STRING
%%

<YYINITIAL> {
//<YYINITIAL> {END_OF_LINE_COMMENT}                           { yybegin(YYINITIAL); return TokenType.COMMENT; }
//<YYINITIAL> ({mNUM_FLOAT}|{mNUM_DOUBLE}|{mDIGIT}) { return TokenType.NUMBER; }
<YYINITIAL> {mNUM_ALL} { return TokenType.NUMBER; }
//<YYINITIAL> {KEY_CHARACTER}+                                { yybegin(YYINITIAL); return TokenType.KEY; }
{DERECTIVE} { return TokenType.DERECTIVE; }
"vec3" { return TokenType.VEC3; }
"mat3" { return TokenType.MAT3; }
"mat4" { return TokenType.MAT4; }
"float" { return TokenType.FLOAT; }
"bool" { return TokenType.BOOL; }
"true" { return TokenType.TRUE; }
"false" { return TokenType.FALSE; }
"int" { return TokenType.INT; }
"if" { return TokenType.IF; }
"else" { return TokenType.ELSE; }
"vec4" { return TokenType.VEC4; }
"vec2" { return TokenType.VEC2; }
"void" { return TokenType.VOID; }
"return" { return TokenType.RETURN; }
"class" {return TokenType.CLASS; }
"in" {return TokenType.IN;}

"+=" {return TokenType.ASSIGN_PLUS;}
"-=" {return TokenType.ASSIGN_MINUS;}
"*=" {return TokenType.ASSIGN_TIMES;}
"/=" {return TokenType.ASSIGN_DIV;}

"=" {return TokenType.ASSIGN;}
"+" {return TokenType.OP_PLUS;}
"!=" {return TokenType.OP_NE;}
"==" {return TokenType.OP_EQ;}
"++" {return TokenType.INC;}
"--" {return TokenType.DEC;}
"-" {return TokenType.OP_MINUS;}
"*" {return TokenType.OP_TIMES;}
"/" {return TokenType.OP_DIV;}
"!" {return TokenType.NOT;}
"(" {return TokenType.LEFT_PARENTHESIS;}
")" {return TokenType.RIGHT_PARENTHESIS;}
"[" {return TokenType.LEFT_INDEX;}
"]" {return TokenType.RIGHT_INDEX;}
{DOT} {return TokenType.DOT;}
"," {return TokenType.COMMA;}
"<" {return TokenType.OP_LT;}
">" {return TokenType.OP_GT;}
"<=" {return TokenType.OP_LE;}
">=" {return TokenType.OP_GE;}
"out" {return TokenType.OUT;}
"for" {return TokenType.FOR;}
"@property" {return TokenType.PROPERTY;}
"@vertex" {return TokenType.VERTEX;}
"@normal" {return TokenType.NORMAL;}
"@uv" {return TokenType.UV;}
"@model" {return TokenType.MODEL;}
"@projection" {return TokenType.PROJECTION;}
"uniform" {return TokenType.UNIFORM;}
{END_OF_LINE_COMMENT} { return TokenType.COMMENT_LINE; }
{COMMENT_BLOCK} { return TokenType.COMMENT_BLOCK; }
//<WAITING_VALUE> {CRLF}({CRLF}|{WHITE_SPACE})+               { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
//(({WHITE_SPACE}+) | ({CRLF}({CRLF}|{WHITE_SPACE})+)) { return TokenType.WHITE_SPACE; }
{ID} {return TokenType.ID; }
"{" { return TokenType.LEFT_BRACE; }
"}" { return TokenType.RIGHT_BRACE; }

({CRLF})+ {return TokenType.END_LINE;}
";" {return TokenType.CMD_SEPARATOR;}

//<WAITING_VALUE> {WHITE_SPACE}+                              { yybegin(WAITING_VALUE); return TokenType.WHITE_SPACE; }

//<WAITING_VALUE> {FIRST_VALUE_CHARACTER}{VALUE_CHARACTER}*   { yybegin(YYINITIAL); return TokenType.VALUE; }

({WHITE_SPACE})+                                     { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
({QUOTE})       { string.setLength(0); startString=zzStartRead; yybegin(STRING); }
}
<STRING> {
      ({QUOTE})                      { yybegin(YYINITIAL); zzStartRead=startString;return TokenType.STRING; }
      [^\n\r\"\\]+                   { string.append( yytext() ); }
      \\t                            { string.append('\t'); }
      \\n                            { string.append('\n'); }

      \\r                            { string.append('\r'); }
      \\\"                           { string.append('\"'); }
      \\                             { string.append('\\'); }
    }

[^]                                                         { return TokenType.BAD_CHARACTER; }