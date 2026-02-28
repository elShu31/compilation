/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.util.HashSet;
import java.util.Set;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandStringConcat extends IrCommand {
    public Temp dst;
    public Temp oprnd1;
    public Temp oprnd2;

    public IrCommandStringConcat(Temp dst, Temp oprnd1, Temp oprnd2) {
        this.dst = dst;
        this.oprnd1 = oprnd1;
        this.oprnd2 = oprnd2;
    }

    @Override
    public Set<Temp> getUsedTemps() {
        Set<Temp> used = new HashSet<>();
        used.add(oprnd1);
        used.add(oprnd2);
        return used;
    }

    @Override
    public Set<Temp> getDefinedTemps() {
        Set<Temp> defined = new HashSet<>();
        defined.add(dst);
        return defined;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().stringConcat(dst, oprnd1, oprnd2);
    }
}
