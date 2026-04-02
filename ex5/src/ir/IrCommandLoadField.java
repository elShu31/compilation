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

public class IrCommandLoadField extends IrCommand {
    public Temp dst;
    public Temp objectBase;
    public int offset;

    public IrCommandLoadField(Temp dst, Temp objectBase, int offset) {
        this.dst = dst;
        this.objectBase = objectBase;
        this.offset = offset;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().loadField(dst, objectBase, offset);
    }

    @Override
    public Set<Temp> getUsedTemps() {
        Set<Temp> s = new HashSet<>();
        if (objectBase != null)
            s.add(objectBase);
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
