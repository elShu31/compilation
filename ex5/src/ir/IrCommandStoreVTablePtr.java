package ir;

import java.util.ArrayList;
import java.util.List;
import temp.Temp;

/**
 * Stores the vtable pointer into the first word of a newly allocated object.
 */
public class IrCommandStoreVTablePtr extends IrCommand {
    public Temp objPtr;
    public String className;

    public IrCommandStoreVTablePtr(Temp objPtr, String className) {
        this.objPtr = objPtr;
        this.className = className;
    }

    @Override
    public List<Temp> getUse() {
        List<Temp> uses = new ArrayList<>();
        if (objPtr != null)
            uses.add(objPtr);
        return uses;
    }

    @Override
    public List<Temp> getDef() {
        return new ArrayList<>();
    }

    @Override
    public void mipsMe() {
        int ptrIdx = objPtr.getSerialNumber();
        String label = "vtable_" + className;

        // Load address of vtable
        mips.MipsGenerator.getInstance().getFileWriter().format("\tla $t0,%s\n", label);
        // Store into first word of object
        mips.MipsGenerator.getInstance().getFileWriter().format("\tsw $t0,0($t%d)\n", ptrIdx);
    }
}
