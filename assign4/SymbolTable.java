package assign4;

import java.util.HashMap;

public class SymbolTable {
    private HashMap<String, Identity> classScope;
    private HashMap<String, Identity> localScope;

    /**
     * Constructor
     */
    public SymbolTable() {
        classScope = new HashMap<String, Identity>();
        localScope = new HashMap<String, Identity>();
    }

    /**
     * Reset local scope.
     */
    public void startSubroutine() {
        localScope = new HashMap<String, Identity>();
    }

    /**
     * Defines a new variable and store it at the appropriate scope.
     * @param name The name of the variable.
     * @param type The  data type of the variable.
     * @param kind Whether the variable is static, a field, an argument, or a local variable.
     */
    public void define(String name, String type, Kind kind) {
        Identity identity = new Identity(name, type, kind);

        switch(kind) {
            case STATIC, FIELD -> classScope.put(name, identity);
            case ARG, VAR -> localScope.put(name, identity);
        }
    }

    /**
     * Returns whether variable of a given name is defined in the current scope.
     * @param name
     * @return
     */
    public boolean isDefined(String name) {
        return classScope.containsKey(name) || localScope.containsKey(name);
    }

    public Kind kindOf(String name, Scope scope) {
        switch(scope) {
            case CLASS -> { return classScope.get(name).kind; }
            default -> { return localScope.get(name).kind; }
        }
    }

    public String typeOf(String name, Scope scope) {
        switch(scope) {
            case CLASS -> { return classScope.get(name).type; }
            default -> { return localScope.get(name).type; }
        }
    }


    public enum Kind {
        STATIC, FIELD, ARG, VAR,
    }

    public enum Scope {
        CLASS, LOCAL,
    }
}
