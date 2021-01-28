package assign4;

import assign4.SymbolTable.Kind;
import tokenizer.Keyword;
import tokenizer.Token;
import tokenizer.Tokenizer;

import java.util.ArrayList;

/**
 * Parse a Jack source file.
 * 
 * @author Tomas Angus De Haro, ta527
 * @version 1.0
 */
public class Parser {
    // The tokenizer.
    private final Tokenizer lex;
    private final SymbolTable symbolTable;
    
    /**
     * Parse a Jack source file.
     * @param lex The tokenizer.
     */
    public Parser(Tokenizer lex)
    {
        this.lex = lex;
        this.symbolTable = new SymbolTable();
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
        //Store class name in symbol table.
        symbolTable.define(lex.getIdentifier(), "", Kind.CLASS);
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
        // Get kind to store
        SymbolTable.Kind kind = lex.getKeyword() == Keyword.FIELD ? SymbolTable.Kind.FIELD : SymbolTable.Kind.STATIC;
        lex.advance();

        // Parse data type and store for definition in symbol table.
        String type = parseType();


        // parse variable declaration list and store identifiers.
        ArrayList<String> identifiers = parseVarList();

        //Store the class variables.
        for (String identifier : identifiers) {
            symbolTable.define(identifier, type, kind);
        }

        // Parse ending semicolon.
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { ';' });

