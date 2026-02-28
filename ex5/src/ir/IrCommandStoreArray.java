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
}
