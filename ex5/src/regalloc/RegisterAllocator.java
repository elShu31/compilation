/***********/
/* PACKAGE */
/***********/
package regalloc;

import java.util.*;
import temp.Temp;

/**
 * Implements Chaitin's Simplification-Based Register Allocation.
 * Tries to color the interference graph with 10 colors ($t0 - $t9).
 */
public class RegisterAllocator {
    private static final int NUM_COLORS = 10;

    public static void allocate(InterferenceGraph ig) {
        Set<Temp> nodes = ig.getNodes();

        // Active graph representation we can mutate
        Map<Temp, Set<Temp>> graph = new HashMap<>();
        for (Temp t : nodes) {
            graph.put(t, new HashSet<>(ig.getNeighbors(t)));
        }

        Stack<Temp> stack = new Stack<>();

        // 1. Simplification Phase
        while (!graph.isEmpty()) {
            Temp nodeToRemove = null;

            // Find a node with degree < NUM_COLORS
            for (Map.Entry<Temp, Set<Temp>> entry : graph.entrySet()) {
                if (entry.getValue().size() < NUM_COLORS) {
                    nodeToRemove = entry.getKey();
                    break;
                }
            }

            // If no such node exists, we must spill (or fail as per exercise instructions)
            if (nodeToRemove == null) {
                System.out.println("Register Allocation Failed");
                System.exit(0);
            }

            // Remove node and its edges from graph
            Set<Temp> neighbors = graph.get(nodeToRemove);
            for (Temp neighbor : neighbors) {
                if (graph.containsKey(neighbor)) {
                    graph.get(neighbor).remove(nodeToRemove);
                }
            }
            graph.remove(nodeToRemove);

            // Push to stack
            stack.push(nodeToRemove);
        }

        // 2. Coloring Phase
        // Temp -> Color (0 to 9)
        Map<Temp, Integer> coloring = new HashMap<>();

        while (!stack.isEmpty()) {
            Temp t = stack.pop();

            // Find available colors by checking original neighbors
            boolean[] usedColors = new boolean[NUM_COLORS];
            for (Temp neighbor : ig.getNeighbors(t)) {
                if (coloring.containsKey(neighbor)) {
                    usedColors[coloring.get(neighbor)] = true;
                }
            }

            // Pick the first available color
            int chosenColor = -1;
            for (int c = 0; c < NUM_COLORS; c++) {
                if (!usedColors[c]) {
                    chosenColor = c;
                    break;
                }
            }

            if (chosenColor == -1) {
                // This shouldn't happen if simplification succeeded, but just in case
                System.out.println("Register Allocation Failed");
                System.exit(0);
            }

            coloring.put(t, chosenColor);
        }

        // 3. Mutation Phase
        // MUTATE THE TEMP:
        // We set the Temp's internal serial number to the color [0-9].
        // This is done AFTER everything is colored so we don't break HashMap hashes
        // during coloring.
        for (Map.Entry<Temp, Integer> entry : coloring.entrySet()) {
            entry.getKey().setSerialNumber(entry.getValue());
        }
    }
}
