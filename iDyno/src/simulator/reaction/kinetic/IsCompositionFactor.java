package simulator.reaction.kinetic;

import simulator.SpatialGrid;

/**
 * Created by chris on 6/25/2015.
 */
public interface IsCompositionFactor {



    void compose(boolean noAccumulation, SpatialGrid output, SpatialGrid input, int i, int j, int k);

}
