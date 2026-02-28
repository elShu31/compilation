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
import temp.*;

public class IrCommandCall extends IrCommand {
    public String funcName;
    public int numArgs;
    public Temp retVal;

    public IrCommandCall(String funcName, int numArgs, Temp retVal) {
        this.funcName = funcName;
        this.numArgs = numArgs;
        this.retVal = retVal;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().callFunc(funcName, numArgs, retVal);
    }
}
