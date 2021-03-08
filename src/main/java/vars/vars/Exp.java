package vars.vars;

import suite.suite.Subject;
import suite.suite.action.Action;
import suite.suite.util.Cascade;
import suite.suite.util.Series;

import static suite.suite.$uite.$;

public abstract class Exp implements Action {

    Subject $inputs;
    Subject $outputs;

    public Exp(Subject $inputs, Subject $outputs) {
        this.$inputs = $inputs;
        this.$outputs = $outputs;
    }

    public static Exp compile(String expressionString) {
        return new ExpressionProcessor().process(expressionString).asExpected();
    }

    public static Subject sin(Subject $) {
        return $(Math.sin($.in().asDouble()));
    }

    public static Subject cos(Subject $) {
        return $(Math.cos($.in().asDouble()));
    }

    public static Subject max(Series f) {
        Cascade<Number> c = f.eachIn().eachAs(Number.class).cascade();
        if(c.hasNext()) {
            double max = c.next().doubleValue();
            for(Number n : c.toEnd()) {
                max = Math.max(max, n.doubleValue());
            }
            return $(max);
        } else return $();
    }

    public static Subject min(Series f) {
        Cascade<Number> c = f.eachIn().eachAs(Number.class).cascade();
        if(c.hasNext()) {
            double min = c.next().doubleValue();
            for(Number n : c.toEnd()) {
                min = Math.min(min, n.doubleValue());
            }
            return $(min);
        } else return $();
    }

    public static Subject sum(Series f) {
        double sum = 0.0;
        for(Number n : f.eachIn().eachAs(Number.class)) {
            sum += n.doubleValue();
        }
        return $(sum);
    }

    public static Subject sub(Subject $) {
        return $($.in("a").asDouble() - $.in("b").asDouble());
    }

    public static Subject add(Subject $) {
        return $($.in("a").asDouble() + $.in("b").asDouble());
    }
}
