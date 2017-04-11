package amesmarket.probability;

import java.util.List;
import java.util.Random;

public class RouletteWheelSelector<T extends IChoice> extends AbstractSelector<T> {

    public RouletteWheelSelector(List<T> elements, Random rand) {
        super(elements, rand);
    }

    @Override
    public T selectElement() {
        double p = getRandom().nextDouble();
        double cumulativeP = 0.0;
        T choice = null;
        for (T item : getElements()) {
            cumulativeP += item.probability();
            if (p <= cumulativeP) {
                choice = item;
                break;
            }
        }

        if(choice == null){
            throw new IllegalArgumentException("Something went wrong. The roullette wheel failed to select anything.");
        }

        return choice;
    }

}
