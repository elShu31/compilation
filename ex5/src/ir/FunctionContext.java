package ir;

/**
 * Tracks the current function context during IR generation.
 * Maintains parameter and local variable offset counters
 * for $fp-relative addressing.
 */
public class FunctionContext {
    private static FunctionContext current = null;

    private String funcName;
    private int numParams;
    private int localCount; // number of locals allocated so far
    private java.util.Map<String, Integer> paramOffsets; // param name -> fp offset
    private java.util.Map<String, Integer> localOffsets; // local name -> fp offset
    private boolean isGlobalScope;

    public FunctionContext(String funcName, int numParams) {
        this.funcName = funcName;
        this.numParams = numParams;
        this.localCount = 0;
        this.paramOffsets = new java.util.LinkedHashMap<>();
        this.localOffsets = new java.util.LinkedHashMap<>();
        this.isGlobalScope = false;
    }

    /****************************************/
    /* Static methods for current context */
    /****************************************/
    public static void enterFunction(String funcName, int numParams) {
        current = new FunctionContext(funcName, numParams);
    }

    public static void exitFunction() {
        current = null;
    }

    public static FunctionContext getCurrent() {
        return current;
    }

    public static boolean isInFunction() {
        return current != null;
    }

    /****************************************/
    /* Register a parameter with its index */
    /* Params are at +8, +12, +16, etc. */
    /* from $fp (after saved $ra and $fp) */
    /****************************************/
    public void addParam(String name, int paramIndex) {
        // First param (index 0) is at +8($fp), second at +12($fp), etc.
        int offset = 8 + paramIndex * 4;
        paramOffsets.put(name, offset);
    }

    /****************************************/
    /* Register a local variable */
    /* Locals are at -4, -8, -12, etc. */
    /* from $fp */
    /****************************************/
    public int addLocal(String name) {
        localCount++;
        int offset = -localCount * 4;
        localOffsets.put(name, offset);
        return offset;
    }

    /****************************************/
    /* Look up a variable's fp-offset */
    /****************************************/
    public Integer getParamOffset(String name) {
        return paramOffsets.get(name);
    }

    public Integer getLocalOffset(String name) {
        return localOffsets.get(name);
    }

    /****************************************/
    /* Get the variable kind and offset */
    /****************************************/
    public VarId.Kind getKind(String name) {
        if (paramOffsets.containsKey(name))
            return VarId.Kind.PARAM;
        if (localOffsets.containsKey(name))
            return VarId.Kind.LOCAL;
        return VarId.Kind.GLOBAL;
    }

    public int getFpOffset(String name) {
        if (paramOffsets.containsKey(name))
            return paramOffsets.get(name);
        if (localOffsets.containsKey(name))
            return localOffsets.get(name);
        return 0; // global — offset not used
    }

    public String getFuncName() {
        return funcName;
    }

    public int getNumParams() {
        return numParams;
    }

    public int getLocalCount() {
        return localCount;
    }
}
