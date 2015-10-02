package uk.ac.ox.oxfish.utility.adaptation;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.maximization.AdaptationAlgorithm;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class ExplorationOrImitationMovementTest 
{


    @Test
    public void imitateBestFriend() throws Exception {

        Fisher optimizer = mock(Fisher.class);
        Fisher friend1 = mock(Fisher.class);
        Fisher friend2 = mock(Fisher.class);
        Map<Fisher,Double> fitness = new HashMap<>();
        fitness.put(friend1,100d);
        fitness.put(friend2,10d);
        fitness.put(optimizer,0d);
        Map<Fisher,SeaTile> locations = new HashMap<>();
        locations.put(friend1,mock(SeaTile.class));
        locations.put(friend2, mock(SeaTile.class));
        when(optimizer.getDirectedFriends()).thenReturn(Arrays.asList(friend1, friend2));

        //contains the result, for testing
        final SeaTile[] newObjective = {null};

        //imitate best friend
        AdaptationAlgorithm<SeaTile> algorithm = spy(new BeamHillClimbing<SeaTile>() {
            @Override
            public SeaTile randomStep(
                    FishState state, MersenneTwisterFast random, Fisher fisher, SeaTile current) {
                return null;
            }
        });

        Adaptation<SeaTile> test = new Adaptation<SeaTile>(
                (Predicate<Fisher>) fisher -> true,
                algorithm,
                (Actuator<SeaTile>) (fisher, change, model) -> newObjective[0] = change,
                new Sensor<SeaTile>() {
                    @Override
                    public SeaTile scan(Fisher fisher) {
                        return locations.get(fisher);
                    }
                },
                new ObjectiveFunction<Fisher>() {
                    @Override
                    public double computeCurrentFitness(Fisher observed) {
                        return fitness.get(observed);
                    }

                    @Override
                    public double computePreviousFitness(Fisher observed) {
                        throw new RuntimeException("Wrong!");
                    }
                },
                0d,
                1d
        );


        test.adapt(optimizer,new MersenneTwisterFast());

        //it should have not explored!
        verify(algorithm, never()).randomize(any(),any(),anyDouble(),any());
        verify(algorithm, never()).exploit(any(), any(), anyDouble(), any());
        //should have copied a friend
        assertTrue(newObjective[0].equals(locations.get(friend1)) ||
                           newObjective[0].equals(locations.get(friend2)) );


    }


    @Test
    public void evenIfFriendsAreWorseYouCallImitate() throws Exception {

        Fisher optimizer = mock(Fisher.class);
        Fisher friend1 = mock(Fisher.class);
        Fisher friend2 = mock(Fisher.class);
        Map<Fisher,Double> fitness = new HashMap<>();
        fitness.put(friend1,100d);
        fitness.put(friend2,10d);
        fitness.put(optimizer,1000d);
        Map<Fisher,SeaTile> locations = new HashMap<>();
        locations.put(friend1,mock(SeaTile.class));
        locations.put(friend2, mock(SeaTile.class));
        when(optimizer.getDirectedFriends()).thenReturn(Arrays.asList(friend1, friend2));

        //contains the result, for testing
        final SeaTile[] newObjective = {null};
        final SeaTile randomized = mock(SeaTile.class);

        //imitate best friend
        AdaptationAlgorithm<SeaTile> algorithm = spy(new BeamHillClimbing<SeaTile>() {
            @Override
            public SeaTile randomStep(
                    FishState state, MersenneTwisterFast random, Fisher fisher, SeaTile current) {
                return randomized;
            }
        });

        Adaptation<SeaTile> test = new Adaptation<>(
                fisher -> true,
                algorithm,
                (fisher, change, model) -> newObjective[0] = change,
                fisher -> locations.get(fisher),
                new ObjectiveFunction<Fisher>() {
                    @Override
                    public double computeCurrentFitness(Fisher observed) {
                        return fitness.get(observed);
                    }

                    @Override
                    public double computePreviousFitness(Fisher observed) {
                        throw new RuntimeException("Wrong!");
                    }
                },
                0d,
                1d
        );


        test.adapt(optimizer,new MersenneTwisterFast());

        //it should have neither explored nor imitate
        verify(algorithm, never()).exploit(any(),any(),anyDouble(),any());
        verify(algorithm, never()).randomize(any(), any(), anyDouble(), any());
        verify(algorithm, times(1)).imitate(any(), any(), anyDouble(), any(), anyCollection(), any(), any());
        //should have stayed on its own
        assertEquals(newObjective[0], null);
    }


    @Test
    public void friendsAreBetterButHardcodedExploration() throws Exception {
        Fisher optimizer = mock(Fisher.class);
        Fisher friend1 = mock(Fisher.class);
        Fisher friend2 = mock(Fisher.class);
        Map<Fisher,Double> fitness = new HashMap<>();
        fitness.put(friend1,100d);
        fitness.put(friend2,10d);
        fitness.put(optimizer,0d);
        Map<Fisher,SeaTile> locations = new HashMap<>();
        locations.put(friend1,mock(SeaTile.class));
        locations.put(friend2, mock(SeaTile.class));
        when(optimizer.getDirectedFriends()).thenReturn(Arrays.asList(friend1, friend2));

        //contains the result, for testing
        final SeaTile[] newObjective = {null};
        final SeaTile randomized = mock(SeaTile.class);

        //imitate best friend
        AdaptationAlgorithm<SeaTile> algorithm = spy(new BeamHillClimbing<SeaTile>() {
            @Override
            public SeaTile randomStep(
                    FishState state, MersenneTwisterFast random, Fisher fisher, SeaTile current) {
                return randomized;
            }
        });

        Adaptation<SeaTile> test = new Adaptation<>(
                fisher -> true,
                algorithm,
                (fisher, change, model) -> newObjective[0] = change,
                fisher -> locations.get(fisher),
                new ObjectiveFunction<Fisher>() {
                    @Override
                    public double computeCurrentFitness(Fisher observed) {
                        return fitness.get(observed);
                    }

                    @Override
                    public double computePreviousFitness(Fisher observed) {
                        throw new RuntimeException("Wrong!");
                    }
                },
                1d,
                1d
        );


        test.adapt(optimizer, new MersenneTwisterFast());

        //it should have explored! explored!
        verify(algorithm, times(1)).randomize(any(),any(),anyDouble(),any());
        verify(algorithm, never()).exploit(any(), any(), anyDouble(), any());
        verify(algorithm, never()).imitate(any(), any(), anyDouble(), any(), anyCollection(), any(), any());
        //should have randomized
        assertEquals(newObjective[0], randomized);

    }
}