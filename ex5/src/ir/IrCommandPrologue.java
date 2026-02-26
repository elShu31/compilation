package ir;

import mips.MipsGenerator;

public class IrCommandPrologue extends IrCommand {
    public String funcName;
    public int numLocals;

    public IrCommandPrologue(String funcName, int numLocals) {
        this.funcName = funcName;
        this.numLocals = numLocals;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().prologue(funcName, numLocals);
    }
}
