package uk.ac.ox.oxfish.gui.widget;

import org.junit.Test;
import static org.junit.Assert.*;

import org.metawidget.inspector.composite.CompositeInspector;
import org.metawidget.inspector.composite.CompositeInspectorConfig;
import org.metawidget.inspector.propertytype.PropertyTypeInspector;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.FixedFavoriteDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.RandomFavoriteDestinationFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 5/29/15.
 */
public class AlgorithmFactoryInspectorTest {


    @Test
    public void readsCorrectly() throws Exception {

        //read serialize, make sure the StrategyFactoryInspector was fired
        ToSerialize serialize = new ToSerialize();
        CompositeInspectorConfig inspectorConfig = new CompositeInspectorConfig().setInspectors(
                new PropertyTypeInspector(),
                new StrategyFactoryInspector() );
        final CompositeInspector inspector = new CompositeInspector(inspectorConfig);
        //inspect!
        String attributeMap = inspector.inspect(serialize, serialize.getClass().getTypeName());
        //example of what should be in: factory_strategy="uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy"
        String lookingFor = "factory_strategy=\"" + DestinationStrategy.class.getName()+"\"";
        assertTrue(attributeMap.contains(lookingFor));

        //it should also work for serialize2 (that is, it shouldn't be fooled by the current value)
        ToSerialize2 two = new ToSerialize2();
        attributeMap = inspector.inspect(two, two.getClass().getTypeName());
        assertTrue(attributeMap.contains(lookingFor));

    }
}


/**
 * this class exists only so that we can make sure the MetaInspector reads  correctly.
 */
class ToSerialize
{
    /**
     * make sure this doesn't break anything!
     */
    int redHerring = 1;

    /**
     * a factory
     */
    AlgorithmFactory<? extends DestinationStrategy> firstFactory = new RandomFavoriteDestinationFactory();

    public int getRedHerring() {
        return redHerring;
    }

    public void setRedHerring(int redHerring) {
        this.redHerring = redHerring;
    }

    public AlgorithmFactory<? extends DestinationStrategy> getFirstFactory() {
        return firstFactory;
    }

    public void setFirstFactory(
            AlgorithmFactory<? extends DestinationStrategy> firstFactory) {
        this.firstFactory = firstFactory;
    }
}

class ToSerialize2
{
    /**
     * make sure this doesn't break anything!
     */
    int redHerring = 1;

    /**
     * a customized factory
     */
    AlgorithmFactory<? extends DestinationStrategy> firstFactory = new FixedFavoriteDestinationFactory();

    public int getRedHerring() {
        return redHerring;
    }

    public void setRedHerring(int redHerring) {
        this.redHerring = redHerring;
    }

    public AlgorithmFactory<? extends DestinationStrategy> getFirstFactory() {
        return firstFactory;
    }

    public void setFirstFactory(
            AlgorithmFactory<? extends DestinationStrategy> firstFactory) {
        this.firstFactory = firstFactory;
    }
}
