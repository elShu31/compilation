package ir;

import mips.MipsGenerator;

public class IrCommandEpilogue extends IrCommand {
    public String funcName;

    public IrCommandEpilogue(String funcName) {
        this.funcName = funcName;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().epilogue();
    }
}
