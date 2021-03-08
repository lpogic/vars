package vars.vars;

import suite.suite.Subject;
import suite.suite.action.Action;
import suite.suite.util.Series;

import java.lang.ref.WeakReference;

import static suite.suite.$uite.$;


public class MonoFun implements Fun{

    Subject $source;
    WeakReference<Target<?>> target;
    Action transition;
    boolean detection;

    public MonoFun(Series $source, Target<?> target, Action transition) {
        var $press = $(false);
        this.$source = $source.convert($ -> {
            var $v = $.at();
            Source<?> vp;
            if($v.is(Source.class)) {
                vp = $v.asExpected();
            } else if($v.raw() == SELF) {
                vp = new Constant<>(this);
            } else {
                vp = new Constant<>($v.raw());
            }
            if(vp.attachOutput(this)) $press.reset(true);
            return $($.raw(), vp);
        }).set();
        if(target instanceof Var && ((Var<?>) target).cycleTest(this))
            throw new RuntimeException("Illegal cycle detected");
        target.attachInput(this);
        this.target = new WeakReference<>(target);
        this.transition = transition;
        if($press.asBoolean()) press(false);
    }

    public void execute() {
        var $in = $source.convert($ -> $($.raw(), $.in().as(Source.class).get(this))).set();
        if(detection) {
            detection = false;
            var $out = transition.play($in);
            if($out.present()) {
                Target<?> i = target.get();
                if (i != null) {
                    i.set($out.asExpected(), this);
                }
            }
        }
    }

    @Override
    public MonoFun press() {
        press(true);
        return this;
    }

    public boolean press(boolean direct) {
        if(detection) return false;
        if(direct)detection = true;
        Target<?> i = target.get();
        return i != null && i.press(this);
    }

    public void attach() {
        attach(false);
    }

    public void attach(boolean forcePress) {
        boolean press = false;
        for (Source<?> vp : $source.eachIn().eachAs(Source.class)) {
            if (vp.attachOutput(this)) press = true;
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

    public MonoFun reduce(boolean execute) {
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
        Target<?> i = target.get();
        return i instanceof Var && ((Var<?>) i).cycleTest(fun);
    }

    @Override
    public boolean isDetection() {
        return detection;
    }
}
