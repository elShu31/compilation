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

public class IrCommandLoadArray extends IrCommand {
    public Temp dst;
    public Temp arrayBase;
    public Temp index;

    public IrCommandLoadArray(Temp dst, Temp arrayBase, Temp index) {
        this.dst = dst;
        this.arrayBase = arrayBase;
        this.index = index;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().loadArray(dst, arrayBase, index);
    }

    @Override
    public Set<Temp> getUsedTemps() {
        Set<Temp> s = new HashSet<>();
        if (arrayBase != null)
            s.add(arrayBase);
        if (index != null)
            s.add(index);
        return s;
    }

    @Override
    public Set<Temp> getDefinedTemps() {
        Set<Temp> s = new HashSet<>();
        if (dst != null)
            s.add(dst);
        return s;
    }
}
