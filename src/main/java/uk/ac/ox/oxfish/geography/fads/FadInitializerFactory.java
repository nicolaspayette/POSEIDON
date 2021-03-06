package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadBiomassAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.LogisticFadBiomassAttractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.DoubleSupplier;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class FadInitializerFactory implements AlgorithmFactory<FadInitializer> {

    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);
    private Path fadCarryingCapacitiesFilePath = TunaScenario.input("fad_carrying_capacities.csv");
    private Map<String, DoubleParameter> attractionIntercepts = new HashMap<>();
    private Map<String, DoubleParameter> tileBiomassCoefficients = new HashMap<>();
    private Map<String, DoubleParameter> fadBiomassCoefficients = new HashMap<>();
    private Map<String, DoubleParameter> growthRates = new HashMap<>();

    @SuppressWarnings("unused")
    public Map<String, DoubleParameter> getAttractionIntercepts() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return attractionIntercepts;
    }

    @SuppressWarnings("unused")
    public void setAttractionIntercepts(final Map<String, DoubleParameter> attractionIntercepts) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.attractionIntercepts = attractionIntercepts;
    }

    @SuppressWarnings("unused")
    public Map<String, DoubleParameter> getTileBiomassCoefficients() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return tileBiomassCoefficients;
    }

    @SuppressWarnings("unused")
    public void setTileBiomassCoefficients(final Map<String, DoubleParameter> tileBiomassCoefficients) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.tileBiomassCoefficients = tileBiomassCoefficients;
    }

    @SuppressWarnings("unused")
    public Map<String, DoubleParameter> getFadBiomassCoefficients() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return fadBiomassCoefficients;
    }

    @SuppressWarnings("unused")
    public void setFadBiomassCoefficients(final Map<String, DoubleParameter> fadBiomassCoefficients) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.fadBiomassCoefficients = fadBiomassCoefficients;
    }

    @SuppressWarnings("unused")
    public Map<String, DoubleParameter> getGrowthRates() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return growthRates;
    }

    @SuppressWarnings("unused")
    public void setGrowthRates(final Map<String, DoubleParameter> growthRates) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.growthRates = growthRates;
    }

    @SuppressWarnings("unused")
    public Path getFadCarryingCapacitiesFilePath() {
        return fadCarryingCapacitiesFilePath;
    }

    @SuppressWarnings("unused")
    public void setFadCarryingCapacitiesFilePath(final Path fadCarryingCapacitiesFilePath) {
        this.fadCarryingCapacitiesFilePath = fadCarryingCapacitiesFilePath;
    }


    @SuppressWarnings("unused")
    public DoubleParameter getFishReleaseProbabilityInPercent() {
        return fishReleaseProbabilityInPercent;
    }

    @SuppressWarnings("unused")
    public void setFishReleaseProbabilityInPercent(final DoubleParameter fishReleaseProbabilityInPercent) {
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    @Override
    public FadInitializer apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        final SpeciesCodes speciesCodes = TunaScenario.speciesCodesSupplier.get();
        final Map<Species, DoubleSupplier> carryingCapacitySuppliers =
            parseAllRecords(fadCarryingCapacitiesFilePath)
                .stream()
                .collect(toImmutableMap(
                    record -> {
                        final String speciesCode = record.getString("species_code");
                        final String speciesName = speciesCodes.getSpeciesName(speciesCode);
                        return fishState.getBiology().getSpecie(speciesName);
                    },
                    record -> {
                        // let's not forget to convert from tonnes to kg...
                        final double carryingCapacity = 1000 * record.getDouble("carrying_capacity");
                        return () -> carryingCapacity;
                    }
                ));

        final Map<Species, FadBiomassAttractor> fadBiomassAttractors =
            carryingCapacitySuppliers
                .entrySet()
                .stream()
                .collect(toImmutableMap(
                    Entry::getKey, // species name
                    entry -> {
                        final String speciesName = entry.getKey().getName();
                        return new LogisticFadBiomassAttractor(
                            fishState.getRandom(),
                            attractionIntercepts.get(speciesName).apply(rng),
                            tileBiomassCoefficients.get(speciesName).apply(rng),
                            fadBiomassCoefficients.get(speciesName).apply(rng),
                            growthRates.get(speciesName).apply(rng),
                            entry.getValue().getAsDouble() // carrying capacity
                        );
                    }
                ));

        return new FadInitializer(
            fishState.getBiology(),
            carryingCapacitySuppliers,
            fadBiomassAttractors,
            fishReleaseProbabilityInPercent.apply(rng) / 100d,
            fishState::getStep
        );
    }
}
