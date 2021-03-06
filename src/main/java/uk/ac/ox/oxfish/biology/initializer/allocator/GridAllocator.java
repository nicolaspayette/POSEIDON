package uk.ac.ox.oxfish.biology.initializer.allocator;

import ec.util.MersenneTwisterFast;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

class GridAllocator implements BiomassAllocator {

    private final DoubleGrid2D grid;

    GridAllocator(DoubleGrid2D grid) {this.grid = grid;}

    @Override
    public double allocate(SeaTile seaTile, NauticalMap map, MersenneTwisterFast random) {
        return grid.get(seaTile.getGridX(), seaTile.getGridY());
    }
}
