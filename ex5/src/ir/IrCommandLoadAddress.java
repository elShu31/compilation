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

public class IrCommandLoadAddress extends IrCommand {
    public Temp dst;
    public String label;

    public IrCommandLoadAddress(Temp dst, String label) {
        this.dst = dst;
        this.label = label;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().la(dst, label);
    }

    @Override
    public Set<Temp> getDefinedTemps() {
        Set<Temp> s = new HashSet<>();
        if (dst != null)
            s.add(dst);
        return s;
    }
}
