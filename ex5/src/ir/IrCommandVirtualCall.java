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

public class IrCommandVirtualCall extends IrCommand {
    public Temp thisPtr;
    public int vtableOffset;
    public int numArgs;
    public Temp retVal;

    public IrCommandVirtualCall(Temp thisPtr, int vtableOffset, int numArgs, Temp retVal) {
        this.thisPtr = thisPtr;
        this.vtableOffset = vtableOffset;
        this.numArgs = numArgs;
        this.retVal = retVal;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().virtualCallFunc(thisPtr, vtableOffset, numArgs, retVal);
    }

    @Override
    public Set<Temp> getUsedTemps() {
        Set<Temp> s = new HashSet<>();
        if (thisPtr != null)
            s.add(thisPtr);
        return s;
    }

    @Override
    public Set<Temp> getDefinedTemps() {
        Set<Temp> s = new HashSet<>();
        if (retVal != null)
            s.add(retVal);
        return s;
    }
}
