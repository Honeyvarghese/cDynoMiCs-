package iDynoOptimizer.Search;

import java.util.Comparator;

/**
 * Created by chris on 9/21/2015.
 */
public class SimulationRunComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {


        SimulationRun sr1 = (SimulationRun)o1;
        SimulationRun sr2 = (SimulationRun)o2;

        if(sr1.getWrinklingIndex() > sr2.getWrinklingIndex()) return -1;
        else if (sr1.getWrinklingIndex() < sr2.getWrinklingIndex()) return 1;
        else return 0;

    }
}
