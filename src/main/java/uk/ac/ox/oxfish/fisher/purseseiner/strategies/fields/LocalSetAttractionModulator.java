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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogisticFunction;

import java.util.function.DoubleUnaryOperator;

import static uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear.getPurseSeineGear;

public class LocalSetAttractionModulator implements LocalAttractionModulator {

    private final DoubleUnaryOperator timeSinceLastVisitModulationFunction;

    public LocalSetAttractionModulator(
        final double timeSinceLastVisitLogisticMidpoint,
        final double timeSinceLastVisitLogisticSteepness
    ) {
        this(
            new LogisticFunction(timeSinceLastVisitLogisticMidpoint, timeSinceLastVisitLogisticSteepness)
        );
    }

    public LocalSetAttractionModulator(
        final DoubleUnaryOperator timeSinceLastVisitModulationFunction
    ) {
        this.timeSinceLastVisitModulationFunction = timeSinceLastVisitModulationFunction;
    }

    @Override
    public double modulate(
        final int x,
        final int y,
        final int t,
        final Fisher fisher
    ) {
        if (!canFishThere(x, y, t, fisher))
            return 0;
        else {
            return getPurseSeineGear(fisher)
                .getLastVisit(fisher.getLocation().getGridLocation())
                .map(lastVisit -> timeSinceLastVisitModulationFunction.applyAsDouble(t - lastVisit))
                .orElse(1.0);
        }
    }


}
