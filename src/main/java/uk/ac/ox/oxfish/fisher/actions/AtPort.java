package uk.ac.ox.oxfish.fisher.actions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * This is the starting state and is in general the state the fisher stays at while at port
 * Created by carrknight on 4/18/15.
 */
public class AtPort implements Action {

    /**
     * Asks the fisher if they want to move, otherwise stay at port.
     *
     * @param model a link to the model, in case you need to grab global objects
     * @param agent a link to the fisher in case you need to get or set agent's variables
     * @param regulation the regulation that tells us whether we can leave
     * @return the next action to take and whether or not to take it now
     */
    @Override
    public ActionResult act(FishState model, Fisher agent, Regulation regulation, double hoursLeft)
    {

        if(regulation.allowedAtSea(agent, model)
                &&
                agent.shouldFisherLeavePort(model))
        {
            agent.updateDestination(model,this);
            assert !agent.getDestination().equals(agent.getHomePort().getLocation()); //shouldn't have chosen to go to port because that's weird
            agent.undock();
            return new ActionResult(new Moving(),hoursLeft);
        }
        else //you don't want to leave this hour, try again next hour
            return new ActionResult(this,Math.max(0,hoursLeft-1));


    }
}
