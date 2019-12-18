package pw.binom.glsl;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import pw.binom.glsl.psi.GLSLTypes;
import com.intellij.lexer.FlexLexer;

%%

%class GLSLLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}
%{
      private int startString;
      private StringBuffer string = new StringBuffer();

      public String stringLiteral(){
            return string.toString();
      }
%}

CRLF=\R
WHITE_SPACE=[\ \n\t\f]
FIRST_VALUE_CHARACTER=[^ \'\"\n\f\\{};\/\d\(\)=\+\-*\/\^%,!<>\.\[\]@]|[a-zA-Z]
VALUE_CHARACTER=[^ \'\"\n\f\\{};\/\(\)=\+\-*\/\^%,!<>\.\[\]@]|[a-zA-Z0-9]
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
//<YYINITIAL> {END_OF_LINE_COMMENT}                           { yybegin(YYINITIAL); return GLSLTypes.COMMENT; }
//<YYINITIAL> ({mNUM_FLOAT}|{mNUM_DOUBLE}|{mDIGIT}) { return GLSLTypes.NUMBER; }
<YYINITIAL> {mNUM_ALL} { return GLSLTypes.NUMBER; }
//<YYINITIAL> {KEY_CHARACTER}+                                { yybegin(YYINITIAL); return GLSLTypes.KEY; }
{DERECTIVE} { return GLSLTypes.DERECTIVE; }
"@vertex" { return GLSLTypes.ANN_VERTEX; }
"@normal" { return GLSLTypes.ANN_NORMAL; }
"@uv" { return GLSLTypes.ANN_UV; }
"@projection" { return GLSLTypes.ANN_PROJECTION; }
"@model" { return GLSLTypes.ANN_MODEL; }
"@property" { return GLSLTypes.ANN_PROPERTY; }
"vec2" { return GLSLTypes.VEC2; }
"vec3" { return GLSLTypes.VEC3; }
"mat3" { return GLSLTypes.MAT3; }
"mat4" { return GLSLTypes.MAT4; }
"float" { return GLSLTypes.FLOAT; }
"int" { return GLSLTypes.INT; }
"bool" { return GLSLTypes.BOOL; }
"vec4" { return GLSLTypes.VEC4; }
"void" { return GLSLTypes.VOID; }
"return" { return GLSLTypes.RETURN; }
"class" {return GLSLTypes.CLASS; }

"+=" {return GLSLTypes.ASSIGN_PLUS;}
"-=" {return GLSLTypes.ASSIGN_MINUS;}
"*=" {return GLSLTypes.ASSIGN_TIMES;}
"/=" {return GLSLTypes.ASSIGN_DIV;}

"=" {return GLSLTypes.ASSIGN;}
"+" {return GLSLTypes.OP_PLUS;}
"++" {return GLSLTypes.UNARY_PLUS;}
"--" {return GLSLTypes.UNARY_MINUS;}
"-" {return GLSLTypes.OP_MINUS;}
"*" {return GLSLTypes.OP_TIMES;}
"/" {return GLSLTypes.OP_DIV;}
"!" {return GLSLTypes.NOT;}
"(" {return GLSLTypes.LEFT_PARENTHESIS;}
")" {return GLSLTypes.RIGHT_PARENTHESIS;}
"[" {return GLSLTypes.LEFT_INDEX;}
"]" {return GLSLTypes.RIGHT_INDEX;}
{DOT} {return GLSLTypes.DOT;}
"," {return GLSLTypes.COMMA;}
"<" {return GLSLTypes.OP_LT;}
">" {return GLSLTypes.OP_GT;}
"<=" {return GLSLTypes.OP_LE;}
">=" {return GLSLTypes.OP_GE;}
"for" {return GLSLTypes.FOR;}
{END_OF_LINE_COMMENT} { return GLSLTypes.COMMENT_LINE; }
{COMMENT_BLOCK} { return GLSLTypes.COMMENT_BLOCK; }
//<WAITING_VALUE> {CRLF}({CRLF}|{WHITE_SPACE})+               { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
//(({WHITE_SPACE}+) | ({CRLF}({CRLF}|{WHITE_SPACE})+)) { return TokenType.WHITE_SPACE; }
{ID} {return GLSLTypes.ID; }
"{" { return GLSLTypes.LEFT_BRACE; }
"}" { return GLSLTypes.RIGHT_BRACE; }
";" {return GLSLTypes.END_LINE;}
({CRLF})+ {return TokenType.WHITE_SPACE;}
({WHITE_SPACE}+) {return TokenType.WHITE_SPACE;}
<WAITING_VALUE> {WHITE_SPACE}+                              { yybegin(WAITING_VALUE); return TokenType.WHITE_SPACE; }

//<WAITING_VALUE> {FIRST_VALUE_CHARACTER}{VALUE_CHARACTER}*   { yybegin(YYINITIAL); return GLSLTypes.VALUE; }

({CRLF}|{WHITE_SPACE})+                                     { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
({QUOTE})       { string.setLength(0); startString=zzStartRead; yybegin(STRING); }
}
<STRING> {
      ({QUOTE})                      { yybegin(YYINITIAL); zzStartRead=startString;return GLSLTypes.STRING; }
      [^\n\r\"\\]+                   { string.append( yytext() ); }
      \\t                            { string.append('\t'); }
      \\n                            { string.append('\n'); }

      \\r                            { string.append('\r'); }
      \\\"                           { string.append('\"'); }
      \\                             { string.append('\\'); }
    }

[^]                                                         { return TokenType.BAD_CHARACTER; }