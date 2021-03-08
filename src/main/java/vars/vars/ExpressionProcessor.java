package vars.vars;

import suite.processor.IntProcessor;
import suite.suite.Subject;
import suite.suite.Suite;
import suite.suite.action.Action;
import suite.suite.action.Impression;

import static suite.suite.$uite.$;
import static suite.suite.$uite.join$;

public class ExpressionProcessor implements IntProcessor {

    enum State {
        PENDING, NUMBER, SYMBOL
    }

    static abstract class ActionProfile {
        Action action;
        String name;

        public ActionProfile(Impression impression, String name) {
            this.action = impression;
            this.name = name;
        }

        public ActionProfile(Action action, String name) {
            this.action = action;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public abstract boolean pushes(ActionProfile that);
    }

    static class PrefixActionProfile extends ActionProfile {

        public PrefixActionProfile(Impression impression, String name) {
            super(impression, name);
        }

        public PrefixActionProfile(Action action, String name) {
            super(action, name);
        }

        @Override
        public boolean pushes(ActionProfile that) {
            return false;
        }
    }

    static class InfixActionProfile extends ActionProfile {
        final int priority;

        public InfixActionProfile(int priority, Impression impression, String name) {
            super(impression, name);
            this.priority = priority;
        }

        public InfixActionProfile(int priority, Action action, String name) {
            super(action, name);
            this.priority = priority;
        }

        public boolean pushes(ActionProfile that) {
            if(that instanceof InfixActionProfile)return priority <= ((InfixActionProfile) that).priority;
            return true;
        }
    }

    static class PostfixActionProfile extends ActionProfile {

        public PostfixActionProfile(Impression impression, String name) {
            super(impression, name);
        }

        public PostfixActionProfile(Action action, String name) {
            super(action, name);
        }

        public boolean pushes(ActionProfile that) {
            return !(that instanceof InfixActionProfile);
        }
    }

    static class FunctionProfile extends ActionProfile {

        public FunctionProfile(String name) {
            super(null, name);
        }

        public FunctionProfile(Action action, String name) {
            super(action, name);
        }

        public boolean pushes(ActionProfile that) {
            return false;
        }
    }

    static class VarNumber extends Number {
        final String symbol;
        Number value;

        public VarNumber(String symbol) {
            this.symbol = symbol;
        }

        void setValue(Number value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return value.intValue();
        }

        @Override
        public long longValue() {
            return value.longValue();
        }

        @Override
        public float floatValue() {
            return value.floatValue();
        }

        @Override
        public double doubleValue() {
            return value.doubleValue();
        }

        @Override
        public String toString() {
            return symbol + " = " + value;
        }
    }

    enum SpecialSymbol {
        OPEN_BRACKET, CLOSE_BRACKET, SPLINE
    }

    private static final ActionProfile attribution = new InfixActionProfile(0, ExpressionProcessor::attribution, "=");
    private static final ActionProfile postAttribution = new InfixActionProfile(0, ExpressionProcessor::postAttribution, "->");
    private static final ActionProfile maximum = new InfixActionProfile(1, ExpressionProcessor::maximum, "|");
    private static final ActionProfile minimum = new InfixActionProfile(1, ExpressionProcessor::minimum, "&");
    private static final ActionProfile addition = new InfixActionProfile(2, ExpressionProcessor::addition, "+");
    private static final ActionProfile subtraction = new InfixActionProfile(2, ExpressionProcessor::subtraction, "-");
    private static final ActionProfile multiplication = new InfixActionProfile(3, ExpressionProcessor::multiplication, "*");
    private static final ActionProfile division = new InfixActionProfile(3, ExpressionProcessor::division, "/");
    private static final ActionProfile exponentiation = new InfixActionProfile(4, ExpressionProcessor::exponentiation, "^");
    private static final ActionProfile reversion = new PrefixActionProfile(ExpressionProcessor::reversion, "0-");
    private static final ActionProfile inversion = new PrefixActionProfile(ExpressionProcessor::inversion, "1/");
    private static final ActionProfile proportion = new PostfixActionProfile(ExpressionProcessor::proportion, "%");
    private static final ActionProfile absolution = new PrefixActionProfile(ExpressionProcessor::absolution, "||");

    private static final Subject descriptiveActionProfiles = join$(
            $("sin", (Action)Exp::sin),
            $("cos", (Action)Exp::cos),
            $("max", (Action)Exp::max),
            $("min", (Action)Exp::min),
            $("sum", (Action)Exp::sum)
    );


    private Subject $functions;
    private Subject $inputs;
    private Subject $outputs;

    private Subject $actions;
    private StringBuilder builder;
    private State state;
    private Subject $rpn;
    private boolean emptyValueBuffer;

    @Override
    public void getReady() {
        $inputs = $();
        $functions = $();
        $outputs = $();
        $rpn = $();
        $actions = $();
        state = State.PENDING;
        emptyValueBuffer = true;
    }

