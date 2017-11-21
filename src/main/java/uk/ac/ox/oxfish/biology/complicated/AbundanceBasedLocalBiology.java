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

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;
import java.util.HashMap;

/**
 * A local biology object based on abundance.
 * It is a container for the number of fish but has no biological processes coded in it.
 * It is quite unsafe as it exposes its arrays in a couple of methods but that is necessary to prevent long delays in copy-pasting
 * abundance data whenever a process takes place
 * Created by carrknight on 3/4/16.
 */
public class AbundanceBasedLocalBiology implements LocalBiology
{


    /**
     * the hashmap contains for each species a table [age][male-female] corresponding to the number of fish of that
     * age and that sex
     */
    private final HashMap<Species,double[][]>  abundance = new HashMap<>();



    /**
     * biomass gets computed somewhat lazily (but this number gets reset under any interaction with the object, no matter how trivial)
     */
    double lastComputedBiomass[];

    /**
     * creates an abundance based local biology that starts off as entirely empty
     * @param biology a list of species
     */
    public AbundanceBasedLocalBiology(GlobalBiology biology)
    {

        //for each species create cohorts
        for(Species species : biology.getSpecies()) {
            double[] male = new double[species.getNumberOfBins()];
            double[] female = new double[species.getNumberOfBins()];
            double[][] fish = new double[][]{male,female};
            abundance.put(species, fish);
        }
        //done!
        lastComputedBiomass = new double[biology.getSpecies().size()];
        Arrays.fill(lastComputedBiomass,Double.NaN);
    }


    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public Double getBiomass(Species species) {

        if(Double.isNaN(lastComputedBiomass[species.getIndex()] )) {
            lastComputedBiomass[species.getIndex()] = FishStateUtilities.weigh(
                    abundance.get(species)[FishStateUtilities.MALE],
                    abundance.get(species)[FishStateUtilities.FEMALE],
                    species.getMeristics()
            );
            assert !Double.isNaN(lastComputedBiomass[species.getIndex()] );
        }
        return lastComputedBiomass[species.getIndex()];

    }



    /**
     * ignored
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        Arrays.fill(lastComputedBiomass,Double.NaN);

    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {

    }


    private static boolean warned = false;

    /**
     * Sends a warning (since that's not usually the kind of behaviour we want) and after that
     * kills off fish starting from the oldest male until enough biomass dies.
     * @param caught fish taken from the sea
     * @param notDiscarded fish put in hold
     * @param biology biology object
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
            Catch caught, Catch notDiscarded, GlobalBiology biology)
    {
        Preconditions.checkArgument(caught.hasAbundanceInformation(), "This biology requires a gear that catches per age bins rather than biomass directly!");

        for(int index = 0; index < caught.numberOfSpecies(); index++) {
            Species species = biology.getSpecie(index);
            if(species.isImaginary()) //ignore imaginary catches
                continue;

            StructuredAbundance catches = caught.getAbundance(species);
            Preconditions.checkArgument(catches.getSubdivisions()==2, " needs male/female split");


            final double[][] abundanceHere = this.abundance.get(species);
            double[] maleCatches =catches.asMatrix()[FishStateUtilities.MALE];
            double[] femaleCatches =catches.asMatrix()[FishStateUtilities.FEMALE];
            Preconditions.checkArgument(maleCatches.length == abundanceHere[FishStateUtilities.MALE].length);
            for(int age=0; age<maleCatches.length; age++)
            {
                abundanceHere[FishStateUtilities.MALE][age]-=maleCatches[age];
                Preconditions.checkArgument(abundanceHere[FishStateUtilities.MALE][age] >=0,
                                            "There is now a negative amount of male fish left at age " + age);
                abundanceHere[FishStateUtilities.FEMALE][age]-=femaleCatches[age];
                Preconditions.checkArgument(abundanceHere[FishStateUtilities.FEMALE][age] >=0,
                                            "There is now a negative amount of female fish left at age " + age);
            }
            lastComputedBiomass[species.getIndex()]=Double.NaN;
        }


    }


    @Override
    public StructuredAbundance getAbundance(Species species) {
        Arrays.fill(lastComputedBiomass,Double.NaN); //force a recount after calling this

        return new StructuredAbundance(abundance.get(species)[FishStateUtilities.MALE],
                                       abundance.get(species)[FishStateUtilities.FEMALE]
                                       );

    }






    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("lastComputedBiomass", lastComputedBiomass)
                .toString();
    }



}
