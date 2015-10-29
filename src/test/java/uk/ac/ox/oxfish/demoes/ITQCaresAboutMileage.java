package uk.ac.ox.oxfish.demoes;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.experiments.MarketFirstDemo;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.DoubleStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ITQCaresAboutMileage {

    @Test
    public void itqNotCaresAboutMileage() throws Exception {


        long seed = System.currentTimeMillis();
        FishState state=
                MarketFirstDemo.generateAndRunMarketDemo(MarketFirstDemo.MarketDemoPolicy.ITQ,
                                                         new FixedDoubleParameter(.1),
                                                         new UniformDoubleParameter(0, 20),
                                                         Paths.get("runs", "market1", "itqOil.csv").toFile(),
                                                         5, seed);

        //the correlation ought to be very small
        Specie specie = state.getSpecies().get(0);

        double[] mileage = new double[state.getFishers().size()];
        double[] catches =  new double[state.getFishers().size()];

        int i=0;
        for(Fisher fisher : state.getFishers())
        {
            mileage[i] = (((RandomCatchabilityTrawl) fisher.getGear()).getTrawlSpeed());
            catches[i] = fisher.getLatestYearlyObservation(
                    specie + " " + AbstractMarket.LANDINGS_COLUMN_NAME);

            i++;
        }

        System.out.println("seed " + seed);
        assertTrue(FishStateUtilities.computeCorrelation(mileage, catches) < -.6);
        //efficiency is 100%
        assertEquals(400000.0, DoubleStream.of(catches).sum(), .1);


        //make sure the same number of landings is recorded in the market
        DataColumn marketData = state.getPorts().iterator().next().getMarket(specie).getData().getColumn(
                AbstractMarket.LANDINGS_COLUMN_NAME);
        Iterator<Double> doubleIterator = marketData.descendingIterator();
        double landedCatches = 0;
        for(i=0;i<365;i++) {

            landedCatches+=doubleIterator.next();
        }
        //sum up the last 365 days of observations
        assertEquals(400000,landedCatches,.1);




    }
}
