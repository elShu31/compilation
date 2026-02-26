package ir;

import java.util.List;
import temp.Temp;

/**
 * IR command to declare a class vtable in the .data section.
 */
public class IrCommandVTable extends IrCommand {
    public String className;
    public List<String> methods;

    public IrCommandVTable(String className, List<String> methods) {
        this.className = className;
        this.methods = methods;
    }

    @Override
    public void mipsMe() {
        // Since we emit data section differently, we handle this in Ir.mipsMe or
        // special data pass.
        // Actually, let's just use MipsGenerator to emit it directly into the data
        // section buffer.
        mips.MipsGenerator.getInstance().emitVTable(className, methods);
    }
}
