/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.experiments.indonesia;




import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesMappedFactory;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesMarketFactory;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.regs.MaxHoursOutRegulation;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.MaxHoursOutFactory;
import uk.ac.ox.oxfish.model.regs.factory.TriggerRegulationFactory;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Slice3Sweeps {

    public static final String DIRECTORY = "docs/indonesia_hub/runs/712/slice3/policy/";
    public static final int MIN_DAYS_OUT = 50;
    public static final int RUNS_PER_POLICY = 1;
    public static final int MAX_DAYS_OUT = 250;


    public static void main(String[] args) throws IOException {


//        effortControl("all", new String[]{"big","small","medium"}, "fixed_recruits", 4, MIN_DAYS_OUT);
//        effortControl("large", new String[]{"big"}, "fixed_recruits", 4, MIN_DAYS_OUT);
//        effortControl("medium", new String[]{"big","medium"}, "fixed_recruits", 4, MIN_DAYS_OUT);


//
//        effortControl("all", new String[]{"big","small","medium"}, "optimistic_recruits", 1, MIN_DAYS_OUT);
//        effortControl("large", new String[]{"big"}, "optimistic_recruits", 1, MIN_DAYS_OUT);
//        effortControl("medium", new String[]{"big","medium"}, "optimistic_recruits", 1, MIN_DAYS_OUT);

    //    effortControl("all4", new String[]{"big","small","medium"}, "optimistic_recruits_spinup_fixedmarket", 1, MIN_DAYS_OUT);
//        effortControl("large", new String[]{"big"}, "optimistic_recruits_spinup_fixedmarket", 1, MIN_DAYS_OUT);
//        effortControl("medium", new String[]{"big","medium"}, "optimistic_recruits_spinup_fixedmarket", 1, MIN_DAYS_OUT);



//       fleetReduction("fleetreduction","optimistic_recruits",1);
//       fleetReduction("fleetreduction","fixed_recruits",4);
//        fleetReduction("fleetreduction","optimistic_recruits_spinup_fixedmarket",1);

//        pricePremium("premium_malabaricus","fixed_recruits",4,"Lutjanus malabaricus");
//        pricePremium("premium_multidens","fixed_recruits",4,"Pristipomoides multidens");
//
//        pricePremium("premium_malabaricus","optimistic_recruits",1,"Lutjanus malabaricus");
//        pricePremium("premium_multidens","optimistic_recruits",1,"Pristipomoides multidens");
//
//        pricePremium("premium_malabaricus","optimistic_recruits_spinup_fixedmarket",1,"Lutjanus malabaricus");
//        pricePremium("premium_multidens","optimistic_recruits_spinup_fixedmarket",1,"Pristipomoides multidens");


//        adaptiveSPR("spr_malabaricus", MIN_DAYS_OUT, "optimistic_recruits", "Lutjanus malabaricus", "100_malabaricus",
//                    false);
//        adaptiveSPR("spr_multidens", MIN_DAYS_OUT, "optimistic_recruits", "Pristipomoides multidens", "100_multidens",
//                    false);
//        adaptiveSPR("spr_malabaricus", MIN_DAYS_OUT, "fixed_recruits", "Lutjanus malabaricus", "100_malabaricus", false);
//        adaptiveSPR("spr_multidens", MIN_DAYS_OUT, "fixed_recruits", "Pristipomoides multidens", "100_multidens", false);
//        adaptiveSPR("spr_malabaricus", MIN_DAYS_OUT, "optimistic_recruits_spinup_fixedmarket", "Lutjanus malabaricus", "100_malabaricus",
//                    false);
//        adaptiveSPR("spr_multidens", MIN_DAYS_OUT, "optimistic_recruits_spinup_fixedmarket", "Pristipomoides multidens", "100_multidens",
//                    false);

//        adaptiveSPR("oraclespr_malabaricus", MIN_DAYS_OUT, "optimistic_recruits_spinup_fixedmarket",
//                    "Lutjanus malabaricus", "100_malabaricus",
//                    true);
//        adaptiveSPR("oraclespr_multidens", MIN_DAYS_OUT, "optimistic_recruits_spinup_fixedmarket",
//                    "Pristipomoides multidens", "100_multidens",
//                    true);

//
//        recruitmentFailure("recruit_failure","fixed_recruits",4,2);
//        recruitmentFailure("recruit_failure","optimistic_recruits",4,2);
//        recruitmentFailure("recruit_failure","optimistic_recruits_spinup_fixedmarket",4,2);
    }

    private static void effortControl(
            String name,
            String[] modifiedTags, final String filename, final int shockYear,
            final int minDaysOut) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int maxDaysOut = MAX_DAYS_OUT; maxDaysOut>= minDaysOut; maxDaysOut-=10) {

            BatchRunner runner = setupRunner(filename);


            int finalMaxDaysOut = maxDaysOut;

            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            runner.setScenarioSetup(
                    scenario -> {

                        //at year 4, impose regulation
                        FlexibleScenario flexible = (FlexibleScenario) scenario;
                        flexible.getPlugins().add(
                                fishState -> new AdditionalStartable() {
                                    @Override
                                    public void start(FishState model) {

                                        model.scheduleOnceAtTheBeginningOfYear(
                                                (Steppable) simState -> {
                                                    fisherloop:
                                                    for (Fisher fisher :
                                                            ((FishState) simState).getFishers()) {

                                                        for (String tag : modifiedTags) {
                                                            if (fisher.getTags().contains(tag)) {
                                                                fisher.setRegulation(
                                                                        new MaxHoursOutRegulation(
                                                                                new ProtectedAreasOnly(),
                                                                                finalMaxDaysOut*24d
                                                                        ));
                                                                continue fisherloop;
                                                            }
                                                        }
                                                    }
                                                },
                                                StepOrder.DAWN,
                                                shockYear
                                        );


                                    }

                                    @Override
                                    public void turnOff() {

                                    }
                                }
                        );

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMaxDaysOut).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }

    private static void fleetReduction(
            String name,
            final String filename, final int shockYear) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(double probability=0; probability<=.05; probability=FishStateUtilities.round5(probability+.005)) {

            BatchRunner runner = setupRunner(filename);




            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            double finalProbability = probability;
            runner.setScenarioSetup(
                    scenario -> {

                        //at year 4, impose regulation
                        FlexibleScenario flexible = (FlexibleScenario) scenario;
                        flexible.getPlugins().add(
                                fishState -> new AdditionalStartable() {
                                    /**
                                     * this gets called by the fish-state right after the scenario has started. It's
                                     * useful to set up steppables
                                     * or just to percolate a reference to the model
                                     *
                                     * @param model the model
                                     */
                                    @Override
                                    public void start(FishState model) {
                                        model.scheduleEveryYear(new Steppable() {
                                            @Override
                                            public void step(SimState simState) {
                                                if(model.getYear()<shockYear)
                                                    return;
                                                List<Fisher> toKill = new LinkedList<>();

                                                for(Fisher fisher : model.getFishers()) {
                                                    if (model.getRandom().nextDouble() < finalProbability)
                                                        toKill.add(fisher);
                                                }
                                                for (Fisher sacrifice : toKill) {
                                                    model.killSpecificFisher(sacrifice);

                                                }


                                            }
                                        },StepOrder.DAWN);
                                    }

                                    /**
                                     * tell the startable to turnoff,
                                     */
                                    @Override
                                    public void turnOff() {

                                    }
                                }
                        );

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalProbability).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }


    private static void pricePremium(
            String name,
            final String filename, final int shockYear,
            final String premiumSpecies
    )throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(double markup=0; markup<=3; markup=FishStateUtilities.round(markup+.1)) {

            BatchRunner runner = setupRunner(filename);




            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            double finalMarkup = markup;
            runner.setScenarioSetup(
                    scenario -> {
                        MersenneTwisterFast fakeRandom = new MersenneTwisterFast(0);

                        //at year 4, impose regulation
                        FlexibleScenario flexible = (FlexibleScenario) scenario;

                        ThreePricesMarketFactory market =
                                ((ThreePricesMappedFactory) flexible.getMarket()).getMarkets().get(
                                premiumSpecies
                        );

                        market.setHighAgeThreshold(
                                new FixedDoubleParameter(
                                ((FixedDoubleParameter) market.getHighAgeThreshold()).getFixedValue() *
                                        (1d+ finalMarkup)
                                )
                        );

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMarkup).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }


    //"SPR " + "Pristipomoides multidens" + " " + "100_multidens"
    private static void adaptiveSPR(
            String name,
            final int minDaysOut,
            final String filename,
            final String speciesTargeted,
            final String survey_name,
            boolean oracleTargeting)throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int maxDaysOut = MAX_DAYS_OUT; maxDaysOut>= minDaysOut; maxDaysOut-=10) {

            BatchRunner runner = setupRunner(filename);




            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            int finalMaxDaysOut = maxDaysOut;
            runner.setScenarioSetup(
                    scenario -> {
                        for(FisherDefinition definition : ((FlexibleScenario) scenario).getFisherDefinitions()) {
                            TriggerRegulationFactory regulation = new TriggerRegulationFactory();
                            regulation.setBusinessAsUsual(new AnarchyFactory());
                            regulation.setEmergency(new MaxHoursOutFactory(finalMaxDaysOut *24));
                            regulation.setHighThreshold(new FixedDoubleParameter(.4));
                            regulation.setLowThreshold(new FixedDoubleParameter(.2));
                            if(oracleTargeting)
                                regulation.setIndicatorName("SPR Oracle - "+speciesTargeted);
                            else
                                regulation.setIndicatorName("SPR "+speciesTargeted+ " " + survey_name);



                            definition.setRegulation(
                                    regulation
                            );
                        }


                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMaxDaysOut).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }



    //no policy, but simulates a year 1 death of all bin 0 and bin 1 population
    private static void recruitmentFailure(
            String name,
            final String filename, final int shockYear, final int runs) throws IOException {
        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();


            BatchRunner runner = setupRunner(filename);

            for(int failure = 1; failure>=0; failure--) {


                //basically we want year 4 to change big boats regulations.
                //because I coded "run" poorly, we have to go through this series of pirouettes
                //to get it done right
                int finalFailure = failure;
                runner.setScenarioSetup(
                        scenario -> {

                            //at year 4, impose regulation
                            FlexibleScenario flexible = (FlexibleScenario) scenario;
                            flexible.getPlugins().add(
                                    fishState -> new AdditionalStartable() {
                                        /**
                                         * this gets called by the fish-state right after the scenario has started. It's
                                         * useful to set up steppables
                                         * or just to percolate a reference to the model
                                         *
                                         * @param model the model
                                         */
                                        @Override
                                        public void start(FishState model) {
                                            if (finalFailure >0) {
                                                model.scheduleOnceAtTheBeginningOfYear(new Steppable() {
                                                    @Override
                                                    public void step(SimState simState) {

                                                        for (SeaTile tile : model.getMap().getAllSeaTilesExcludingLandAsList())
                                                            for (Species species : model.getSpecies()) {


                                                                double[][] matrix = tile.getAbundance(
                                                                        species).asMatrix();
                                                                if(matrix == null || matrix.length==0 ||
                                                                        matrix[0].length ==0 ||
                                                                species.isImaginary())
                                                                    continue;
                                                                matrix[0][0] = 0;
                                                                matrix[0][1] = 0;
                                                            }


                                                    }
                                                }, StepOrder.DAWN, shockYear);
                                            }
                                        }
                                        /**
                                         * tell the startable to turnoff,
                                         */
                                        @Override
                                        public void turnOff() {

                                        }
                                    }
                            );

                        }
                );


                runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                    @Override
                    public void consume(StringBuffer writer, FishState model, Integer year) {
                        writer.append(finalFailure).append(",");
                    }
                });


                //while (runner.getRunsDone() < 1) {
                for (int i = 0; i < runs; i++) {
                    StringBuffer tidy = new StringBuffer();
                    runner.run(tidy);
                    fileWriter.write(tidy.toString());
                    fileWriter.flush();
                }
            }
        fileWriter.close();

    }


    @NotNull
    public static BatchRunner setupRunner(String filename) {
        ArrayList<String> columnsToPrint = Lists.newArrayList(
                "Average Cash-Flow",
                "Average Cash-Flow of population0",
                "Average Cash-Flow of population1",
                "Average Cash-Flow of population2",
                "Average Number of Trips of population0",
                "Average Number of Trips of population1",
                "Average Number of Trips of population2",
                "Number Of Active Fishers of population0",
                "Number Of Active Fishers of population1",
                "Number Of Active Fishers of population2",
                "Average Distance From Port of population0",
                "Average Distance From Port of population1",
                "Average Distance From Port of population2",
                "Average Trip Duration of population0",
                "Average Trip Duration of population1",
                "Average Trip Duration of population2",
                "Epinephelus areolatus Landings of population0",
                "Pristipomoides multidens Landings of population0",
                "Lutjanus malabaricus Landings of population0",
                "Lutjanus erythropterus Landings of population0",
                "Others Landings of population0",

                "Epinephelus areolatus Landings of population1",
                "Pristipomoides multidens Landings of population1",
                "Lutjanus malabaricus Landings of population1",
                "Lutjanus erythropterus Landings of population1",
                "Others Landings of population1",
                "Epinephelus areolatus Landings of population2",
                "Pristipomoides multidens Landings of population2",
                "Lutjanus malabaricus Landings of population2",
                "Lutjanus erythropterus Landings of population2",
                "Others Landings of population2",

                "Biomass Epinephelus areolatus",
                "Biomass Pristipomoides multidens",
                "Biomass Lutjanus malabaricus",
                "Biomass Lutjanus erythropterus",
                "Total Landings of population0",
                "Total Landings of population1",
                "Total Landings of population2",

                "SPR " + "Epinephelus areolatus" + " " + "100_areolatus",
                "SPR " + "Pristipomoides multidens" + " " + "100_multidens",
                "SPR " + "Lutjanus malabaricus" + " " + "100_malabaricus",
                "SPR " + "Lutjanus erythropterus" + " " + "100_erythropterus",
                "SPR Oracle - " + "Epinephelus areolatus",
                "SPR Oracle - " + "Pristipomoides multidens" ,
                "SPR Oracle - " + "Lutjanus malabaricus",
                "SPR Oracle - " + "Lutjanus erythropterus",
                "Percentage Mature Catches " + "Epinephelus areolatus" + " " + "100_areolatus",
                "Percentage Mature Catches " + "Pristipomoides multidens" + " " + "100_multidens",
                "Percentage Mature Catches " + "Lutjanus malabaricus" + " " + "100_malabaricus",
                "Percentage Mature Catches " + "Lutjanus erythropterus" + " " + "100_erythropterus"

        );
        for(int i=0; i<25; i++) {
            columnsToPrint.add("Epinephelus areolatus Catches (kg) - age bin " + i);
            columnsToPrint.add("Pristipomoides multidens Catches (kg) - age bin " + i);
            columnsToPrint.add("Lutjanus malabaricus Catches (kg) - age bin " + i);
            columnsToPrint.add("Lutjanus erythropterus Catches (kg) - age bin " + i);

            columnsToPrint.add("Epinephelus areolatus Abundance 0."+i+" at day " + 365);
            columnsToPrint.add("Lutjanus malabaricus Abundance 0."+i+" at day " + 365);
            columnsToPrint.add("Pristipomoides multidens Abundance 0."+i+" at day " + 365);
            columnsToPrint.add("Lutjanus erythropterus Abundance 0."+i+" at day " + 365);


            columnsToPrint.add("Epinephelus areolatus Catches(#) 0."+i+" 100_areolatus");
            columnsToPrint.add("Lutjanus malabaricus Catches(#) 0."+i+" 100_malabaricus");
            columnsToPrint.add("Pristipomoides multidens Catches(#) 0."+i+" 100_multidens");
            columnsToPrint.add("Lutjanus erythropterus Catches(#) 0."+i+" 100_erythropterus");
        }

        return new BatchRunner(
                Paths.get(DIRECTORY,
                          filename + ".yaml"),
                15,
                columnsToPrint,
                Paths.get(DIRECTORY,
                          filename),
                null,
                System.currentTimeMillis(),
                -1
        );
    }


    public static void enforcement(
            String name,
            String cheatingTag, final String filename) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+"_enforcement.csv").toFile());
        fileWriter.write("run,year,enforcement,policy,variable,value\n");
        fileWriter.flush();

        for(int maxDaysOut=200; maxDaysOut>=50; maxDaysOut-=10) {
            for(double probabilityOfCheating = 0; probabilityOfCheating<=1; probabilityOfCheating+=.2) {

                probabilityOfCheating = FishStateUtilities.round(probabilityOfCheating);
                BatchRunner runner = setupRunner(filename);


                int finalMaxDaysOut = maxDaysOut;

                //basically we want year 4 to change big boats regulations.
                //because I coded "run" poorly, we have to go through this series of pirouettes
                //to get it done right
                double finalProbabilityOfCheating = probabilityOfCheating;
                runner.setScenarioSetup(
                        scenario -> {

                            //at year 4, impose regulation
                            FlexibleScenario flexible = (FlexibleScenario) scenario;
                            flexible.getPlugins().add(
                                    fishState -> new AdditionalStartable() {
                                        @Override
                                        public void start(FishState model) {

                                            model.scheduleOnceAtTheBeginningOfYear(
                                                    (Steppable) simState -> {
                                                        fisherloop:
                                                        for (Fisher fisher :
                                                                ((FishState) simState).getFishers()) {

                                                            if (!fisher.getTags().contains(cheatingTag)) {
                                                                fisher.setRegulation(
                                                                        new FishingSeason(true, finalMaxDaysOut));
                                                            } else {
                                                                if (!model.getRandom().nextBoolean(
                                                                        finalProbabilityOfCheating))
                                                                    fisher.setRegulation(
                                                                            new FishingSeason(true, finalMaxDaysOut));

                                                            }


                                                        }
                                                    },
                                                    StepOrder.DAWN,
                                                    4
                                            );


                                        }

                                        @Override
                                        public void turnOff() {

                                        }
                                    }
                            );

                        }
                );


                final String cheatingString = Double.toString(probabilityOfCheating);
                runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                    @Override
                    public void consume(StringBuffer writer, FishState model, Integer year) {
                        writer.append(cheatingString).append(",").append(finalMaxDaysOut).append(",");
                    }
                });


                //while (runner.getRunsDone() < 1) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
                //   }
            }
        }
        fileWriter.close();
    }
}
