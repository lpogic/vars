package vars.vars;

import suite.suite.Subject;
import suite.suite.Suite;
import suite.suite.action.Action;
import suite.suite.util.Cascade;
import vars.util.Generator;

public class Vars {

    public static<T> Var<T> get(T t) {
        return new SimpleVar<>(t, false);
    }

    public static<T> Var<T> get(T t, boolean instant) {
        return new SimpleVar<>(t, instant);
    }

    public static NumberVar number(Number n) {
        return new NumberVar(n, false);
    }

    public static NumberVar number(Number n, boolean instant) {
        return new NumberVar(n, instant);
    }

    public static<T> Var<T> act(Subject $, Action a) {
        Var<T> v = new SimpleVar<>(null, false);
        $ = Funs.prepareParams($, Generator.number().cascade());
        new MonoFun(Source.prepareComponents($, v), v, a).press();
        return v;
    }

    public static NumberVar exp(Subject $params, String e) {
        NumberVar numberVar = new NumberVar(null, false);
        $params = Funs.prepareParams($params, Generator.alpha().cascade());
        new MonoFun(Source.prepareComponents($params, numberVar), numberVar, Exp.compile(e)).press();
        return numberVar;
    }

    public static NumberVar exp(Subject $params, Action e) {
        NumberVar numberVar = new NumberVar(null, false);
        $params = Funs.prepareParams($params, Generator.alpha().cascade());
        new MonoFun(Source.prepareComponents($params, numberVar), numberVar, e).press();
        return numberVar;
    }
}