    private void pushAction(ActionProfile profile) {
        for(var $ : $actions.reverse()) {
            var $i = $.at();
            if($i.is(ActionProfile.class)) {
                ActionProfile actionProfile = $i.asExpected();
                if(profile.pushes(actionProfile)) {
                    $rpn.add(actionProfile);
                    $actions.unset($.raw());
                } else break;
            } else if($i.raw() == SpecialSymbol.SPLINE) {
                break;
            } else {
                $rpn.add($i.raw());
                $actions.unset($.raw());
            }
        }
        $actions.add(profile);
        emptyValueBuffer = true;
    }

    @Override
    public void advance(int i) {
        switch (state) {
            case PENDING:
                if(Character.isDigit(i)) {
                    builder = new StringBuilder();
                    builder.appendCodePoint(i);
                    state = State.NUMBER;
                } else if(Character.isJavaIdentifierStart(i)) {
                    builder = new StringBuilder();
                    builder.appendCodePoint(i);
                    state = State.SYMBOL;
                } else if(i == '+') {
                    pushAction(emptyValueBuffer ? absolution : addition);
                } else if(i == '-') {
                    pushAction(emptyValueBuffer ? reversion : subtraction);
                } else if(i == '*') {
                    if(!emptyValueBuffer) pushAction(multiplication);
                } else if(i == '/') {
                    pushAction(emptyValueBuffer ? inversion : division);
                } else if(i == '^') {
                    pushAction(exponentiation);
                } else if(i == '%') {
                    $rpn.add(proportion);
                } else if(i == '&') {
                    pushAction(minimum);
                } else if(i == '|') {
                    pushAction(maximum);
                } else if(i == '=') {
                    VarNumber var = $rpn.last().in().asExpected();
                    $outputs.put(var.symbol, var);
                    if(emptyValueBuffer) $rpn.add(var);
                    pushAction(attribution);
                } else if(i == '(') {
                    $actions.add(SpecialSymbol.SPLINE);
                    $rpn.add(SpecialSymbol.OPEN_BRACKET);
                    emptyValueBuffer = true;
                } else if(i == ')') {
                    for(var $ : $actions.reverse()) {
                        $actions.unset($.raw());
                        if($.in().raw() == SpecialSymbol.SPLINE) {
                            break;
                        } else {
                            $rpn.add($.in().raw());
                        }
                    }
                    $rpn.add(SpecialSymbol.CLOSE_BRACKET);
                } else if(i == ',') {
                    for(var $ : $actions.reverse()) {
                        if($.in().raw() == SpecialSymbol.SPLINE) {
                            break;
                        } else {
                            $rpn.add($.in().raw());
                            $actions.unset($.raw());
                        }
                    }
                    emptyValueBuffer = true;
                } else if(i == ';') {
                    int brackets = 0;
                    for(var $ : $rpn.reverse()) {
                        if($.in().raw() == SpecialSymbol.OPEN_BRACKET) {
                            if (--brackets < 0) break;
                        } else if($.in().raw() == SpecialSymbol.CLOSE_BRACKET) {
                            ++brackets;
                        }
                        $actions.add($.in().raw());
                        $rpn.unset($.raw());
                    }
                    $actions.add(SpecialSymbol.SPLINE);
                    emptyValueBuffer = true;
                }/* else if(i == '`') {
                    builder = new StringBuilder();
                    state = State.SYMBOL;
                }*/ else if(!Character.isWhitespace(i)) {
                    throw new RuntimeException();
                }
                break;
            case NUMBER:
                if(Character.isDigit(i) || i == '.') {
                    builder.appendCodePoint(i);
                } else if(!Character.isWhitespace(i)) {
                    try {
                        double d = Double.parseDouble(builder.toString());
                        $rpn.add(d);
                        state = State.PENDING;
                        emptyValueBuffer = false;
                        advance(i);
                    } catch (NumberFormatException nfe) {
                        throw new RuntimeException(nfe);
                    }
                }
                break;
            case SYMBOL:
                if(Character.isJavaIdentifierPart(i)) {
                    builder.appendCodePoint(i);
                } else if(i == '(') {
                    String str = builder.toString();
                    pushAction($functions.sate(str, Suite.set(new FunctionProfile(str + "()"))).in().asExpected());
                    $actions.add(SpecialSymbol.SPLINE);
                    $rpn.add(SpecialSymbol.OPEN_BRACKET);
                    state = State.PENDING;
                } else if(!Character.isWhitespace(i)) {
                    VarNumber var = new VarNumber(builder.toString());
                    Subject $in = $inputs.get(var.symbol);
                    if($in.present()) var = $in.in().asExpected();
                    else if(i != '=') $inputs.put(var.symbol, var);
                    $rpn.add(var);
                    emptyValueBuffer = false;
                    state = State.PENDING;
                    advance(i);
                }
                break;
        }
    }

