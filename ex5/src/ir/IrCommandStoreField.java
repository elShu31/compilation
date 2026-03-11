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

    @Override
    public Set<Temp> getDefinedTemps() {
        // Storing to memory does not define a new temp
        return new HashSet<>();
    }
}
