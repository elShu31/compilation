/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/
import mips.*;

public class IrCommandPrologue extends IrCommand {
    public String funcName;
    public int localVarsSize;

    public IrCommandPrologue(String funcName, int localVarsSize) {
        this.funcName = funcName;
        this.localVarsSize = localVarsSize;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().prologue(funcName, localVarsSize);
    }
}
