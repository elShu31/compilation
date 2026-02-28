package regalloc;

import cfg.CFG;
import ir.IrCommand;
import temp.Temp;

import java.util.*;

public class LivenessAnalyzer {
    private CFG cfg;
    private List<Set<Temp>> inSets;
    private List<Set<Temp>> outSets;

    public LivenessAnalyzer(CFG cfg) {
        this.cfg = cfg;
        this.inSets = new ArrayList<>();
        this.outSets = new ArrayList<>();

        int size = cfg.size();
        for (int i = 0; i < size; i++) {
            inSets.add(new HashSet<>());
            outSets.add(new HashSet<>());
        }

        analyze();
    }

    private void analyze() {
        boolean changed = true;
        int size = cfg.size();

        while (changed) {
            changed = false;

            // Iterate backwards
            for (int i = size - 1; i >= 0; i--) {
                IrCommand cmd = cfg.getCommand(i);
                Set<Temp> use = cmd.getUsedTemps();
                Set<Temp> def = cmd.getDefinedTemps();

                Set<Temp> out = new HashSet<>();
                for (int succ : cfg.getSuccessors(i)) {
                    out.addAll(inSets.get(succ));
                }

                Set<Temp> in = new HashSet<>(out);
                in.removeAll(def);
                in.addAll(use);

                if (!out.equals(outSets.get(i))) {
                    outSets.set(i, out);
                    changed = true;
                }

                if (!in.equals(inSets.get(i))) {
                    inSets.set(i, in);
                    changed = true;
                }
            }
        }
    }

    public Set<Temp> getIn(int index) {
        return inSets.get(index);
    }

    public Set<Temp> getOut(int index) {
        return outSets.get(index);
    }
}
