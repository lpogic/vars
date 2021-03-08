package vars.vars;

import suite.suite.util.Series;

import static suite.suite.$uite.$;

public interface Source<T> {

    class Const {}

    Const OWN_VALUE = new Const();
    Const SELF = new Const();

    T get(Fun fun);
    boolean attachOutput(Fun fun);
    void detachOutput(Fun fun);
    default Source<T> weak() {
        return this;
    }

    static Series prepareComponents(Series $components, Source<?> self) {
        return $components.convert($ -> {
            var $v = $.at();
            if($v.raw() == OWN_VALUE)
                return $($.raw(), self.weak());
            else if($v.raw() == SELF)
                return $($.raw(), new Constant<>(self));
            else return $;
        });
    }
}
