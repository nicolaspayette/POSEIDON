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
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntFunction;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.initializer.allocator.JuvenileMatureAllocationGridsSupplier.AgeGroup;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 *
 */
public class AbundanceReallocator extends Reallocator<Entry<String, AgeGroup>> {


    private final Map<String, ? extends IntFunction<AgeGroup>> binToAgeGroupMappings;

    /**
     * Constructs a new AbundanceReallocator.
     *
     * @param allocationGrids       The distribution grids used to reallocate biomass
     * @param period                The number to use as modulo for looping the schedule (usually
     *                              365)
     * @param binToAgeGroupMappings A map from species names to a function giving us the age group
     *                              (juvenile or adult) for each age bin
     */
    public AbundanceReallocator(
        final AllocationGrids<Entry<String, AgeGroup>> allocationGrids,
        final int period,
        final Map<String, ? extends IntFunction<AgeGroup>> binToAgeGroupMappings
    ) {
        super(allocationGrids, period);
        this.binToAgeGroupMappings = ImmutableMap.copyOf(binToAgeGroupMappings);
    }

    @Override
    void performReallocation(
        final GlobalBiology globalBiology,
        final NauticalMap nauticalMap,
        final Map<Entry<String, AgeGroup>, ? extends DoubleGrid2D> grids
    ) {
        final Map<SeaTile, AbundanceLocalBiology> seaTileBiologies =
            getSeaTileBiologies(nauticalMap);

        aggregateBiomass(globalBiology, seaTileBiologies).forEach((species, globalAbundance) ->
            range(0, globalAbundance.length).forEach(subdivision ->
                range(0, globalAbundance[subdivision].length).forEach(bin -> {
                    final DoubleGrid2D grid = grids.get(entry(
                        species.getName(),
                        binToAgeGroupMappings.get(species.getName()).apply(bin)
                    ));
                    seaTileBiologies.forEach((seaTile, biology) ->
                        biology.getAbundance(species).asMatrix()[subdivision][bin] =
                            globalAbundance[subdivision][bin]
                                * grid.get(seaTile.getGridX(), seaTile.getGridY())
                    );
                })
            )
        );
    }

    private static Map<SeaTile, AbundanceLocalBiology> getSeaTileBiologies(
        final NauticalMap nauticalMap
    ) {
        return nauticalMap.getAllSeaTilesExcludingLandAsList()
            .stream()
            .filter(seaTile -> seaTile.getBiology() instanceof AbundanceLocalBiology)
            .collect(toImmutableMap(
                identity(),
                seaTile -> ((AbundanceLocalBiology) seaTile.getBiology())
            ));
    }

    private static Map<Species, double[][]> aggregateBiomass(
        final GlobalBiology globalBiology,
        final Map<SeaTile, ? extends AbundanceLocalBiology> seaTileBiologies
    ) {

        // Create a map from species to empty abundance arrays which we
        // are going to mutate directly when summing up global abundance
        final Map<Species, double[][]> abundances = globalBiology.getSpecies()
            .stream()
            .collect(toImmutableMap(
                identity(),
                species -> new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()]
            ));

        seaTileBiologies.forEach((seaTile, biology) ->
            abundances.forEach((species, abundance) ->
                range(0, abundance.length).forEach(subdivision ->
                    Arrays.setAll(
                        abundance[subdivision],
                        bin -> biology.getAbundance(species).getAbundance(subdivision, bin)
                    )
                )
            )
        );
        return abundances;
    }

}