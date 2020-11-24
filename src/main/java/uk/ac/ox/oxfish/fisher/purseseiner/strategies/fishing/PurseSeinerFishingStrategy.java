/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Streams;
import ec.util.MersenneTwisterFast;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.OpportunisticFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.SearchAction;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.ActionAttractionField;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogisticFunction;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.Math.exp;
import static java.util.Comparator.comparingDouble;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear.getPurseSeineGear;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class PurseSeinerFishingStrategy implements FishingStrategy {

    private final double movingThreshold = 0.1; // TODO: this needs to be a parameter
    private final Function<Fisher, Map<Class<? extends PurseSeinerAction>, Double>> actionWeightsLoader;
    private final Function<Fisher, SetOpportunityDetector> setOpportunityLocatorProvider;
    private final Multiset<Class<? extends PurseSeinerAction>> actionCounts = HashMultiset.create();
    private final DoubleUnaryOperator searchActionValueFunction;
    private final double searchActionDecayConstant;
    private final DoubleUnaryOperator fadDeploymentActionValueFunction;
    private final double fadDeploymentActionDecayConstant;
    private ImmutableMap<? extends Class<? extends PurseSeinerAction>, ActionAttractionField> attractionFields;
    private SetOpportunityDetector setOpportunityDetector;
    private Map<Class<? extends PurseSeinerAction>, Double> actionWeights;
    private List<Entry<PurseSeinerAction, Double>> potentialActions = ImmutableList.of();

    public PurseSeinerFishingStrategy(
        final Function<Fisher, Map<Class<? extends PurseSeinerAction>, Double>> actionWeightsLoader,
        final Function<Fisher, SetOpportunityDetector> setOpportunityLocatorProvider,
        final double searchActionSigmoidMidpoint,
        final double searchActionSigmoidSteepness,
        final double searchActionDecayConstant,
        final double fadDeploymentActionSigmoidMidpoint,
        final double fadDeploymentActionSigmoidSteepness,
        final double fadDeploymentActionDecayConstant
    ) {
        this.actionWeightsLoader = actionWeightsLoader;
        this.setOpportunityLocatorProvider = setOpportunityLocatorProvider;
        this.searchActionValueFunction =
            new LogisticFunction(searchActionSigmoidMidpoint, searchActionSigmoidSteepness);
        this.searchActionDecayConstant = searchActionDecayConstant;
        this.fadDeploymentActionValueFunction =
            new LogisticFunction(fadDeploymentActionSigmoidMidpoint, fadDeploymentActionSigmoidSteepness);
        this.fadDeploymentActionDecayConstant = fadDeploymentActionDecayConstant;
    }

    @Override public void start(final FishState model, final Fisher fisher) {
        actionWeights = normalizeWeights(actionWeightsLoader.apply(fisher));
        setOpportunityDetector = setOpportunityLocatorProvider.apply(fisher);
        attractionFields =
            getPurseSeineGear(fisher)
                .getAttractionFields()
                .stream()
                .filter(field -> field instanceof ActionAttractionField)
                .map(field -> (ActionAttractionField) field)
                .collect(toImmutableMap(
                    ActionAttractionField::getActionClass,
                    identity()
                ));
    }

    private <T> Map<T, Double> normalizeWeights(final Map<T, Double> weightMap) {
        final double sumOfWeights =
            weightMap.values().stream().mapToDouble(Double::doubleValue).sum();
        return weightMap.entrySet().stream()
            .collect(toImmutableMap(Entry::getKey, entry -> entry.getValue() / sumOfWeights));
    }

    @Override public boolean shouldFish(
        final Fisher fisher,
        final MersenneTwisterFast random,
        final FishState fishState,
        final TripRecord currentTrip
    ) {
        if (potentialActions.isEmpty()) potentialActions = findPotentialActions(fisher);
        if (potentialActions.isEmpty()) actionCounts.clear();
        return !potentialActions.isEmpty();
    }

    private List<Entry<PurseSeinerAction, Double>> findPotentialActions(final Fisher fisher) {

        if (fisher.getLocation().isLand()) return ImmutableList.of();

        final Int2D gridLocation = fisher.getLocation().getGridLocation();
        final List<PurseSeinerAction> setActions = setOpportunityDetector.possibleSetActions();

        final Stream<Entry<PurseSeinerAction, Double>> weightedSetActions =
            setActions.stream().map(this::weightedAction);

        // Generate a search action for each of the set classes with no opportunities,
        // and give them a weight equivalent to the class they replace
        final ImmutableSet<? extends Class<?>> setActionClasses =
            setActions.stream().map(Object::getClass).collect(toImmutableSet());
        final Stream<Entry<PurseSeinerAction, Double>> weightedSearchActions = Stream
            .of(
                OpportunisticFadSetAction.class,
                NonAssociatedSetAction.class,
                DolphinSetAction.class
            )
            .filter(actionClass -> !setActionClasses.contains(actionClass))
            .map(actionClass ->
                weightedAction(new SearchAction(
                    fisher,
                    computeActionValue(
                        actionCounts.count(SearchAction.class),
                        attractionFields.get(actionClass).getValueAt(gridLocation),
                        searchActionValueFunction,
                        searchActionDecayConstant
                    ),
                    setOpportunityDetector
                ))
            );

        Stream<Entry<PurseSeinerAction, Double>> weightedFadDeploymentAction = Stream
            .of(new FadDeploymentAction(
                fisher,
                computeActionValue(
                    actionCounts.count(FadDeploymentAction.class),
                    attractionFields.get(FadDeploymentAction.class).getValueAt(gridLocation),
                    fadDeploymentActionValueFunction,
                    fadDeploymentActionDecayConstant
                )
            ))
            .map(this::weightedAction);

        return Streams
            .concat(
                weightedSetActions,
                weightedSearchActions,
                weightedFadDeploymentAction
            )
            .filter(entry -> entry.getKey().isPermitted())
            .filter(entry -> entry.getValue() > movingThreshold)
            .collect(toImmutableList());

    }

    private Entry<PurseSeinerAction, Double> weightedAction(PurseSeinerAction action) {
        return entry(action, action.getValue() * actionWeights.getOrDefault(action.getClass(), 0.0));
    }

    private static double computeActionValue(
        final int previousActionsHere,
        final double locationValue,
        final DoubleUnaryOperator valueFunction,
        final double decayConstant
    ) {
        final double value = valueFunction.applyAsDouble(locationValue);
        final double decay = exp(-decayConstant * previousActionsHere);
        return value * decay;
    }

    @Override public ActionResult act(
        final FishState fishState,
        final Fisher fisher,
        final Regulation regulation,
        final double hoursLeft
    ) {
        // record our visit to that tile
        getPurseSeineGear(fisher).recordVisit(fisher.getLocation().getGridLocation(), fishState.getStep());

        // Pick the potential action with the highest value or
        // get moving if there aren't any possible actions.
        final Optional<PurseSeinerAction> chosenAction =
            potentialActions.stream()
                .filter(entry -> entry.getKey().getDuration() <= hoursLeft)
                .max(comparingDouble(Entry::getValue))
                .map(Entry::getKey);
        potentialActions = ImmutableList.of();
        return chosenAction
            .map(action -> {
                actionCounts.add(action.getClass());
                return action.act(fishState, fisher, regulation, hoursLeft);
            })
            .orElse(new ActionResult(new Arriving(), 0)); // wait until tomorrow
    }

}
