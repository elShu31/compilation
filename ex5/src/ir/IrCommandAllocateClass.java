/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import mips.*;
import temp.*;
import java.util.Set;
import java.util.HashSet;

public class IrCommandAllocateClass extends IrCommand {
    public Temp dst;
    public int sizeBytes;
    public String className;

    public IrCommandAllocateClass(Temp dst, int sizeBytes, String className) {
        this.dst = dst;
        this.sizeBytes = sizeBytes;
        this.className = className;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().allocateClass(dst, sizeBytes, className);
    }

    @Override
    public Set<Temp> getDefinedTemps() {
        Set<Temp> s = new HashSet<>();
        if (dst != null)
            s.add(dst);
        return s;
    }
}
