/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.initializer.factory;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import uk.ac.ox.oxfish.biology.boxcars.BoxCarSimulator;
import uk.ac.ox.oxfish.biology.boxcars.EquallySpacedBertalanffyFactory;
import uk.ac.ox.oxfish.biology.boxcars.FixedBoxcarAging;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.biology.complicated.factory.*;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantAllocatorFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;


/**
 * a factory for box car that keeps track of most parameters in one place and then feeds them to independent natural process factories.
 * It uses a quick simulation to initialize abundance at virgin levels
 */
public class SingleSpeciesBoxcarFactory implements AlgorithmFactory<SingleSpeciesAbundanceInitializer> {

    private String speciesName = "Red Fish";

    private BoxCarSimulator  abundanceSimulator;

    /**
     * initial abundance is what % of K? We just multiply this by the abundance generated by the BoxCarSimulator
     */
    private DoubleParameter initialBtOverK = new FixedDoubleParameter(1d);


    //from FixedBoxcarBertalannfyAging

    private DoubleParameter LInfinity= new FixedDoubleParameter(113);

    private DoubleParameter K = new FixedDoubleParameter(.364);

    //meristics

    /**
     * the allometric alpha converting length length cm to weight grams
     */
    private DoubleParameter allometricAlpha =
            new FixedDoubleParameter(0.015);

    /**
     * the allometric beta converting length length cm to weight grams
     */
    private DoubleParameter allometricBeta =
            new FixedDoubleParameter(2.961);

    private double cmPerBin = 5;

    private int numberOfBins = 25;

    //mortality

    private DoubleParameter yearlyMortality = new FixedDoubleParameter(.1);


    //recruitment

    private DoubleParameter virginRecruits = new FixedDoubleParameter(40741397);

    private DoubleParameter steepness = new FixedDoubleParameter(0.6);

    //this is SSB / R0; I think it's more understandable to put SSB in but on the other hand it's easier to
    //make sure SSB and R0 do not go completely out of synch
    private DoubleParameter cumulativePhi = new FixedDoubleParameter(14.2444066771724);

    private DoubleParameter lengthAtMaturity = new FixedDoubleParameter(50);



    private AlgorithmFactory<? extends BiomassAllocator> initialAbundanceAllocator = new ConstantAllocatorFactory();


    private AlgorithmFactory<? extends AbundanceDiffuser> diffuser = new NoDiffuserFactory();

    private AlgorithmFactory<? extends BiomassAllocator> recruitAllocator = new ConstantAllocatorFactory();

    private AlgorithmFactory<? extends BiomassAllocator> habitabilityAllocator = new ConstantAllocatorFactory();


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SingleSpeciesAbundanceInitializer apply(FishState state) {

        //aging
        FixedBoxcarAging aging = new FixedBoxcarAging(
                K.apply(state.getRandom()),
                LInfinity.apply(state.getRandom())
        );
        //meristic
        EquallySpacedBertalanffyFactory  meristic = new EquallySpacedBertalanffyFactory();
        meristic.setCmPerBin(cmPerBin);
        meristic.setNumberOfBins(numberOfBins);
        meristic.setAllometricAlpha(allometricAlpha);
        meristic.setAllometricBeta(allometricBeta);
        meristic.setRecruitLengthInCm(new FixedDoubleParameter(0));
        meristic.setMaxLengthInCm(getLInfinity());
        meristic.setkYearlyParameter(getK());
        //mortality
        ExponentialMortalityProcess mortality = new ExponentialMortalityProcess(
                getYearlyMortality().apply(state.getRandom()));
        //recruitment
        RecruitmentBySpawningJackKnifeMaturity recruitment = new RecruitmentBySpawningJackKnifeMaturity();
        recruitment.setLengthAtMaturity(lengthAtMaturity.apply(state.getRandom()));
        recruitment.setCumulativePhi(cumulativePhi);
        recruitment.setSteepness(steepness);
        Double virginRecruits = this.virginRecruits.apply(state.getRandom());
        recruitment.setVirginRecruits(new FixedDoubleParameter(virginRecruits));

        RecruitmentBySpawningBiomass recruitmentInstance = recruitment.apply(state);
        GrowthBinByList meristicsInstance = meristic.apply(state);
        BoxCarSimulator simulator = new BoxCarSimulator(
                virginRecruits,
                aging,
                recruitmentInstance,
                meristicsInstance,
                mortality);
        StructuredAbundance structuredAbundance = simulator.virginCondition(state, 100);
        Preconditions.checkState(structuredAbundance.getSubdivisions()==1, "invalid boxcar abundance structure!");
        double scaling = initialBtOverK.apply(state.getRandom());

        double[] abundances = structuredAbundance.asMatrix()[0];

        final double carryingCapacity = FishStateUtilities.weigh(structuredAbundance,meristicsInstance);
        System.out.println("================================= ");
        System.out.println("carrying capacity " + carryingCapacity);

        InitialAbundanceFromListFactory abundance = new InitialAbundanceFromListFactory();
        abundance.setFishPerBinPerSex(Doubles.asList(abundances));

        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                model.getYearlyDataSet().registerGatherer("Bt/K " + speciesName,
                        new Gatherer<FishState>() {
                            @Override
                            public Double apply(FishState state) {
                                return state.getTotalBiomass(state.getBiology().getSpecie(speciesName))/carryingCapacity;
                            }
                        },Double.NaN);
            }

