package ir;

import temp.*;
import mips.*;
import java.util.Set;
import java.util.HashSet;

public class IrCommandBinopEqStrings extends IrCommand {
    public Temp t1;
    public Temp t2;
    public Temp dst;

    public IrCommandBinopEqStrings(Temp dst, Temp t1, Temp t2) {
        this.dst = dst;
        this.t1 = t1;
        this.t2 = t2;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().stringCompareEq(dst, t1, t2);
    }

    @Override
    public Set<Temp> getUsedTemps() {
        Set<Temp> s = new HashSet<>();
        if (t1 != null)
            s.add(t1);
        if (t2 != null)
            s.add(t2);
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
