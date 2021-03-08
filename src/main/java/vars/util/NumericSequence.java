package vars.util;

import suite.suite.util.Sequence;

import java.util.Iterator;

public class NumericSequence implements Sequence<Integer> {

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            int n = 0;

            @Override
            public boolean hasNext() {
                return n < Integer.MAX_VALUE;
            }

            @Override
            public Integer next() {
                return n++;
            }
        };
    }
}
