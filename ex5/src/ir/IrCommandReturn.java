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

public class IrCommandReturn extends IrCommand {
    public String funcName;
    public Temp retVal;

    public IrCommandReturn(String funcName, Temp retVal) {
        this.funcName = funcName;
        this.retVal = retVal;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().returnFromFunc(funcName, retVal);
    }

    @Override
    public Set<Temp> getUsedTemps() {
        Set<Temp> s = new HashSet<>();
        if (retVal != null)
            s.add(retVal);
        return s;
    }
}
