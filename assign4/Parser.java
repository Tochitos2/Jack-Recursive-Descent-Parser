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
        // Check first token is the keyword 'class'.
        ValidateType(Token.KEYWORD);
        ValidateKeyWord(Keyword.CLASS);
        lex.advance();

        // Check for class identifier.
        ValidateType(Token.IDENTIFIER);
        lex.advance();

        // Check for opening bracket
        ValidateType(Token.SYMBOL);
        ValidateSymbol('{');
        lex.advance();

        // Check for closing bracket
        ValidateType(Token.SYMBOL);
        ValidateSymbol('}');
        lex.advance();

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
