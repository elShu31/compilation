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

public class IrCommandPushArg extends IrCommand {
    public Temp arg;

    public IrCommandPushArg(Temp arg) {
        this.arg = arg;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().pushArg(arg);
    }
}
