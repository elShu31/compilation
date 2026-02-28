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
import temp.*;
import mips.*;
import java.util.Set;
import java.util.HashSet;

public class IrCommandPrintString extends IrCommand {
    public Temp t;

    public IrCommandPrintString(Temp t) {
        this.t = t;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().printString(t);
    }

    @Override
    public Set<Temp> getUsedTemps() {
        Set<Temp> s = new HashSet<>();
        if (t != null)
            s.add(t);
        return s;
    }
}
