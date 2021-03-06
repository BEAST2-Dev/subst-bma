package beast.evolution.tree.coalescent;

import beast.core.Description;
import beast.core.Input;
import beast.evolution.tree.Node;
import beast.evolution.tree.Scaler;
import beast.evolution.tree.Tree;
import beast.util.HeapSort;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chieh-Hsi Wu
 */
@Description("Use this class the scale the tree intervals to the correct units for coalescent likelihood.")
public class ScaledTreeIntervals extends TreeIntervals {



    @Override
    public void initAndValidate(){
        scalerChanged = true;
        treeChanged = true;
        scaler = scalerInput.get();
        intervalsKnown = false;
        calculateIntervals();


    }

    public Input<Scaler> scalerInput = new Input<Scaler>(
            "scaler",
            "A component that scales the tree intervals",
            Input.Validate.REQUIRED
    );

    private double[] rawIntervals;
    private double[] storedRawIntervals;

    private Scaler scaler;

    private boolean scalerChanged;
    private boolean treeChanged;



    /**
     * CalculationNode methods *
     */
    @Override
    protected boolean requiresRecalculation() {
        if(treeInput.get().somethingIsDirty()){
            treeChanged = true;

        }
        if(scaler.isDirtyCalculation()){
            scalerChanged = true;
        }
        intervalsKnown = false;
        return true;
    }

    protected void calculateIntervals(){
        if(treeChanged){
            calculateRawIntervals();
        }
        intervals = new double[rawIntervals.length];
        double scalerFactor = scaler.getScaleFactor();

        for(int i = 0;i < intervals.length;i++){
            intervals[i] = rawIntervals[i]*scalerFactor;

        }
        intervalsKnown = true;

    }

    protected void calculateRawIntervals(){
        Tree tree = treeInput.get();

        final int nodeCount = tree.getNodeCount();

        double[] times = new double[nodeCount];
        int[] childCounts = new int[nodeCount];

        collectTimes(tree, times, childCounts);

        int[] indices = new int[nodeCount];

        HeapSort.sort(times, indices);

        if (rawIntervals == null || rawIntervals.length != nodeCount) {
            rawIntervals = new double[nodeCount];
            lineageCounts = new int[nodeCount];
            lineagesAdded = new List[nodeCount];
            lineagesRemoved = new List[nodeCount];
//            lineages = new List[nodeCount];

            storedRawIntervals = new double[nodeCount];
            storedLineageCounts = new int[nodeCount];

        } else {
            for (List<Node> l : lineagesAdded) {
                if (l != null) {
                    l.clear();
                }
            }
            for (List<Node> l : lineagesRemoved) {
                if (l != null) {
                    l.clear();
                }
            }
        }

        // start is the time of the first tip
        double start = times[indices[0]];
        int numLines = 0;
        int nodeNo = 0;
        intervalCount = 0;
        while (nodeNo < nodeCount) {

            int lineagesRemoved = 0;
            int lineagesAdded = 0;

            double finish = times[indices[nodeNo]];
            double next;

            do {
                final int childIndex = indices[nodeNo];
                final int childCount = childCounts[childIndex];
                // dont use nodeNo from here on in do loop
                nodeNo += 1;
                if (childCount == 0) {
                    addLineage(intervalCount, tree.getNode(childIndex));
                    lineagesAdded += 1;
                } else {
                    lineagesRemoved += (childCount - 1);

                    // record removed lineages
                    final Node parent = tree.getNode(childIndex);
                    //assert childCounts[indices[nodeNo]] == beast.tree.getChildCount(parent);
                    //for (int j = 0; j < lineagesRemoved + 1; j++) {
                    for (int j = 0; j < childCount; j++) {
                        Node child = j == 0 ? parent.getLeft() : parent.getRight();
                        removeLineage(intervalCount, child);
                    }

                    // record added lineages
                    addLineage(intervalCount, parent);
                    // no mix of removed lineages when 0 th
                    if (multifurcationLimit == 0.0) {
                        break;
                    }
                }

                if (nodeNo < nodeCount) {
                    next = times[indices[nodeNo]];
                } else break;
            } while (Math.abs(next - finish) <= multifurcationLimit);

            if (lineagesAdded > 0) {

                if (intervalCount > 0 || ((finish - start) > multifurcationLimit)) {
                    rawIntervals[intervalCount] = finish - start;
                    lineageCounts[intervalCount] = numLines;
                    intervalCount += 1;
                }

                start = finish;
            }

            // add sample event
            numLines += lineagesAdded;

            if (lineagesRemoved > 0) {

                rawIntervals[intervalCount] = finish - start;
                lineageCounts[intervalCount] = numLines;
                intervalCount += 1;
                start = finish;
            }
            // coalescent event
            numLines -= lineagesRemoved;
        }


    }

    protected void store(){

        System.arraycopy(rawIntervals, 0, storedRawIntervals, 0, rawIntervals.length);
        storedIntervals = new double[intervals.length];
        super.store();
    }

}
