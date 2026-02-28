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

public class IrCommandAllocateArray extends IrCommand {
    public Temp dst;
    public Temp size;

    public IrCommandAllocateArray(Temp dst, Temp size) {
        this.dst = dst;
        this.size = size;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().allocateArray(dst, size);
    }

    @Override
    public Set<Temp> getUsedTemps() {
        Set<Temp> s = new HashSet<>();
        if (size != null)
            s.add(size);
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
