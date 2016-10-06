package uk.ac.ox.oxfish.fisher.heatmap.regression;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.strategies.destination.HeatmapDestinationStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.ExploreImitateAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.AdaptationAlgorithm;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.maximization.RandomStep;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * A regression that works by Beam hill-climbing to update regression parameters of its delegate
 * Created by carrknight on 8/26/16.
 */
public class SocialTuningRegression<V>  implements GeographicalRegression<V>
{

    /**
     * the underlying regression doing all the work
     */
    private final  GeographicalRegression<V> delegate;


    private final AdaptationProbability probability;



    final private boolean yearly;


    final private AdaptationAlgorithm<double[]> optimizer;

    final static private Sensor<double[]> parameterSensor = new Sensor<double[]>() {
        @Override
        public double[] scan(Fisher fisher) {
            return ((HeatmapDestinationStrategy) fisher.getDestinationStrategy()).getHeatmap().getParametersAsArray();
        }
    };

    public SocialTuningRegression(
            GeographicalRegression<V> delegate,
            AdaptationProbability probability,
            boolean yearly) {
        this.delegate = delegate;
        this.probability = probability;

        this.yearly = yearly;



     /*   optimizer = new ParticleSwarmAlgorithm<double[]>(
                inertia,
                memoryWeight,
                friendWeight,
                initialParameters.length,
                //shallow memory, I am afraid
                new Function<Fisher, double[]>() {
                    @Override
                    public double[] apply(Fisher fisher) {
                        return parameterSensor.scan(fisher);
                    }
                },
                //no projection, force positive?
                new ParticleSwarmAlgorithm.PSOCoordinateTransformer<double[]>() {
                    @Override
                    public double[] toCoordinates(double[] variable, Fisher fisher, FishState model) {
                        if(forcePositive)
                            for(int i=0; i<variable.length; i++)
                                variable[i]= Math.max(variable[i],0);
                        return variable;
                    }

                    @Override
                    public double[] fromCoordinates(double[] variable, Fisher fisher, FishState model) {
                        if(forcePositive)
                            for(int i=0; i<variable.length; i++)
                                variable[i]= Math.max(variable[i],0);
                        return variable;
                    }
                },
                explorationShocks,
                random,
                initialVelocity);
*/
     optimizer = new BeamHillClimbing<double[]>(
             new RandomStep<double[]>() {
                 @Override
                 public double[] randomStep(FishState state, MersenneTwisterFast random, Fisher fisher,
                                            double[] current)
                 {
                     double[] toReturn = Arrays.copyOf(current,current.length);
                     for(int i=0; i<current.length; i++)
                         toReturn[i]= toReturn[i] *(.95+random.nextDouble()*.1);
                     return toReturn;
                 }
             }
     );


    }


    private ExploreImitateAdaptation<double[]> adaptation;

    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model, fisher);
        //start the optimizer!
        Predicate<Fisher> predictate = new Predicate<Fisher>() {
            @Override
            public boolean test(Fisher fisher) {
                return fisher.getDestinationStrategy() instanceof HeatmapDestinationStrategy;
            }
        };
        Actuator<double[]> actuator = new Actuator<double[]>() {
            @Override
            public void apply(Fisher fisher, double[] change, FishState model) {
                model.scheduleOnce(
                        new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                ((HeatmapDestinationStrategy) fisher.getDestinationStrategy()).getHeatmap().setParameters(
                                        Arrays.copyOf(change,change.length));
                            }
                        },
                        StepOrder.DAWN
                );

            }
        };

        if(yearly) {
            adaptation = new ExploreImitateAdaptation<>(
                    predictate,
                    optimizer,
                    actuator,
                    parameterSensor,
                    new CashFlowObjective(365),probability);
            fisher.addYearlyAdaptation(
                    adaptation);
        }
        //else bimonthly
        else
        {
            adaptation = new ExploreImitateAdaptation<>(
                    predictate,
                    optimizer,
                    actuator,
                    parameterSensor,
                    new CashFlowObjective(60), 0, 1);
            fisher.addBiMonthlyAdaptation(adaptation);
        }





    }

    @Override
    public void turnOff(Fisher fisher) {
        delegate.turnOff(fisher);
        if(adaptation != null)
        {
            if(yearly)
                fisher.removeYearlyAdaptation(adaptation);
            else
                fisher.removeBiMonthlyAdaptation(adaptation);
        }
    }

    /**
     * predict numerical value here
     * @param tile
     * @param time
     * @param fisher
     * @return
     */
    @Override
    public double predict(SeaTile tile, double time, Fisher fisher) {
        return delegate.predict(tile, time, fisher);
    }

    /**
     * learn from this observation
     * @param observation
     * @param fisher
     */
    @Override
    public void addObservation(
            GeographicalObservation<V> observation,
            Fisher fisher) {
        delegate.addObservation(observation, fisher);
    }

    /**
     * turn the "V" value of the geographical observation into a number
     * @param observation
     * @param fisher
     * @return
     */
    @Override
    public double extractNumericalYFromObservation(
            GeographicalObservation<V> observation,
            Fisher fisher) {
        return delegate.extractNumericalYFromObservation(observation, fisher);
    }

    /**
     * Transforms the parameters used (and that can be changed) into a double[] array so that it can be inspected
     * from the outside without knowing the inner workings of the regression
     * @return an array containing all the parameters of the model
     */
    @Override
    public double[] getParametersAsArray() {
        return delegate.getParametersAsArray();
    }

    /**
     * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
     * to transition to these parameters
     * @param parameterArray the new parameters for this regresssion
     */
    @Override
    public void setParameters(double[] parameterArray) {
        delegate.setParameters(parameterArray);
    }



    /**
     * Getter for property 'probability'.
     *
     * @return Value for property 'probability'.
     */
    public AdaptationProbability getProbability() {
        return probability;
    }

    /**
     * Getter for property 'yearly'.
     *
     * @return Value for property 'yearly'.
     */
    public boolean isYearly() {
        return yearly;
    }
}