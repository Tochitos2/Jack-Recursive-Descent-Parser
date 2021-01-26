package tokenizer;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Tokenizer/lexical analyser for the Jack language.
 * 
 * @author djb
 * @version 2020.12.04
 */
public class Tokenizer {
    private final BufferedReader input;
    // The current line being processed.
    private String currentLine;
    // The current line number.
    private int lineNumber = 0;
    private static final char COMMENT_CHAR = '/';

    // Elements of the next token.
    private Token tokenType;
    private Keyword keyword;
    private char symbol;
    private String identifier;
    private int intVal;
    private String stringVal;
    
    private boolean debug = false;
    
    /**
     * Create a Tokenizer for the given input.
     * @param reader The file to be read.
     */
    public Tokenizer(BufferedReader reader)
    {
        this.input = reader;
        currentLine = "";
        moveToNextToken();
    }
    
    /**
     * Are there any more tokens?
     * @return true if there is at least one more token.
     */
    public boolean hasMoreTokens()
    {
        return currentLine != null;
    }
    
    /**
     * Advance to the next token.
     */
    public void advance()
    {        
        assert hasMoreTokens();
        
        tokenType = null;
        keyword = null;
        symbol = 0;
        identifier = null;
        intVal = Integer.MIN_VALUE;
        stringVal = null;
        
        decodeNextToken();
        //debug();
        moveToNextToken();
    }
    
    /**
     * A debugging method.
     * Return details of the current token.
     * @return a formatted version of the current token.
     */
    public String getTokenDetails()
    {
        StringBuilder s = new StringBuilder();
        s.append(tokenType).append(' ');
        switch(tokenType) {
            case KEYWORD:
                s.append(keyword); break;
            case IDENTIFIER:
                s.append(identifier); break;
            case SYMBOL:
                s.append(symbol); break;
            case INT_CONST:
                s.append(intVal); break;
            case STRING_CONST:
                s.append(stringVal); break;
            default:
                s.append("???"); break;
                
        }
        s.append(' ');
        return s.toString();
    }
    
    /**
     * Return the current token.
     * @return the current token.
     */
    public Token getTokenType()
    {
        return tokenType;
    }
    
    /**
     * Return the current keyword.
     * @return the current keyword.
     */
    public Keyword getKeyword()
    {
        assert tokenType == Token.KEYWORD;
        return keyword;
    }
    
    /**
     * Return the current symbol.
     * @return the current symbol.
     */
    public char getSymbol()
    {
        assert tokenType == Token.SYMBOL;
        return symbol;
    }
    
    /**
     * Return the text of the current identifier.
     * @return the current identifier.
     */
    public String getIdentifier()
    {
        assert tokenType == Token.IDENTIFIER;
        return identifier;
    }
    
    /**
     * Return the integer that is the current INT_CONST
     * @return the current integer.
     */
    public int getIntval()
    {
        assert tokenType == Token.INT_CONST;
        return intVal;
    }

    /**
     * Return the string that is the current STRING_CONST
     * @return the current string.
     */
    public String getStringVal()
    {
        assert tokenType == Token.STRING_CONST;
        return stringVal;
    }
    
    /**
     * Return the number of the current line being processed.
     * @return the current line number.
     */
    public int getLineNumber()
    {
        return lineNumber;
    }
    
