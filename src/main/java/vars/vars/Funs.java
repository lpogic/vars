package vars.vars;

import suite.suite.Subject;
import suite.suite.Suite;
import suite.suite.action.Action;
import suite.suite.util.Cascade;
import vars.util.Generator;

import java.util.Objects;
import java.util.function.BiPredicate;

import static suite.suite.$uite.$;
import static suite.suite.$uite.$$;

public class Funs {

    public static MultiFun act(Subject $source, Action act, Subject $target) {
        var tokens = Generator.number().cascade();
        $source = prepareParams($source, tokens);
        return new MultiFun($source, $target, act);
    }

    public static MonoFun act(Subject $source, Action act, Target<?> target) {
        $source = prepareParams($source, Generator.number().cascade());
        return new MonoFun($source, target, act);
    }

    public static MultiFun exp(Subject $source, Exp exp, Subject $target) {
        var tokens = Generator.alpha().cascade();
        $source = prepareParams($source, tokens);
        $target = prepareParams($target, tokens);
        return new MultiFun($source, $target, exp);
    }

    public static MultiFun exp(Subject $source, String exp, Subject $target) {
        return exp($source, Exp.compile(exp), $target);
    }

    public static MonoFun exp(Subject $source, Exp exp, Target<?> target) {
        $source = prepareParams($source, Generator.alpha().cascade());
        return new MonoFun($source, target, exp);
    }

    public static MonoFun exp(Subject $source, String exp, Target<?> target) {
        return exp($source, Exp.compile(exp), target);
    }

    public static MonoFun assign(Source<?> source, Target<?> target) {
        return new MonoFun($$(source), target, Subject::at);
    }

    public static<O, I extends O> MonoFun suppressIdentity(Source<O> source, Var<I> target) {
        return new MonoFun($$(source, target.weak()), target,
                $ -> $.in().raw() == $.last().in().raw() ? $() : $.at());
    }

    public static<O, I extends O> MonoFun suppressEquality(Source<O> source, Var<I> target) {
        return new MonoFun($$(source, target.weak()), target,
                $ -> Objects.equals($.in().raw(), $.last().in().raw()) ? $() : $.at());
    }

    public static<O, I extends O> MonoFun suppress(Source<O> source, Var<I> target, BiPredicate<I, O> suppressor) {
        return new MonoFun($$(source, target.weak()), target,
                $ -> suppressor.test($.last().in().asExpected(), $.in().asExpected()) ? $() : $.at());
    }

    public static<O, I extends O> MonoFun select(Source<O> source, Var<I> target, BiPredicate<I, O> selector) {
        return new MonoFun($$(source, target.weak()), target,
                $ -> selector.test($.last().in().asExpected(), $.in().asExpected()) ? $.at() : $());
    }


    static Subject prepareParams(Subject $params, Cascade<?> tokens) {
        var $r = Suite.set();
        for(var $ : $params) {
            if($.is(Suite.Auto.class)) {
                for(var t : tokens) {
                    if($params.absent(t)) {
                        $r.put(t, $.in().raw());
                        break;
                    }
                }
            } else {
                $r.alter($);
            }
        }
        return $r;
    }
}
