/***********/
/* PACKAGE */
/***********/
package dataflow;

import java.util.*;
import cfg.*;
import ir.*;
import temp.Temp;

/**
 * Backward dataflow analysis for Liveness.
 * Computes IN and OUT sets for each IR command.
 * 
 * IN[n] = USE[n] U (OUT[n] - DEF[n])
 * OUT[n] = U_{s in succ[n]} IN[s]
 */
public class LivenessAnalysis {
    private CFG cfg;
    private List<LivenessState> inStates;
    private List<LivenessState> outStates;

    public LivenessAnalysis(CFG cfg) {
        this.cfg = cfg;
        this.inStates = new ArrayList<>();
        this.outStates = new ArrayList<>();
    }

    protected LivenessState createInitialState() {
        return new LivenessState();
    }

    protected void transfer(IrCommand cmd, LivenessState outState, LivenessState inState) {
        // IN[n] = USE[n] U (OUT[n] - DEF[n])
        Set<Temp> newInTemps = new HashSet<>(outState.liveTemps);

        // OUT[n] - DEF[n]
        for (Temp defTemp : cmd.getDef()) {
            newInTemps.remove(defTemp);
        }

        // U USE[n]
        newInTemps.addAll(cmd.getUse());

        inState.liveTemps = newInTemps;
    }

    public void analyze() {
        int n = cfg.size();
        if (n == 0)
            return;

        for (int i = 0; i < n; i++) {
            inStates.add(createInitialState());
            outStates.add(createInitialState());
        }

        Queue<Integer> worklist = new LinkedList<>();
        Set<Integer> inWorklist = new HashSet<>();

        // For backwards analysis, start from the end
        for (int i = n - 1; i >= 0; i--) {
            worklist.add(i);
            inWorklist.add(i);
        }

        while (!worklist.isEmpty()) {
            int nodeIdx = worklist.poll();
            inWorklist.remove(nodeIdx);

            // Compute OUT[n] = union of IN[s] for all successors s
            Set<Integer> succs = cfg.getSuccessors(nodeIdx);
            LivenessState newOut = createInitialState();
            for (int succIdx : succs) {
                newOut.join(inStates.get(succIdx));
            }
            outStates.set(nodeIdx, newOut);

            // Compute new IN[n] = transfer(n, OUT[n])
            LivenessState newIn = createInitialState();
            transfer(cfg.getCommand(nodeIdx), newOut, newIn);

            if (!newIn.equals(inStates.get(nodeIdx))) {
                inStates.set(nodeIdx, newIn);
                for (int predIdx : cfg.getPredecessors(nodeIdx)) {
                    if (!inWorklist.contains(predIdx)) {
                        worklist.add(predIdx);
                        inWorklist.add(predIdx);
                    }
                }
            }
        }
    }

    public LivenessState getInState(int nodeIdx) {
        return inStates.get(nodeIdx);
    }

    public LivenessState getOutState(int nodeIdx) {
        return outStates.get(nodeIdx);
    }
}
