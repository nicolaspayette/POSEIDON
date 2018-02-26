/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.initializer;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * Logistic initializer but one where there is more fish out west than near the coast
 * Created by carrknight on 2/12/16.
 */
public class FromLeftToRightLogisticInitializer implements BiologyInitializer{


    private DiffusingLogisticInitializer delegate;

    /**
     * the leftmost seatile will have 100% capacity, the rightmost seatile will have minCapacityRatio%
     */
    private double minCapacityRatio;

    private final double exponent;

    public FromLeftToRightLogisticInitializer(
            DiffusingLogisticInitializer delegate,
            double minCapacityRatio, double exponent) {
        this.delegate = delegate;
        this.minCapacityRatio = minCapacityRatio;
        this.exponent = exponent;
        Preconditions.checkArgument(minCapacityRatio>=0);
        Preconditions.checkArgument(minCapacityRatio<=1);
    }

    /**
     * Call the independent logistic initializer but add a steppable to call to smooth fish around
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random  mersenne randomizer
     * @param model
     */
    @Override
    public void processMap(
            GlobalBiology biology, NauticalMap map,
            MersenneTwisterFast random, FishState model) {
        delegate.processMap(biology, map, random, model);
    }



    /**
     * creates the global biology object for the model
     *
     * @param random                the random number generator
     * @param modelBeingInitialized the model we are in the process of initializing
     * @return a global biology object
     */
    @Override
    public GlobalBiology generateGlobal(
            MersenneTwisterFast random, FishState modelBeingInitialized) {
        return delegate.generateGlobal(random, modelBeingInitialized);
    }

    public DoubleParameter getCarryingCapacity() {
        return delegate.getCarryingCapacity();
    }

    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     * @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     * @param map
     */
    @Override
    public LocalBiology generateLocal(
            GlobalBiology biology, SeaTile seaTile,
            MersenneTwisterFast random, int mapHeightInCells, int mapWidthInCells,
            NauticalMap map) {
        //it must be a logistic biology to come out of the diffusing logistic biology!
        LocalBiology generated = delegate.generateLocal(
                biology, seaTile, random, mapHeightInCells, mapWidthInCells,map );
        if(seaTile.getAltitude()>0)
            return generated;
        BiomassLocalBiology local = (BiomassLocalBiology) generated;
        double correctRatio = Math.max(
                Math.pow(
                        (mapWidthInCells-seaTile.getGridX())
                                /
                                (float)mapWidthInCells,
                        exponent),


                minCapacityRatio);
        assert correctRatio>=0;
        assert correctRatio<=1;
        for(Species species : biology.getSpecies()) {
            double newCarryingCapacity = local.getCarryingCapacity(species) * correctRatio;
            local.setCarryingCapacity(species, newCarryingCapacity);
            local.setCurrentBiomass(species,newCarryingCapacity);
        }
        return local;
    }



}
