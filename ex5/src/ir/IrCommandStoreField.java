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

public class IrCommandStoreField extends IrCommand {
    public Temp objectBase;
    public int offset;
    public Temp src;

    public IrCommandStoreField(Temp objectBase, int offset, Temp src) {
        this.objectBase = objectBase;
        this.offset = offset;
        this.src = src;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().storeField(objectBase, offset, src);
    }

    @Override
    public Set<Temp> getUsedTemps() {
        Set<Temp> s = new HashSet<>();
        if (objectBase != null)
            s.add(objectBase);
        if (src != null)
            s.add(src);
        return s;
    }
}
