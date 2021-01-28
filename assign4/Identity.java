package assign4;

public class Identity {
    public String name;
    public String type;
    public SymbolTable.Kind kind;

    public Identity(String name, String type, SymbolTable.Kind kind) {
        this.name = name;
        this.type = type;
        this.kind = kind;
    }
}
