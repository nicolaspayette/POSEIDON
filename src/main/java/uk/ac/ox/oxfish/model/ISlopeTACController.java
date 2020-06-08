package uk.ac.ox.oxfish.model;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.regs.policymakers.ISlope;
import uk.ac.ox.oxfish.model.regs.policymakers.TargetToTACController;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class ISlopeTACController implements AlgorithmFactory<AdditionalStartable> {


    private String indicatorColumnName = "Species 0 CPUE";
    private String catchColumnName = "Species 0 Landings";

    private DoubleParameter gainLambdaParameter = new FixedDoubleParameter(0.4);

    private DoubleParameter precautionaryScaling = new FixedDoubleParameter(0.8);

    private int interval = 5;
    private int startingYear = 10;


    @Override
    public AdditionalStartable apply(FishState fishState) {
        return new AdditionalStartable() {
            @Override
            public void start(FishState model) {
                fishState.scheduleOnceInXDays(
                        new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                TargetToTACController controller = new TargetToTACController(
                                        new ISlope(
                                                catchColumnName,
                                                indicatorColumnName,
                                                gainLambdaParameter.apply(model.getRandom()),
                                                precautionaryScaling.apply(model.getRandom()),
                                                interval
                                        )
                                );
                                controller.start(model);
                                controller.step(model);
                            }
                        },
                        StepOrder.DAWN,
                        365 * startingYear + 1
                );
            }
        };

    }


    public String getIndicatorColumnName() {
        return indicatorColumnName;
    }

    public void setIndicatorColumnName(String indicatorColumnName) {
        this.indicatorColumnName = indicatorColumnName;
    }

    public String getCatchColumnName() {
        return catchColumnName;
    }

    public void setCatchColumnName(String catchColumnName) {
        this.catchColumnName = catchColumnName;
    }

    public DoubleParameter getGainLambdaParameter() {
        return gainLambdaParameter;
    }

    public void setGainLambdaParameter(DoubleParameter gainLambdaParameter) {
        this.gainLambdaParameter = gainLambdaParameter;
    }

    public DoubleParameter getPrecautionaryScaling() {
        return precautionaryScaling;
    }

    public void setPrecautionaryScaling(DoubleParameter precautionaryScaling) {
        this.precautionaryScaling = precautionaryScaling;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getStartingYear() {
        return startingYear;
    }

    public void setStartingYear(int startingYear) {
        this.startingYear = startingYear;
    }
}




