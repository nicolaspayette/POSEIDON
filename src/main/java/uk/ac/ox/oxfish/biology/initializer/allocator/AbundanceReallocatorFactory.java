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
import static uk.ac.ox.oxfish.biology.initializer.allocator.JuvenileMatureAllocationGridsSupplier.AgeGroup.JUVENILE;
import static uk.ac.ox.oxfish.biology.initializer.allocator.JuvenileMatureAllocationGridsSupplier.AgeGroup.MATURE;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntFunction;
import uk.ac.ox.oxfish.biology.initializer.allocator.JuvenileMatureAllocationGridsSupplier.AgeGroup;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.model.FishState;

public class AbundanceReallocatorFactory extends ReallocatorFactory<AbundanceReallocator> {

    private Map<String, Integer> firstAdultBinPerSpecies;

    public AbundanceReallocatorFactory() {
    }

    public AbundanceReallocatorFactory(
        final Path speciesCodesFilePath,
        final Path biomassDistributionsFilePath,
        final int period,
        final Map<String, Integer> firstMatureBinPerSpecies
    ) {
        super(speciesCodesFilePath, biomassDistributionsFilePath, period);
        this.firstAdultBinPerSpecies = ImmutableMap.copyOf(firstMatureBinPerSpecies);
    }

    @Override
    public AbundanceReallocator apply(final FishState fishState) {
        final AllocationGrids<Entry<String, AgeGroup>> grids =
            new JuvenileMatureAllocationGridsSupplier(
                getSpeciesCodesFilePath(),
                getBiomassDistributionsFilePath(),
                new MapExtent(fishState.getMap())
            ).get();
        final Map<String, IntFunction<AgeGroup>> binToAgeGroupMappings =
            firstAdultBinPerSpecies.entrySet().stream().collect(toImmutableMap(
                Entry::getKey,
                entry -> bin -> bin >= entry.getValue() ? MATURE : JUVENILE
            ));
        return new AbundanceReallocator(grids, getPeriod(), binToAgeGroupMappings);
    }
}
