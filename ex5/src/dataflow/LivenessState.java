/***********/
/* PACKAGE */
/***********/
package dataflow;

import java.util.HashSet;
import java.util.Set;
import temp.Temp;

/**
 * DataflowState concrete implementation for Liveness Analysis.
 * Wraps a set of Temp variables that are currently live.
 */
public class LivenessState implements DataflowState<LivenessState> {
    public Set<Temp> liveTemps;

    public LivenessState() {
        this.liveTemps = new HashSet<>();
    }

    public LivenessState(Set<Temp> liveTemps) {
        this.liveTemps = new HashSet<>(liveTemps);
    }

    @Override
    public void join(LivenessState other) {
        // Join for liveness is Union
        this.liveTemps.addAll(other.liveTemps);
    }

    @Override
    public LivenessState copy() {
        return new LivenessState(this.liveTemps);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        LivenessState that = (LivenessState) obj;
        return liveTemps.equals(that.liveTemps);
    }

    @Override
    public int hashCode() {
        return liveTemps.hashCode();
    }
}
