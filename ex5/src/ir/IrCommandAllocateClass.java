/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.util.Set;
import java.util.HashSet;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import mips.*;
import temp.*;

public class IrCommandAllocateClass extends IrCommand {
    public Temp dst;
    public int size;
    public String className;

    public IrCommandAllocateClass(Temp dst, int size, String className) {
        this.dst = dst;
        this.size = size;
        this.className = className;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().allocateClass(dst, size, className);
    }

    @Override
    public Set<Temp> getUsedTemps() {
        // Size is a constant int here, so there is no temp used for it
        return new HashSet<>();
    }

    @Override
    public Set<Temp> getDefinedTemps() {
        Set<Temp> s = new HashSet<>();
        if (dst != null)
            s.add(dst);
        return s;
    }
}
