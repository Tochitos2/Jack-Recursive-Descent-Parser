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
        ValidateType(Token.KEYWORD);
        ValidateKeyWord(Keyword.CLASS);
        lex.advance();

        // Check for class identifier.
        ValidateType(Token.IDENTIFIER);
        lex.advance();

        // Parse 0 or more class variable declarations.
        while(isClassVarDec()) {
            parseClassVarDec();
        }

        //TODO: Parse 0 or more subroutine declarations...

        // Check for opening bracket
        ValidateType(Token.SYMBOL);
        ValidateSymbol('{');
        lex.advance();

        // Check for closing bracket
        ValidateType(Token.SYMBOL);
        ValidateSymbol('}');
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

    }
    
    /**
     * A ParsingFailure exception is thrown on any form of
     * error detected during the parse.
     */
    public static class ParsingFailure extends RuntimeException
    {

    }

    public void ValidateType(Token expectedType) {
        if(lex.getTokenType() != expectedType) throw new ParsingFailure();
    }

    public void ValidateKeyWord(Keyword keyword) {
        if(lex.getKeyword() != keyword) throw new ParsingFailure();
    }

    public void ValidateSymbol(char symbol){
        if(lex.getSymbol() != symbol) throw new ParsingFailure();
    }

}
