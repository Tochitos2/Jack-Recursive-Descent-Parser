package assign4;

import tokenizer.Keyword;
import tokenizer.Token;
import tokenizer.Tokenizer;

import java.security.Key;

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


        while(isRoutineKind()) {
            parseSubroutineDec();
        }

        // Check for closing bracket
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[]{ '}' });
    }

    /**
     * Checks if the current token is the start of a class variable declaration.
     * @return Returns true if the current token is the static or field keyword.
     */
    private boolean isClassVarDec() {
        return lex.getTokenType() == Token.KEYWORD &&
                (lex.getKeyword() == Keyword.STATIC || lex.getKeyword() == Keyword.FIELD);
    }

    /**
     * Parse a class variable declaration.
     * @throws ParsingFailure on failure.
     */
    private void parseClassVarDec() {
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
    private void parseVarList() {
        validateTokenType(new Token[]{ Token.IDENTIFIER });
        lex.advance();

        // Recursive call if more than one variable.
        if(lex.getTokenType() == Token.SYMBOL && lex.getSymbol() == ',') {
            lex.advance();
            parseVarList();
        }
    }


    private boolean isRoutineKind() {
        return lex.getTokenType() == Token.KEYWORD &&
                (lex.getKeyword() == Keyword.CONSTRUCTOR ||
                        lex.getKeyword() == Keyword.FUNCTION ||
                        lex.getKeyword() == Keyword.METHOD);
    }

    /**
     * subroutineDec ::= routineKind ( VOID | type ) IDENTIFIER
     *  '(' parameterList ? ')' subroutineBody
     */
    private void parseSubroutineDec() {
        parseRoutineKind();

        // if type void advance, else parse as type.
        if(lex.getTokenType() == Token.KEYWORD && lex.getKeyword() == Keyword.VOID) lex.advance();
        else parseType();

        // Check for opening parameters bracket.
        validateTokenType(new Token[]{ Token.IDENTIFIER });
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[]{ '(' });
        lex.advance();

        // If not a symbol atom then must be parameters, so parse them.
        if(lex.getTokenType() != Token.SYMBOL) {
            parseParameterList();
        }
        // Check for closing parameters bracket.
        validateTokenType(new Token[]{ Token.IDENTIFIER });
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[]{ ')' });
        lex.advance();

        parseSubroutineBody();
    }

    private void parseRoutineKind() {
        validateTokenType(new Token[]{ Token.KEYWORD });
        validateKeyWord(new Keyword[]{ Keyword.CONSTRUCTOR, Keyword.FUNCTION, Keyword.METHOD});
        lex.advance();
    }

    private void parseParameterList() {
        // parse first of 1-n parameters.
        parseType();
        validateTokenType(new Token[]{ Token.IDENTIFIER });
        lex.advance();

        // if there are more parameters remaining then recursively call method.
        if(lex.getTokenType() == Token.SYMBOL && lex.getSymbol() == ',') {
            lex.advance();
            parseParameterList();
        }
    }

    private void parseSubroutineBody() {
        // Check for opening curly brace
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[]{ '{' });
        lex.advance();

        //TODO:
        while(isVarDec()) {
            parseVarDec();
        }

        //TODO:
        while(isStatement) {
            parseStatement();
        }

        // Check for closing curly brace
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[]{ '}' });
        lex.advance();
    }

    private boolean isVarDec() {
        return lex.getTokenType() == Token.KEYWORD && lex.getKeyword() == Keyword.VAR;
    }

    private void parseVarDec() {
        // Check for var keyword.
        validateTokenType(new Token[]{ Token.KEYWORD });
        validateKeyWord(new Keyword[]{ Keyword.VAR });
        lex.advance();

        // Check for type declaration.
        parseType();

        // Check for list of identifiers.
        parseVarList();

        // Check for closing semicolon.
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[]{ ';' });
        lex.advance();
    }

    private boolean isStatement() {
        return lex.getTokenType() == Token.KEYWORD &&
                (lex.getKeyword() == Keyword.DO ||
                        lex.getKeyword() == Keyword.IF ||
                        lex.getKeyword() == Keyword.LET ||
                        lex.getKeyword() == Keyword.RETURN ||
                        lex.getKeyword() == Keyword.WHILE);
    }

    private void parseStatement() {
        validateTokenType(new Token[] { Token.KEYWORD });

        switch(lex.getKeyword()) {
            case DO:
                parseDoStatement();
                break;
            case IF:
                parseIfStatement();
                break;
            case LET:
                parseLetStatement();
                break;
            case RETURN:
                parseReturnStatement();
                break;
            case WHILE:
                parseWhileStatement();
                break;
            default:
                throw new ParsingFailure();

        }
    }

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
    private void parseType() {
        validateTokenType(new Token[]{ Token.KEYWORD, Token.IDENTIFIER });

        if(lex.getTokenType() == Token.KEYWORD) validateKeyWord(new Keyword[]{Keyword.INT, Keyword.CHAR, Keyword.BOOLEAN});
        lex.advance();
    }


    private void validateTokenType(Token[] validTypes) {
        for(Token token: validTypes) {
            if(lex.getTokenType() == token) return;
        }
        throw new ParsingFailure();
    }

    private void validateKeyWord(Keyword[] validKeywords) {
        for(Keyword keyword: validKeywords) {
            if(lex.getKeyword() == keyword) return;
        }
        throw new ParsingFailure();
    }

    private void validateSymbol(char[] validSymbols){
        for(char c: validSymbols) {
            if(lex.getSymbol() == c) return;
        }
        throw new ParsingFailure();
    }


}
