package amesmarket.probability;

import java.util.List;

/**
 * Always choose the same element
 * @author sean
 *
 * @param <T>
 */
public class DeterministicSelector<T extends IChoice> extends AbstractSelector<T> {

    int idx = 0;
    /**
     * @param elements
     * @param idx index in the list to choose;
     */
    public DeterministicSelector(List<T> elements, int idx) {
        super(elements, null);
        this.idx = idx;
    }

    @Override
    public T selectElement() {
        return getElements().get(idx);
    }

}