        lex.advance();
    }

    /**
     * varList ::= IDENTIFIER ( ',' varList ) ?
     */
    private ArrayList<String> parseVarList() {
        validateTokenType(new Token[]{ Token.IDENTIFIER });
        // Store identity name.
        ArrayList<String> varNames = new ArrayList<>();
        varNames.add(lex.getIdentifier());
        lex.advance();

        // Recursive call if more than one variable.
        if(lex.getTokenType() == Token.SYMBOL && lex.getSymbol() == ',') {
            lex.advance();
            // Add all further identifiers to list
            varNames.addAll(parseVarList());
        }
        return varNames;
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
        // Start new subroutine local scope.
        symbolTable.startSubroutine();

        String type;
        // if type void advance, else parse as type.
        if(lex.getTokenType() == Token.KEYWORD && lex.getKeyword() == Keyword.VOID) {
            type = "void";
            lex.advance();
        }
        else type = parseType();


        validateTokenType(new Token[]{ Token.IDENTIFIER });
        // Store function name in symbol table.
        symbolTable.define(lex.getIdentifier(), type, Kind.FUNC);
        lex.advance();
        // Check for opening parameters bracket.
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[]{ '(' });
        lex.advance();

        // If not a symbol atom then must be parameters, so parse and store them in the symbol table.
        if(lex.getTokenType() != Token.SYMBOL) {
            ArrayList<Identity> parameters = parseParameterList();
            parameters.forEach((identity -> symbolTable.define(identity.name, identity.type, identity.kind)));
        }
        // Check for closing parameters bracket.
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

    private ArrayList<Identity> parseParameterList() {
        // parse first of 1-n parameters.
        String type = parseType();
        validateTokenType(new Token[]{ Token.IDENTIFIER });
        String identifier = lex.getIdentifier();
        lex.advance();

        //store first parameter.
        ArrayList<Identity> parameters = new ArrayList<>();
        parameters.add(new Identity(identifier, type, SymbolTable.Kind.ARG));

        // if there are more parameters remaining then recursively call method.
        if(lex.getTokenType() == Token.SYMBOL && lex.getSymbol() == ',') {
            lex.advance();
            parameters.addAll(parseParameterList());
        }
        return parameters;
    }

    private void parseSubroutineBody() {
        // Check for opening curly brace
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[]{ '{' });
        lex.advance();

        // Parse 0-n local variable declarations.
        while(isVarDec()) {
            parseVarDec();
        }

        // Parse 0-n statements.
        while(isStatement()) {
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

        // Check for and store type.
        String type = parseType();

        // Check for and store list of identifiers.
        ArrayList<String> identifiers = parseVarList();

        // Store variables in symbol table.
        identifiers.forEach(identity -> symbolTable.define(identity, type, Kind.VAR));

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

        switch (lex.getKeyword()) {
            case DO -> parseDoStatement();
            case IF -> parseIfStatement();
            case LET -> parseLetStatement();
            case RETURN -> parseReturnStatement();
            case WHILE -> parseWhileStatement();
            default -> throw new ParsingFailure();
        }
    }

    private void parseDoStatement() {
        validateTokenType(new Token[]{ Token.KEYWORD });
        validateKeyWord(new Keyword[]{ Keyword.DO });
        lex.advance();

        parseSubroutineCall(Character.MIN_VALUE);

        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { ';' });
        lex.advance();
    }

    private void parseIfStatement() {
        validateTokenType(new Token[]{ Token.KEYWORD });
        validateKeyWord(new Keyword[]{ Keyword.IF });
        lex.advance();

        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { '(' });
        lex.advance();

        parseExpression();

        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { ')' });
        lex.advance();

        // Parse if statement block
        parseBlock();

        // parse optional else statement block
        if(lex.getTokenType() == Token.KEYWORD && lex.getKeyword() == Keyword.ELSE) {
            validateTokenType(new Token[]{ Token.KEYWORD });
            validateKeyWord(new Keyword[]{ Keyword.ELSE });
            lex.advance();

            parseBlock();
        }
    }

    private void parseLetStatement() {
        validateTokenType(new Token[]{ Token.KEYWORD });
        validateKeyWord(new Keyword[]{ Keyword.LET });
        lex.advance();

        validateTokenType(new Token[]{ Token.IDENTIFIER });
        //Check variable has been declared.
        String identifier = lex.getIdentifier();
        if(!symbolTable.isDefined(identifier)) throw new ParsingFailure();
        lex.advance();

        // Parse optional identifier index.
        if(lex.getTokenType() == Token.SYMBOL && lex.getSymbol() == '[') {
            //Check variable is of type array.
            if(symbolTable.typeOf(identifier) != "Array") throw new ParsingFailure();

            validateTokenType(new Token[]{ Token.SYMBOL });
            validateSymbol(new char[] { '[' });
            lex.advance();

            parseExpression();

            validateTokenType(new Token[]{ Token.SYMBOL });
            validateSymbol(new char[] { ']' });
            lex.advance();
        }

        // Assignment operator
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { '=' });
        lex.advance();

        parseExpression();

        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { ';' });
        lex.advance();
    }

    private void parseReturnStatement() {
        validateTokenType(new Token[]{ Token.KEYWORD });
        validateKeyWord(new Keyword[]{ Keyword.RETURN });
        lex.advance();

        // If rule termination is not found then expect an expression.
        if(lex.getTokenType() != Token.SYMBOL || lex.getSymbol() != ';') {
            parseExpression();
        }

        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { ';' });
        lex.advance();
    }

    private void parseWhileStatement() {
        validateTokenType(new Token[]{ Token.KEYWORD });
        validateKeyWord(new Keyword[]{ Keyword.WHILE });
        lex.advance();

        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { '(' });
        lex.advance();

        parseExpression();

        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { ')' });
        lex.advance();

        // Parse if statement block
        parseBlock();
    }

    private void parseSubroutineCall(char c) {
        parseSubroutineReference(c);


        // Opening subroutine parameter bracket
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { '(' });
        lex.advance();

        if(lex.getTokenType() != Token.SYMBOL || lex.getSymbol() != ';') {
            parseExpressionList();
        }

        // Closing subroutine parameter bracket
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { ')' });
        lex.advance();
    }

    private void parseExpressionList() {
        parseExpression();

        if(lex.getTokenType() == Token.SYMBOL && lex.getSymbol() == ',') {
            lex.advance();
            parseExpressionList();
        }
    }

    private void parseSubroutineReference(char c) {
        if(c != '.' && c!= '(') {
            validateTokenType(new Token[]{ Token.IDENTIFIER });
            lex.advance();

            // Optional additional identifier
            if(lex.getTokenType() == Token.SYMBOL && lex.getSymbol() == '.') {
                validateTokenType(new Token[]{Token.SYMBOL});
                validateSymbol(new char[]{'.'});
                lex.advance();

                validateTokenType(new Token[]{Token.IDENTIFIER});
                lex.advance();
            }
        }
        else if(c == '.') {
            lex.advance();
            validateTokenType(new Token[]{Token.IDENTIFIER});
            lex.advance();
        }
    }

    private void parseBlock() {
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { '{' });
        lex.advance();

        // Parse 0-n statements.
        while(isStatement()) {
            parseStatement();
        }

        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { '}' });
        lex.advance();
    }

    private void parseExpression() {
        parseTerm();

        if(isBinaryOp()) {
            parseBinaryOp();

            parseExpression();
        }
    }

    private boolean isBinaryOp() {
        if(lex.getTokenType() == Token.SYMBOL) {
            return switch (lex.getSymbol()) {
                case '+', '-', '*', '/', '&', '|', '<', '=', '>' -> true;
                default -> false;
            };
        }
        return false;
    }

    private void parseBinaryOp() {
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[]{ '+', '-', '*', '/', '&', '|', '<', '=', '>' });
        lex.advance();
    }

    private void parseTerm() {
        if(lex.getTokenType() == Token.INT_CONST){ lex.advance(); return; }
        if(lex.getTokenType() == Token.STRING_CONST){ lex.advance(); return; }
        if(isUnaryOp()) {
            parseUnaryOp();
            parseTerm();
            return;
        }
        if(isKeywordConstant()) {
            parseKeywordConstant();
            return;
        }
        if(lex.getTokenType() == Token.SYMBOL && lex.getSymbol() == '(') {
            validateTokenType(new Token[]{ Token.SYMBOL });
            validateSymbol(new char[] { '(' });
            lex.advance();

            parseExpression();

            validateTokenType(new Token[]{ Token.SYMBOL });
            validateSymbol(new char[] { ')' });
            lex.advance();
            return;
        }
        if(lex.getTokenType() == Token.IDENTIFIER) {
            String identifier = lex.getIdentifier();
            //Check variable has been declared.
            if(!symbolTable.isDefined(identifier)) throw new ParsingFailure();
            lex.advance();

            // Check for optional IDENTIFIER ( '[' expression ']' ) ?
            if(lex.getTokenType() == Token.SYMBOL && lex.getSymbol() == '[') {

                //Check variable is of type array.
                if(symbolTable.typeOf(identifier) != "Array") throw new ParsingFailure();

                // Parse index
                validateTokenType(new Token[]{ Token.SYMBOL });
                validateSymbol(new char[] { '[' });
                lex.advance();

                parseExpression();

                validateTokenType(new Token[]{ Token.SYMBOL });
                validateSymbol(new char[] { ']' });
                lex.advance();
                return;
            }
            // if the code is a subroutine call
            if(lex.getTokenType() == Token.SYMBOL && (lex.getSymbol() == '.' || lex.getSymbol() == '(')) {
                parseSubroutineCall(lex.getSymbol());
            }

        }

    }

    private void parseKeywordConstant() {
        validateTokenType(new Token[]{ Token.KEYWORD });
        validateKeyWord(new Keyword[]{ Keyword.TRUE, Keyword.FALSE, Keyword.NULL, Keyword.THIS });
        lex.advance();
    }

    private boolean isKeywordConstant() {
        return lex.getTokenType() == Token.KEYWORD &&
                (lex.getKeyword() == Keyword.TRUE ||
                        lex.getKeyword() == Keyword.FALSE ||
                        lex.getKeyword() == Keyword.NULL ||
                        lex.getKeyword() == Keyword.THIS );
    }

    private void parseUnaryOp() {
        validateTokenType(new Token[]{ Token.SYMBOL });
        validateSymbol(new char[] { '-', '~' });
        lex.advance();
    }

    private boolean isUnaryOp() {
        return lex.getTokenType() == Token.SYMBOL &&
                (lex.getSymbol() == '-' || lex.getSymbol() == '~');
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
    private String parseType() {
        validateTokenType(new Token[]{ Token.KEYWORD, Token.IDENTIFIER });

        if(lex.getTokenType() == Token.KEYWORD) validateKeyWord(new Keyword[]{Keyword.INT, Keyword.CHAR, Keyword.BOOLEAN});
        String type=  lex.getTokenType() == Token.IDENTIFIER ? lex.getIdentifier() : lex.getKeyword().toString();
        lex.advance();
        return type;
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
