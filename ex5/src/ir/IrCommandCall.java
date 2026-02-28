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
import java.util.Set;
import java.util.HashSet;

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

    @Override
    public Set<Temp> getDefinedTemps() {
        Set<Temp> s = new HashSet<>();
        if (retVal != null)
            s.add(retVal);
        return s;
    }
}
