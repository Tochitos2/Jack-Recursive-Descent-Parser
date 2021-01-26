package tokenizer;

/**
 * Keywords of the Jack language.
 * 
 * @author djb
 * @version 2020.11.29
 */
public enum Keyword {
    CLASS, METHOD, FUNCTION, CONSTRUCTOR,
    INT, BOOLEAN, CHAR, VOID,
    VAR, STATIC, FIELD,
    LET, DO, IF, ELSE, WHILE,
    RETURN,
    TRUE, FALSE,
    NULL,
    THIS;
    
    public static Keyword identify(String s)
    {
        switch(s) {
            case "class": return CLASS;
            case "method": return METHOD;
            case "function": return FUNCTION;
            case "constructor": return CONSTRUCTOR;
            case "int": return INT;
            case "boolean": return BOOLEAN;
            case "char": return CHAR;
            case "void": return VOID;
            case "var": return VAR;
            case "static": return STATIC;
            case "field": return FIELD;
            case "let": return LET;
            case "do": return DO;
            case "if": return IF;
            case "else": return ELSE;
            case "while": return WHILE;
            case "return": return RETURN;
            case "true": return TRUE;
            case "false": return FALSE;
            case "null": return NULL;
            case "this": return THIS;
            default:
                return null;
        }
    }
}
