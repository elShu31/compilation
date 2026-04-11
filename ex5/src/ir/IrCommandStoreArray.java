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

public class IrCommandStoreArray extends IrCommand {
    public Temp arrayBase;
    public Temp index;
    public Temp src;

    public IrCommandStoreArray(Temp arrayBase, Temp index, Temp src) {
        this.arrayBase = arrayBase;
        this.index = index;
        this.src = src;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().storeArray(arrayBase, index, src);
    }

    @Override
    public Set<Temp> getUsedTemps() {
        Set<Temp> s = new HashSet<>();
        if (arrayBase != null)
            s.add(arrayBase);
        if (index != null)
            s.add(index);
        if (src != null)
            s.add(src);
        return s;
    }
}
