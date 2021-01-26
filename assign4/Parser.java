package assign4;

import tokenizer.Keyword;
import tokenizer.Token;
import tokenizer.Tokenizer;

/**
 * Parse a Jack source file.
 * 
 * @author
 * @version 
 */
public class Parser {
    // The tokenizer.
    private final Tokenizer lex;
    
    /**
     * Parse a Jack source file.
     * @param lex The tokenizer.
     */
    public Parser(Tokenizer lex)
    {
        this.lex = lex;
    }
    
    /**
     * Parse a Jack class file.
     * @throws ParsingFailure on failure.
     */
    public void parseClass() {
        // Move to first token.
        lex.advance();

        // Check first token is the keyword 'class'.
        validateTokenType(new Token[]{ Token.KEYWORD });
        validateKeyWord(new Keyword[]{ Keyword.CLASS });
        lex.advance();

        // Check for class identifier.
        validateTokenType(new Token[]{ Token.IDENTIFIER });
        lex.advance();

        // Check for opening bracket
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[]{ '{' });
        lex.advance();

        // Parse 0 or more class variable declarations.
        while(isClassVarDec()) {
            parseClassVarDec();
        }

        //TODO: Parse 0 or more subroutine declarations...
//        while(isSubroutineDec()) {
//            parseSubroutineDec();
//        }

        // Check for closing bracket
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[]{ '}' });
    }

    /**
     * Checks if the current token is the start of a class variable declaration.
     * @return Returns true if the current token is the static or field keyword.
     */
    public boolean isClassVarDec() {
        return lex.getTokenType() == Token.KEYWORD &&
                (lex.getKeyword() == Keyword.STATIC || lex.getKeyword() == Keyword.FIELD);
    }

    /**
     * Parse a class variable declaration.
     * @throws ParsingFailure on failure.
     */
    public void parseClassVarDec() {
        validateTokenType(new Token[]{ Token.KEYWORD });
        validateKeyWord(new Keyword[]{ Keyword.FIELD, Keyword.STATIC });
        lex.advance();

        // Parse data type.
        parseType();

        // parse variable declaration list.
        parseVarList();

        // Parse ending semicolon.
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { ';' });

        lex.advance();
    }

    /**
     * varList ::= IDENTIFIER ( ',' varList ) ?
     */
    public void parseVarList() {
        validateTokenType(new Token[]{ Token.IDENTIFIER });
        lex.advance();

        // Recursive call if more than one variable.
        if(lex.getTokenType() == Token.SYMBOL && lex.getSymbol() == ',') {
            lex.advance();
            parseVarList();
        }
    }


//    public boolean isSubroutineDec() {
//        return lex.getTokenType() == Token.KEYWORD &&
//                (lex.getKeyword() == Keyword.VOID || lex.getKeyword() == Keyword.Ty);
//    }
    
    /**
     * A ParsingFailure exception is thrown on any form of
     * error detected during the parse.
     */
    public static class ParsingFailure extends RuntimeException
    {

    }

    /**
     *  A type must be either a keyword (int, char, boolean) or and identifier.
     */
    public void parseType() {
        validateTokenType(new Token[]{ Token.KEYWORD, Token.IDENTIFIER });

        if(lex.getTokenType() == Token.KEYWORD) validateKeyWord(new Keyword[]{Keyword.INT, Keyword.CHAR, Keyword.BOOLEAN});
        lex.advance();
    }


    public void validateTokenType(Token[] validTypes) {
        for(Token token: validTypes) {
            if(lex.getTokenType() == token) return;
        }
        throw new ParsingFailure();
    }

    public void validateKeyWord(Keyword[] validKeywords) {
        for(Keyword keyword: validKeywords) {
            if(lex.getKeyword() == keyword) return;
        }
        throw new ParsingFailure();
    }

    public void validateSymbol(char[] validSymbols){
        for(char c: validSymbols) {
            if(lex.getSymbol() == c) return;
        }
        throw new ParsingFailure();
    }


}
