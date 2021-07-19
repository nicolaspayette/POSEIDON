package uk.ac.ox.oxfish.biology;

import static com.google.common.cache.CacheLoader.from;
import static com.google.common.collect.ImmutableBiMap.toImmutableBiMap;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import java.nio.file.Path;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * Builds a SpeciesCodes map from a CSV file. The CSV file columns must be:
 * <ul>
 *  <li>{@code species_code}</li>
 *  <li>{@code species_name}</li>
 * </ul>
 */
public class SpeciesCodesFromFileFactory implements Supplier<SpeciesCodes> {

    private static final LoadingCache<Path, SpeciesCodes> cache =
        CacheBuilder.newBuilder().build(from(SpeciesCodesFromFileFactory::getSpeciesCodes));
    private Path speciesCodeFilePath;

    public SpeciesCodesFromFileFactory(final Path speciesCodeFilePath) {
        this.speciesCodeFilePath = speciesCodeFilePath;
    }

    @NotNull
    private static SpeciesCodes getSpeciesCodes(final Path path) {
        return new SpeciesCodes(
            parseAllRecords(path).stream().collect(toImmutableBiMap(
                r -> r.getString("species_code"),
                r -> r.getString("species_name")
            ))
        );
    }

    @SuppressWarnings("unused")
    public Path getSpeciesCodeFilePath() {
        return speciesCodeFilePath;
    }

    @SuppressWarnings("unused")
    public void setSpeciesCodeFilePath(final Path speciesCodeFilePath) {
        this.speciesCodeFilePath = speciesCodeFilePath;
    }

    @Override
    public SpeciesCodes get() {
        return cache.getUnchecked(speciesCodeFilePath);
    }
}
