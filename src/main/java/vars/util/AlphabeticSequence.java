package vars.util;

import suite.suite.util.Sequence;

import java.util.Iterator;

public class AlphabeticSequence implements Sequence<String> {

    @Override
    public Iterator<String> iterator() {
        return new Iterator<>() {
            char c = 'a';

            @Override
            public boolean hasNext() {
                return c <= 'z';
            }

            @Override
            public String next() {
                return "" + c++;
            }
        };
    }
}
