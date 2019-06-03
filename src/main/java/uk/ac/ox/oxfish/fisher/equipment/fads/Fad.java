package uk.ac.ox.oxfish.fisher.equipment.fads;

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.aggregation.FishAggregation;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

import static java.lang.Math.min;

public class Fad implements FishAggregation {

    private final FadManager owner;
    private final BiomassLocalBiology aggregatedBiology;
    final private double proportionFished;

    public Fad(
        FadManager owner,
        BiomassLocalBiology aggregatedBiology,
        double proportionFished
    ) {
        this.owner = owner;
        this.aggregatedBiology = aggregatedBiology;
        this.proportionFished = proportionFished;
    }

    @Override
    public BiomassLocalBiology getAggregatedBiology() { return aggregatedBiology; }

    /* For now, just aggregate fish in fixed proportion of the underlying biomass.
       We'll probably need different types of FADs in the future when we start
       complexifying the model.
    */
    @Override
    public void aggregateFish(VariableBiomassBasedBiology seaTileBiology, GlobalBiology globalBiology) {
        if (proportionFished > 0) {
            // Calculate the catches and add them to the FAD biology:
            double[] catches = new double[globalBiology.getSize()];
            for (Species species : globalBiology.getSpecies()) {
                double currentBiomass = aggregatedBiology.getBiomass(species);
                double maxCatch = aggregatedBiology.getCarryingCapacity(species) - currentBiomass;
                double caught = min(seaTileBiology.getBiomass(species) * proportionFished, maxCatch);
                aggregatedBiology.setCurrentBiomass(species, currentBiomass + caught);
                catches[species.getIndex()] = caught;
            }
            // Remove the catches from the underlying biology:
            final Catch catchObject = new Catch(catches);
            seaTileBiology.reactToThisAmountOfBiomassBeingFished(catchObject, catchObject, globalBiology);
        }
    }

    public FadManager getOwner() { return owner; }

    public void releaseFish(VariableBiomassBasedBiology seaTileBiology, GlobalBiology globalBiology) {
        for (Species species : globalBiology.getSpecies()) {
            // Remove biomass from the FAD...
            final Double fadBiomass = aggregatedBiology.getBiomass(species);
            aggregatedBiology.setCurrentBiomass(species, 0);

            // ...and send that biomass down to the sea tile's biology.
            // In the unlikely event that the sea tile's carrying capacity is exceeded,
            // the extra fish is lost.
            final Double seaTileBiomass = seaTileBiology.getBiomass(species);
            final Double seaTileCarryingCapacity = seaTileBiology.getCarryingCapacity(species);
            final double newSeaTileBiomass = min(seaTileBiomass + fadBiomass, seaTileCarryingCapacity);
            seaTileBiology.setCurrentBiomass(species, newSeaTileBiomass);
        }
    }
}
