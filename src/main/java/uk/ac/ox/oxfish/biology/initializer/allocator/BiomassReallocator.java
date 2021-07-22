/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.initializer.allocator;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.stream;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.ToIntFunction;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Redistributes the biomass around according to a "schedule" that maps a simulation step to a grid
 * index. The biomass grids are normalized upon construction, but care must be taken to have the all
 * non-empty grid locations match with {@code SeaTile} that have a BiomassLocal biology, otherwise
 * biomass will be lost. The biomass grids are stored in mutable {@code DoubleGrid2D} fields, but
 * those should never be mutated, so the class is safe to share between parallel simulations. Note
 * that the {@code reallocate} method mutates the tiles biomass arrays directly.
 */
public class BiomassReallocator extends Reallocator<String> {

    /**
     * Constructs a new BiomassReallocator.
     *
     * @param allocationGrids The distribution grids used to reallocate biomass
     * @param period          The number to use as modulo for looping the schedule (normally 365)
     */
    public BiomassReallocator(
        final AllocationGrids<String> allocationGrids,
        final int period
    ) {
        super(allocationGrids, period);
    }

    @Override
    void performReallocation(
        final GlobalBiology globalBiology,
        final NauticalMap nauticalMap,
        final Map<String, ? extends DoubleGrid2D> grids
    ) {
        performReallocation(
            globalBiology,
            nauticalMap.getAllSeaTilesExcludingLandAsList(),
            getBiomassPerSpecies(globalBiology, nauticalMap),
            grids
        );
    }

    private static void performReallocation(
        final GlobalBiology globalBiology,
        final Collection<? extends SeaTile> seaTiles,
        final Map<String, Double> biomassPerSpecies,
        final Map<String, ? extends DoubleGrid2D> grids
    ) {
        final ImmutableMap<Integer, DoubleGrid2D> indexedBiomassGrids =
            makeNewBiomassGrids(
                grids,
                biomassPerSpecies,
                speciesName -> globalBiology.getSpecie(speciesName).getIndex()
            );
        seaTiles
            .stream()
            .filter(seaTile -> seaTile.getBiology() instanceof VariableBiomassBasedBiology)
            .forEach(seaTile -> {
                final double[] biomass =
                    ((VariableBiomassBasedBiology) seaTile.getBiology()).getCurrentBiomass();
                indexedBiomassGrids.forEach(
                    (i, grid) -> biomass[i] = grid.get(seaTile.getGridX(), seaTile.getGridY()));
            });
    }

    private static ImmutableMap<Integer, DoubleGrid2D> makeNewBiomassGrids(
        final Map<String, ? extends DoubleGrid2D> biomassDistributionGridPerSpeciesName,
        final Map<String, Double> totalBiomassPerSpeciesName,
        final ToIntFunction<? super String> getSpeciesIndex
    ) {
        return biomassDistributionGridPerSpeciesName
            .entrySet()
            .stream()
            .flatMap(entry -> {
                final String speciesName = entry.getKey();
                final DoubleGrid2D grid2D = entry.getValue();
                //noinspection UnstableApiUsage
                return stream(Optional
                    .ofNullable(totalBiomassPerSpeciesName.get(speciesName))
                    .map(biomass -> entry(
                        getSpeciesIndex.applyAsInt(speciesName),
                        new DoubleGrid2D(grid2D).multiply(biomass)
                    )));
            })
            .collect(toImmutableMap(Entry::getKey, Entry::getValue));
    }

    static ImmutableMap<String, Double> getBiomassPerSpecies(
        final GlobalBiology globalBiology,
        final NauticalMap nauticalMap
    ) {
        return globalBiology.getSpecies().stream().collect(toImmutableMap(
            Species::getName,
            nauticalMap::getTotalBiomass
        ));
    }

    void reallocate(
        final FishState fishState,
        final GlobalBiology globalBiology,
        final NauticalMap nauticalMap,
        final Map<String, Double> biomassToReallocate
    ) {
        getAllocationGrids()
            .atOrBeforeStep(fishState.getStep() % getPeriod())
            .ifPresent(grids -> performReallocation(
                globalBiology,
                nauticalMap.getAllSeaTilesExcludingLandAsList(),
                biomassToReallocate,
                grids
            ));
    }

}