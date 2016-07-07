package uk.ac.ox.oxfish.fisher.heatmap.regression;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Created by carrknight on 6/28/16.
 */
public class TimeAndSpaceKernelRegression implements GeographicalRegression {


    private final TimeOnlyKernelRegression timeRegression;

    private final SpaceOnlyKernelRegression spaceRegression;


    public TimeAndSpaceKernelRegression(double timeBandwidth,
                                        double spaceBandwidth,
                                        int numberOfObservations) {

        timeRegression = new TimeOnlyKernelRegression(numberOfObservations,timeBandwidth);
        spaceRegression = new SpaceOnlyKernelRegression(numberOfObservations,spaceBandwidth);

    }

    @Override
    public void addObservation(GeographicalObservation observation, Fisher fisher) {
        timeRegression.addObservation(observation,fisher );
        spaceRegression.addObservation(observation, fisher);
    }

    @Override
    public double predict(SeaTile tile, double time, FishState state, Fisher fisher) {

        if(tile.getAltitude()>=0)
            return 0;
        else
            return predict(tile.getGridX(),tile.getGridY(),time);
    }


    public double predict(int x, int y, double time) {

        double[] timePrediction = timeRegression.generatePrediction(x, y, time);
        double[] spacePrediction = spaceRegression.generatePrediction(x, y, time);

        return (timePrediction[0]+spacePrediction[0])/(timePrediction[1]+spacePrediction[1]);


    }

    /**
     * Getter for property 'bandwidth'.
     *
     * @return Value for property 'bandwidth'.
     */
    public double getTimeBandwidth() {
        return timeRegression.getBandwidth();
    }

    /**
     * Setter for property 'bandwidth'.
     *
     * @param bandwidth Value to set for property 'bandwidth'.
     */
    public void setTimeBandwidth(double bandwidth) {
        timeRegression.setBandwidth(bandwidth);
    }

    /**
     * Getter for property 'maximumNumberOfObservations'.
     *
     * @return Value for property 'maximumNumberOfObservations'.
     */
    public int getMaximumNumberOfObservationsTime() {
        return timeRegression.getMaximumNumberOfObservations();
    }


    /**
     * Getter for property 'bandwidth'.
     *
     * @return Value for property 'bandwidth'.
     */
    public double getSpaceBandwidth() {
        return spaceRegression.getBandwidth();
    }

    /**
     * Setter for property 'bandwidth'.
     *
     * @param bandwidth Value to set for property 'bandwidth'.
     */
    public void setSpaceBandwidth(double bandwidth) {
        spaceRegression.setBandwidth(bandwidth);
    }

    /**
     * Getter for property 'maximumNumberOfObservations'.
     *
     * @return Value for property 'maximumNumberOfObservations'.
     */
    public int getMaximumNumberOfObservationsSpace() {
        return spaceRegression.getMaximumNumberOfObservations();
    }
}