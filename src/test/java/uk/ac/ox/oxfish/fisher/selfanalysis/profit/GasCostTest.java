package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 7/13/16.
 */
public class GasCostTest {


    @Test
    public void attached() throws Exception {

        //if I attach it to a fisher in a real simulation it should compute precisely the gas costs
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(1);
        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        MaximumStepsFactory fishingStrategy = new MaximumStepsFactory();
        scenario.setFishingStrategy(fishingStrategy);
        state.start();


        GasCost cost = new GasCost();
        LameTripSimulator simulator = new LameTripSimulator();

        final Fisher fisher = state.getFishers().get(0);
        fisher.addTripListener(
                new TripListener() {
                    @Override
                    public void reactToFinishedTrip(TripRecord record) {
                        System.out.println("day : " + state.getDay());
                        assertEquals(cost.cost(fisher,state,record,0d,fisher.getHoursAtSea() ),
                                     record.getTotalCosts(),.001d);
                        TripRecord simulated = simulator.simulateRecord(fisher, record.getMostFishedTileInTrip(),
                                                                        state, 24 * 5,
                                                                        new double[]{record.getSoldCatch()[0] / record.getEffort()}
                        );
                        assertEquals(simulated.getDistanceTravelled(),record.getDistanceTravelled(),.001d);
                        assertEquals(record.getEffort()+record.getDistanceTravelled()/fisher.getBoat().getSpeedInKph() - record.getDurationInHours(),0,.1d);
                        assertEquals(simulated.getEffort(),record.getEffort(),.001d);
                        assertEquals(simulated.getLitersOfGasConsumed(),record.getLitersOfGasConsumed(),.001d);
                        assertEquals(simulated.getDurationInHours(),record.getDurationInHours(),.1);

                    }
                }
        );

        for(int i=0; i<10000; i++)
            state.schedule.step(state);


    }
}