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

public class IrCommandPushArg extends IrCommand {
    public Temp arg;

    public IrCommandPushArg(Temp arg) {
        this.arg = arg;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().pushArg(arg);
    }

    @Override
    public Set<Temp> getUsedTemps() {
        Set<Temp> s = new HashSet<>();
        if (arg != null)
            s.add(arg);
        return s;
    }
}