            @Override
            public void turnOff() {

            }
        });

        return new SingleSpeciesAbundanceInitializer(
                speciesName,
                abundance.apply(state),
                initialAbundanceAllocator.apply(state),
                aging,
                meristicsInstance,
                scaling,
                recruitmentInstance,
                diffuser.apply(state),
                recruitAllocator.apply(state),
                habitabilityAllocator.apply(state),
                mortality,
                true,
                false

        );


    }

    /**
     * Getter for property 'speciesName'.
     *
     * @return Value for property 'speciesName'.
     */
    public String getSpeciesName() {
        return speciesName;
    }

    /**
     * Setter for property 'speciesName'.
     *
     * @param speciesName Value to set for property 'speciesName'.
     */
    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }
    /**
     * Getter for property 'initialBtOverK'.
     *
     * @return Value for property 'initialBtOverK'.
     */
    public DoubleParameter getInitialBtOverK() {
        return initialBtOverK;
    }

    /**
     * Setter for property 'initialBtOverK'.
     *
     * @param initialBtOverK Value to set for property 'initialBtOverK'.
     */
    public void setInitialBtOverK(DoubleParameter initialBtOverK) {
        this.initialBtOverK = initialBtOverK;
    }

    /**
     * Getter for property 'LInfinity'.
     *
     * @return Value for property 'LInfinity'.
     */
    public DoubleParameter getLInfinity() {
        return LInfinity;
    }

    /**
     * Setter for property 'LInfinity'.
     *
     * @param LInfinity Value to set for property 'LInfinity'.
     */
    public void setLInfinity(DoubleParameter LInfinity) {
        this.LInfinity = LInfinity;
    }

    /**
     * Getter for property 'k'.
     *
     * @return Value for property 'k'.
     */
    public DoubleParameter getK() {
        return K;
    }

    /**
     * Setter for property 'k'.
     *
     * @param k Value to set for property 'k'.
     */
    public void setK(DoubleParameter k) {
        K = k;
    }

    /**
     * Getter for property 'allometricAlpha'.
     *
     * @return Value for property 'allometricAlpha'.
     */
    public DoubleParameter getAllometricAlpha() {
        return allometricAlpha;
    }

    /**
     * Setter for property 'allometricAlpha'.
     *
     * @param allometricAlpha Value to set for property 'allometricAlpha'.
     */
    public void setAllometricAlpha(DoubleParameter allometricAlpha) {
        this.allometricAlpha = allometricAlpha;
    }

    /**
     * Getter for property 'allometricBeta'.
     *
     * @return Value for property 'allometricBeta'.
     */
    public DoubleParameter getAllometricBeta() {
        return allometricBeta;
    }

    /**
     * Setter for property 'allometricBeta'.
     *
     * @param allometricBeta Value to set for property 'allometricBeta'.
     */
    public void setAllometricBeta(DoubleParameter allometricBeta) {
        this.allometricBeta = allometricBeta;
    }

    /**
     * Getter for property 'cmPerBin'.
     *
     * @return Value for property 'cmPerBin'.
     */
    public double getCmPerBin() {
        return cmPerBin;
    }

    /**
     * Setter for property 'cmPerBin'.
     *
     * @param cmPerBin Value to set for property 'cmPerBin'.
     */
    public void setCmPerBin(double cmPerBin) {
        this.cmPerBin = cmPerBin;
    }

    /**
     * Getter for property 'numberOfBins'.
     *
     * @return Value for property 'numberOfBins'.
     */
    public int getNumberOfBins() {
        return numberOfBins;
    }

    /**
     * Setter for property 'numberOfBins'.
     *
     * @param numberOfBins Value to set for property 'numberOfBins'.
     */
    public void setNumberOfBins(int numberOfBins) {
        this.numberOfBins = numberOfBins;
    }

    /**
     * Getter for property 'yearlyMortality'.
     *
     * @return Value for property 'yearlyMortality'.
     */
    public DoubleParameter getYearlyMortality() {
        return yearlyMortality;
    }

    /**
     * Setter for property 'yearlyMortality'.
     *
     * @param yearlyMortality Value to set for property 'yearlyMortality'.
     */
    public void setYearlyMortality(DoubleParameter yearlyMortality) {
        this.yearlyMortality = yearlyMortality;
    }

    /**
     * Getter for property 'virginRecruits'.
     *
     * @return Value for property 'virginRecruits'.
     */
    public DoubleParameter getVirginRecruits() {
        return virginRecruits;
    }

    /**
     * Setter for property 'virginRecruits'.
     *
     * @param virginRecruits Value to set for property 'virginRecruits'.
     */
    public void setVirginRecruits(DoubleParameter virginRecruits) {
        this.virginRecruits = virginRecruits;
    }

    /**
     * Getter for property 'steepness'.
     *
     * @return Value for property 'steepness'.
     */
    public DoubleParameter getSteepness() {
        return steepness;
    }

    /**
     * Setter for property 'steepness'.
     *
     * @param steepness Value to set for property 'steepness'.
     */
    public void setSteepness(DoubleParameter steepness) {
        this.steepness = steepness;
    }

    /**
     * Getter for property 'cumulativePhi'.
     *
     * @return Value for property 'cumulativePhi'.
     */
    public DoubleParameter getCumulativePhi() {
        return cumulativePhi;
    }

    /**
     * Setter for property 'cumulativePhi'.
     *
     * @param cumulativePhi Value to set for property 'cumulativePhi'.
     */
    public void setCumulativePhi(DoubleParameter cumulativePhi) {
        this.cumulativePhi = cumulativePhi;
    }

    /**
     * Getter for property 'lengthAtMaturity'.
     *
     * @return Value for property 'lengthAtMaturity'.
     */
    public DoubleParameter getLengthAtMaturity() {
        return lengthAtMaturity;
    }

    /**
     * Setter for property 'lengthAtMaturity'.
     *
     * @param lengthAtMaturity Value to set for property 'lengthAtMaturity'.
     */
    public void setLengthAtMaturity(DoubleParameter lengthAtMaturity) {
        this.lengthAtMaturity = lengthAtMaturity;
    }

    /**
     * Getter for property 'initialAbundanceAllocator'.
     *
     * @return Value for property 'initialAbundanceAllocator'.
     */
    public AlgorithmFactory<? extends BiomassAllocator> getInitialAbundanceAllocator() {
        return initialAbundanceAllocator;
    }

    /**
     * Setter for property 'initialAbundanceAllocator'.
     *
     * @param initialAbundanceAllocator Value to set for property 'initialAbundanceAllocator'.
     */
    public void setInitialAbundanceAllocator(
            AlgorithmFactory<? extends BiomassAllocator> initialAbundanceAllocator) {
        this.initialAbundanceAllocator = initialAbundanceAllocator;
    }

    /**
     * Getter for property 'diffuser'.
     *
     * @return Value for property 'diffuser'.
     */
    public AlgorithmFactory<? extends AbundanceDiffuser> getDiffuser() {
        return diffuser;
    }

    /**
     * Setter for property 'diffuser'.
     *
     * @param diffuser Value to set for property 'diffuser'.
     */
    public void setDiffuser(
            AlgorithmFactory<? extends AbundanceDiffuser> diffuser) {
        this.diffuser = diffuser;
    }

    /**
     * Getter for property 'recruitAllocator'.
     *
     * @return Value for property 'recruitAllocator'.
     */
    public AlgorithmFactory<? extends BiomassAllocator> getRecruitAllocator() {
        return recruitAllocator;
    }

    /**
     * Setter for property 'recruitAllocator'.
     *
     * @param recruitAllocator Value to set for property 'recruitAllocator'.
     */
    public void setRecruitAllocator(
            AlgorithmFactory<? extends BiomassAllocator> recruitAllocator) {
        this.recruitAllocator = recruitAllocator;
    }

    /**
     * Getter for property 'habitabilityAllocator'.
     *
     * @return Value for property 'habitabilityAllocator'.
     */
    public AlgorithmFactory<? extends BiomassAllocator> getHabitabilityAllocator() {
        return habitabilityAllocator;
    }

    /**
     * Setter for property 'habitabilityAllocator'.
     *
     * @param habitabilityAllocator Value to set for property 'habitabilityAllocator'.
     */
    public void setHabitabilityAllocator(
            AlgorithmFactory<? extends BiomassAllocator> habitabilityAllocator) {
        this.habitabilityAllocator = habitabilityAllocator;
    }


    public BoxCarSimulator getAbundanceSimulator() {
        return abundanceSimulator;
    }

    public void setAbundanceSimulator(BoxCarSimulator abundanceSimulator) {
        this.abundanceSimulator = abundanceSimulator;
    }
}
