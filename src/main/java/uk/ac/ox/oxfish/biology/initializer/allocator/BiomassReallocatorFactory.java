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

import static uk.ac.ox.oxfish.model.scenario.TunaScenario.input;

import java.nio.file.Path;
import java.util.Objects;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class BiomassReallocatorFactory implements AlgorithmFactory<BiomassReallocator> {

    private Path speciesCodesFilePath = input("species_codes.csv");
    private Path biomassDistributionsFilePath = input("biomass_distributions.csv");
    private int period = 365;

    @Override
    public int hashCode() {
        return Objects.hash(speciesCodesFilePath, biomassDistributionsFilePath, period);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BiomassReallocatorFactory that = (BiomassReallocatorFactory) o;
        return period == that.period && Objects.equals(
            speciesCodesFilePath,
            that.speciesCodesFilePath
        ) && Objects.equals(biomassDistributionsFilePath, that.biomassDistributionsFilePath);
    }

    @SuppressWarnings("unused")
    public Path getSpeciesCodesFilePath() {
        return speciesCodesFilePath;
    }

    @SuppressWarnings("unused")
    public void setSpeciesCodesFilePath(final Path speciesCodesFilePath) {
        this.speciesCodesFilePath = speciesCodesFilePath;
    }

    @SuppressWarnings("unused")
    public Path getBiomassDistributionsFilePath() {
        return biomassDistributionsFilePath;
    }

    @SuppressWarnings("unused")
    public void setBiomassDistributionsFilePath(final Path biomassDistributionsFilePath) {
        this.biomassDistributionsFilePath = biomassDistributionsFilePath;
    }

    @Override
    public BiomassReallocator apply(final FishState fishState) {
        final AllocationGrids<String> grids =
            new AllocationGridsFactory(
                speciesCodesFilePath,
                biomassDistributionsFilePath
            ).apply(fishState);
        return new BiomassReallocator(grids, period);
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(final int period) {
        this.period = period;
    }
}