    /**
     * Decode the next token.
     * The current line is position at its first character.
     */
    private void decodeNextToken()
    {
        char c = currentLine.charAt(0);
        if(Character.isAlphabetic(c) || c == '_') {
            int index = 1;
            int len = currentLine.length();
            while(index < len && isIdChar(currentLine.charAt(index))) {
                index++;
            }
            String word = currentLine.substring(0, index);
            currentLine = currentLine.substring(index);
            keyword = Keyword.identify(word);
            if(keyword != null) {
                tokenType = Token.KEYWORD;
            }
            else {
                tokenType = Token.IDENTIFIER;
                identifier = word;
            }
        }
        else if(Character.isDigit(c)) {
            tokenType = Token.INT_CONST;
            int index = 1;
            int len = currentLine.length();
            while(index < len && Character.isDigit(currentLine.charAt(index))) {
                index++;
            }
            String num = currentLine.substring(0, index);
            currentLine = currentLine.substring(index);
            intVal = Integer.parseInt(num);
        }  
        else if(c == '"') {
            currentLine = currentLine.substring(1);
            tokenType = Token.STRING_CONST;
            int index = 0;
            int len = currentLine.length();
            while(index < len && currentLine.charAt(index) != '"') {
                index++;
            }
            if(index < len) {
                stringVal = currentLine.substring(0, index);
                currentLine = currentLine.substring(index + 1);
            }
            else {
                throw new IllegalStateException("Unterminated string");
            }
        }
        else {
            currentLine = currentLine.substring(1);
            switch(c) {
                case '+':
                case '-':
                case '*':
                case '/':
                case '&':
                case '|':
                case '<':
                case '>':
                case '=':
                case '~':
                case '{':
                case '}':
                case '(':
                case ')':
                case '[':
                case ']':
                case '.':
                case ',':
                case ';':
                    tokenType = Token.SYMBOL;
                    symbol = c;
                    break;
                default:
                    throw new IllegalStateException(
                            "Unrecognised character: " + c);
            }
        }
        currentLine = currentLine.trim();
    }
    
    /**
     * Is the given character belongs in an identifier?
     * @param c The character to test.
     * @return true if the character belongs in an identifier.
     */
    private boolean isIdChar(char c)
    {
        return Character.isAlphabetic(c) ||
               Character.isDigit(c) ||
               c == '_';
    }
    
    /**
     * Find the start of the next token, skipping any
     * blank lines and comments.
     */
    private void moveToNextToken()
    {
        currentLine = currentLine.trim();
        while(currentLine != null && currentLine.isEmpty()) {
            currentLine = readNonblankLine();
        }
        while(currentLine != null &&
                    currentLine.length() >= 2 && 
                    currentLine.charAt(0) == COMMENT_CHAR &&
                    (currentLine.charAt(1) == COMMENT_CHAR ||
                     currentLine.charAt(1) == '*')) {
            if(currentLine.charAt(1) == COMMENT_CHAR) {
                // Single-line comment.
                currentLine = readNonblankLine();
            }
            else if(currentLine.charAt(1) == '*') {
                // Multi-line comment.
                currentLine = skipMultiLineComment(currentLine);
                if(currentLine != null) {
                    currentLine = currentLine.trim();
                    if(currentLine.isEmpty()) {
                        currentLine = readNonblankLine();
                    }
                }
            }
        }
    }
        
    /**
     * The given line includes the start of a multi-line comment.
     * Find and return the next significant text beyond the comment.
     * @param line The start of the commen.
     * @return the next significant text beyond the comment.
     */
    private String skipMultiLineComment(String line)
    {
        // Drop the first two characters.
        line = line.substring(2);
        boolean endFound = false;
        while(!endFound && line != null) {
            int index = 0;
            int len = line.length();
            while(index < len && line.charAt(index) != '*') {
                index++;
            }
            if(index == len) {
                line = readNonblankLine();
            }
            else {
                // Possible end of comment.
                if(index + 1 < len && line.charAt(index + 1) == COMMENT_CHAR) {
                    endFound = true;
                    line = line.substring(index + 2);
                }
                else {
                    // Skip the '*'.
                    line = line.substring(index + 1);
                }
            }
        }
        return line;
    }
    
    /**
     * Read and return the next non-blank line.
     * @return The next line, or null if there are none.
     * @throws UnexpectedIOException on any input error.
     */
    private String readNonblankLine()
            throws UnexpectedIOException
    {
        try {
            String line = input.readLine();
            lineNumber++;
            debug(line);
            while(line != null && line.trim().isEmpty()) {
                line = input.readLine();
                lineNumber++;
                debug(line);
            }
            if(line != null) {
                //System.out.println(line);
                return line.trim();
            }
            else {
                return line;
            }
        } catch (IOException ex) {
            throw new UnexpectedIOException(ex.getMessage());
        }
    }
    
    /**
     * Print the given string.
     * @param s The string to be printed.
     */
    private void debug(String s)
    {
        if(debug && s != null) {
            System.out.println(s);
        }
    }
    
    /**
     * An unexpected read error occurred.
     * Handle via an unchecked exception rather than an IOException.
     */
    private static class UnexpectedIOException extends RuntimeException
    {
        public UnexpectedIOException(String message)
        {
            super(message);
        }
    }
}
