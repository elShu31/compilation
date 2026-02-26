package ir;

import mips.MipsGenerator;
import temp.Temp;

import java.util.ArrayList;
import java.util.List;

/**
 * IR command to concatenate two strings.
 * dst = str1 + str2
 * 
 * Implementation: compute lengths via strlen loops,
 * allocate new buffer via sbrk, copy both strings.
 */
public class IrCommandStringConcat extends IrCommand {
    public Temp dst;
    public Temp str1;
    public Temp str2;

    public IrCommandStringConcat(Temp dst, Temp str1, Temp str2) {
        this.dst = dst;
        this.str1 = str1;
        this.str2 = str2;
    }

    @Override
    public List<Temp> getDef() {
        List<Temp> defs = new ArrayList<>();
        if (dst != null)
            defs.add(dst);
        return defs;
    }

    @Override
    public List<Temp> getUse() {
        List<Temp> uses = new ArrayList<>();
        if (str1 != null)
            uses.add(str1);
        if (str2 != null)
            uses.add(str2);
        return uses;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().stringConcat(dst, str1, str2);
    }
}
