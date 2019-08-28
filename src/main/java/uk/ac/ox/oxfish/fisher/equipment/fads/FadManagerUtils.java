package uk.ac.ox.oxfish.fisher.equipment.fads;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.PurseSeineGear;

import java.util.Optional;
import java.util.stream.Stream;

import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;

/**
 * This provides convenience implementations for the various classes that need to access the
 * FadManager instance buried in the fisher's PurseSeineGear.
 */
public interface FadManagerUtils {

    static FadManager getFadManager(Fisher fisher) {
        return ((PurseSeineGear) fisher.getGear()).getFadManager();
    }

    static Optional<Fad> oneOfFadsHere(Fisher fisher) {
        return getFadManager(fisher).oneOfFadsHere();
    }

    static Optional<Fad> oneOfDeployedFads(Fisher fisher) {
        return oneOf(getFadManager(fisher).getDeployedFads(), fisher.grabRandomizer());
    }

    static Stream<Fad> fadsHere(Fisher fisher) { return bagToStream(getFadManager(fisher).getFadsHere()); }

}