    @Override
    public Subject finish() {
        switch (state) {
            case NUMBER -> {
                try {
                    double d = Double.parseDouble(builder.toString());
                    $rpn.add(d);
                } catch (NumberFormatException nfe) {
                    throw new RuntimeException(nfe);
                }
            }
            case SYMBOL -> {
                VarNumber var = new VarNumber(builder.toString());
                var = $inputs.sate(var.symbol, Suite.set(var)).in().asExpected();
                $rpn.add(var);
            }
        }
        for(var $ : $actions.reverse().eachIn()) {
            if($.raw() != SpecialSymbol.SPLINE) {
                $rpn.add($.raw());
            }
        }
        return Suite.set(new Exp($inputs, $outputs) {
            @Override
            public Subject play(Subject $subject) {
                for (var v : ExpressionProcessor.this.$inputs.eachIn().eachAs(VarNumber.class)) {
                    Object o = $subject.in(v.symbol).orGiven(null);
                    if(o instanceof Number) v.value = (Number)o;
                    else if(o instanceof Boolean) v.value = (Boolean) o ? 1 : -1;
                }
                for (var $f : $functions) {
                    String funName = $f.asString();
                    Subject $ = $subject.in(funName).get();
                    if ($.present()) $f.in().as(FunctionProfile.class).action = $.asExpected();
                    else {
                        $ = descriptiveActionProfiles.in(funName).get();
                        if ($.present()) $f.in().as(FunctionProfile.class).action = $.asExpected();
                        else throw new RuntimeException("Function '" + funName + "' is not defined");
                    }
                }
                Subject $bracketStack = $();
                Subject $result = $();
                for (var $ : $rpn.eachIn()) {
                    if ($.is(Number.class)) {
                        $result.add($.raw());
                    } else if ($.is(ActionProfile.class)) {
                        if ($.is(FunctionProfile.class)) {
                            Subject $p = $();
                            for (var $1 : $result.reverse()) {
                                if ($1.in().raw() == $bracketStack.last().in().raw()) {
                                    $bracketStack.unset($bracketStack.last().raw());
                                    break;
                                }
                                $result.unset($1.raw());
                                $p.aimedAdd($p.raw(), $1.in().raw());
                            }
                            $p = $.as(FunctionProfile.class).action.play($p);
                            $result.addEntire($p.eachRaw());
                        } else {
                            $.as(ActionProfile.class).action.play($result);
                        }
                    } else if($.raw() == SpecialSymbol.OPEN_BRACKET){
                        $bracketStack.alter($result.last());
                    }
                }
                if(ExpressionProcessor.this.$outputs.present()) {

                    return ExpressionProcessor.this.$outputs.convert($ -> $($.raw(), $.in().as(VarNumber.class).doubleValue())).set();
                } else {
                    return $result.at();
                }
            }
        });
    }

    protected static void addition(Subject $) {
        double a = $.take($.last().raw()).at().asDouble();
        double b = $.take($.last().raw()).at().asDouble();
        $.add(b + a);
    }

    protected static void subtraction(Subject $) {
        double a = $.take($.last().raw()).at().asDouble();
        double b = $.take($.last().raw()).at().asDouble();
        $.add(b - a);
    }

    protected static void multiplication(Subject $) {
        double a = $.take($.last().raw()).at().asDouble();
        double b = $.take($.last().raw()).at().asDouble();
        $.add(b * a);
    }

    protected static void division(Subject $) {
        double a = $.take($.last().raw()).at().asDouble();
        double b = $.take($.last().raw()).at().asDouble();
        $.add(b / a);
    }

    protected static void exponentiation(Subject $) {
        double a = $.take($.last().raw()).at().asDouble();
        double b = $.take($.last().raw()).at().asDouble();
        $.add(Math.pow(b, a));
    }

    protected static void proportion(Subject $) {
        double a = $.take($.last().raw()).at().asDouble();
        $.add(a / 100.0);
    }

    protected static void attribution(Subject $) {
        double a = $.take($.last().raw()).at().asDouble();
        VarNumber var = $.last().at().asExpected();
        var.value = a;
    }

    protected static void postAttribution(Subject $) {
        VarNumber var = $.take($.last().raw()).at().asExpected();
        var.value = $.take($.last().raw()).at().asDouble();
    }

    protected static void inversion(Subject $) {
        double a = $.take($.last().raw()).at().asDouble();
        $.add(1 / a);
    }

    protected static void reversion(Subject $) {
        double a = $.take($.last().raw()).at().asDouble();
        $.add(-a);
    }

    protected static void absolution(Subject $) {
        double a = $.take($.last().raw()).at().asDouble();
        $.add(Math.abs(a));
    }

    protected static void maximum(Subject $) {
        double a = $.take($.last().raw()).at().asDouble();
        double b = $.take($.last().raw()).at().asDouble();
        $.add(Math.max(a, b));
    }

    protected static void minimum(Subject $) {
        double a = $.take($.last().raw()).at().asDouble();
        double b = $.take($.last().raw()).at().asDouble();
        $.add(Math.min(a, b));
    }
}
