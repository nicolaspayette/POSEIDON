package uk.ac.ox.oxfish.model.data;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class YearlyFisherDataSetTest {


    @Test
    public void testCash() throws Exception {

        YearlyFisherDataSet yearlyGatherer = new YearlyFisherDataSet();
        Fisher fisher = mock(Fisher.class);
        when(fisher.getBankBalance()).thenReturn(0d);
        yearlyGatherer.start(mock(FishState.class), fisher);

        when(fisher.getBankBalance()).thenReturn(1d);
        yearlyGatherer.step(mock(FishState.class));
        when(fisher.getBankBalance()).thenReturn(2d);
        yearlyGatherer.step(mock(FishState.class));
        when(fisher.getBankBalance()).thenReturn(3d);
        yearlyGatherer.step(mock(FishState.class));
        assertEquals(1d, yearlyGatherer.getDataView().get("CASH").get(0), .0001);
        assertEquals(2d, yearlyGatherer.getDataView().get("CASH").get(1), .0001);
        assertEquals(3d, yearlyGatherer.getDataView().get("CASH").get(2), .0001);
        assertEquals(1d,yearlyGatherer.getDataView().get("NET_CASH_FLOW").get(0),.0001);
        assertEquals(1d,yearlyGatherer.getDataView().get("NET_CASH_FLOW").get(1),.0001);
        assertEquals(1d,yearlyGatherer.getDataView().get("NET_CASH_FLOW").get(2),.0001);


    }
}