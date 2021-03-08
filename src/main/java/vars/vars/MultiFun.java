package vars.vars;

import suite.suite.Subject;
import suite.suite.action.Action;
import suite.suite.util.Series;
import vars.util.Generator;

import java.lang.ref.WeakReference;

import static suite.suite.$uite.$;


public class MultiFun implements Fun{

    public static MultiFun compose(Series $source, Series $target, Action transition) {
        MultiFun fun = new MultiFun($source, $target, transition);
        fun.attach();
        if(fun.detection) fun.press(false);
        return fun;
    }

    public static MultiFun exp(Subject $source, Exp exp, Subject $target) {
        var tokens = Generator.alpha().cascade();
        $source = Vars.prepareParams($source, tokens);
        $target = Vars.prepareParams($target, tokens);
        MultiFun fun = new MultiFun($source, $target, exp);
        if(fun.detection) fun.press(false);
        return fun;
    }

    public static MultiFun exp(Subject $source, String exp, Subject $target) {
        return exp($source, Exp.compile(exp), $target);
    }

    Subject $source;
    Subject $target;
    Action transition;
    boolean detection;

    public MultiFun(Series $source, Series $target, Action transition) {
        var $press = $(false);
        this.$source = $source.convert($ -> {
            var $v = $.at();
            Source<?> o;
            if($v.is(Source.class)) {
                o = $v.asExpected();
            } else if($v.raw() == SELF) {
                o = new Constant<>(this);
            } else {
                o = new Constant<>($v.raw());
            }
            if(o.attachOutput(this)) $press.reset(true);
            return $($.raw(), o);
        }).set();
        this.$target = $();
        for(var $ : $target) {
            var $v = $.at();
            if($v.is(Target.class)) {
                Target<?> i = $v.asExpected();
                if(i instanceof Var && ((Var<?>) i).cycleTest(this))
                    throw new RuntimeException("Illegal cycle detected");
                i.attachInput(this);
                this.$target.put($.raw(), new WeakReference<>(i));
            }
        }
        this.transition = transition;
        if($press.asBoolean()) press(false);
    }

    public void execute() {
        var $in = $source.convert($ -> $($.raw(), $.in().as(Source.class).get(this))).set();
        if(detection) {
            detection = false;
            var $out = transition.play($in);
            $out.forEach($ -> {
                var key = $.raw();
                var $output = $target.in(key).get();
                if ($output.present()) {
                    WeakReference<Target<?>> ref = $output.asExpected();
                    Target<?> i = ref.get();
                    if(i != null) {
                        i.set($.in().asExpected(), this);
                    }
                }
            });
        }
    }

    @Override
    public MultiFun press() {
        press(true);
        return this;
    }

    public boolean press(boolean direct) {
        if(detection) return false;
        if(direct)detection = true;
        for(var $ : $target.eachIn()) {
            WeakReference<Target<?>> ref = $.asExpected();
            Target<?> var = ref.get();
            if(var != null && var.press(this)) return true;
        }
        return false;
    }

    public void attach() {
        attach(false);
    }

    public void attach(boolean forcePress) {
        boolean press = false;
        for(Source<?> vp : $source.eachIn().eachAs(Source.class)) {
            if(vp.attachOutput(this)) press = true;
        }
        if(forcePress) press(true);
        else if(press) press(false);
    }

    public void detach() {
        $source.eachRaw().forEach(this::detach);
    }

    public void detach(Object key) {
        var $ = $source.get(key);
        if($.present()) {
            Source<?> var = $.in().asExpected();
            var.detachOutput(this);
        }
    }

    public MultiFun reduce(boolean execute) {
        boolean allConstants = true;
        for(Object o : $source.eachIn().eachRaw()) {
            if(!(o instanceof Constant)) {
                allConstants = false;
                break;
            }
        }
        if(execute){
            detection = true;
            execute();
        }
        if(allConstants)detach();
        return this;
    }

    public boolean cycleTest(Fun fun) {
        for(var $ : $target) {
            WeakReference<Target<?>> ref = $.in().asExpected();
            Target<?> v = ref.get();
            if(v == null){
                $target.unset($.raw()); // clear unused output
            } else if(v instanceof Var && ((Var<?>) v).cycleTest(fun)) return true;
        }
        return false;
    }

    @Override
    public boolean isDetection() {
        return detection;
    }
}
