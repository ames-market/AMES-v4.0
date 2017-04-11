//FIXME Licenece
package amesmarket.probability;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Sean L. Mooney
 *
 * @param <T>
 */
public abstract class AbstractSelector<T extends IChoice> {

    private final ArrayList<T> elements;
    private final Random rand;

    public AbstractSelector(List<T> elements, Random rand){
        //TODO-X check elements
        this.elements = new ArrayList<T>(elements);
        this.rand = rand;
    }

    public Random getRandom() {
        return rand;
    }

    public List<T> getElements() {
        return elements;
    }

    public abstract T selectElement();

}
