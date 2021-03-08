package vars.vars;

import suite.suite.Subject;
import suite.suite.Suite;
import suite.suite.action.Action;
import suite.suite.action.Impression;
import suite.suite.util.Series;

import java.lang.ref.WeakReference;
import java.util.function.Function;

import static suite.suite.$uite.$;

public class Monitor implements Target<Object>, Source<Boolean> {

    Subject $instantInputs = $();
    Subject $intentInputs = $();
    Subject $outputs = $();
    Subject $detections = $();

    public static Monitor compose(boolean pressed, Subject $source, Impression impression) {
        Monitor monitor = new Monitor();
        Fun fun = Funs.act($source, impression, monitor);
        if(pressed) fun.press(true);
        return monitor;
    }

    public static Monitor compose(boolean pressed, Subject $components) {
        return Monitor.compose(pressed, $components, $ -> {});
    }

    public Fun intent(Subject $source, Action action, Subject $target) {
        return Funs.act($source, action, $target.add(this));
    }

    public Fun intent(Subject $source, Impression impression) {
        return Funs.act($source, impression, this);
    }

    public <V> Fun intent(Subject $source, Function<Subject, V> function, Target<V> target) {
        return Funs.act($source, $ -> $(Var.OWN_VALUE, function.apply($)), $(Var.OWN_VALUE, target).add(this));
    }

    public Fun instant(Subject $source, Action action, Subject $target) {
        Fun fun = Funs.act($source, action, $target.add(this));
        attachInstant(fun);
        return fun;
    }

    public Fun instant(Subject $source, Impression impression) {
        Fun fun = Funs.act($source, impression, this);
        attachInstant(fun);
        return fun;
    }

    public <V> Fun instant(Subject $source, Function<Subject, V> function, Target<?> target) {
        Fun fun = Funs.act($source, $ -> $(Var.OWN_VALUE, function.apply($)), $(Var.OWN_VALUE, target).add(this));
        attachInstant(fun);
        return fun;
    }

    public boolean release() {
        if ($detections.present()) {
            $detections.eachAs(Fun.class).forEach(Fun::execute);
            $detections = $();
            return true;
        }
        return false;
    }

    public Boolean get() {
        return release();
    }

    public Boolean get(Fun fun) {
        return release();
    }

    @Override
    public boolean press(Fun fun) {
        if($instantInputs.present(fun)) {
            fun.execute();
            return true;
        } else {
            boolean pressOutputs = $detections.absent();
            $detections.sate(fun);
            if(pressOutputs) {
                for(var $ : $outputs.eachIn()) {
                    WeakReference<Fun> ref = $.asExpected();
                    Fun f = ref.get();
                    if(f != null && f != fun && f.press(false)) return true;
                }
            }
            return false;
        }
    }

    @Override
    public void set(Object value, Fun fun) {
        $detections.unset(fun);
    }

    public void attachInstant(Fun fun) {
        $detections.unset(fun);
        $intentInputs.unset(fun);
        $instantInputs.set(fun);
        if(fun.isDetection())fun.execute();
    }

    @Override
    public void attachInput(Fun fun) {
        if($instantInputs.absent(fun)) $intentInputs.set(fun);
    }

    public void detach(Fun fun) {
        $intentInputs.unset(fun);
        $instantInputs.unset(fun);
        $detections.unset(fun);
    }

    @Override
    public boolean attachOutput(Fun fun) {
        $outputs.sate(new WeakReference<>(fun));
        return $detections.present();
    }

    @Override
    public void detachOutput(Fun fun) {
        for(var $ : $outputs.eachIn()) {
            WeakReference<Fun> ref = $.asExpected();
            Fun f = ref.get();
            if(f == null || f == fun) {
                $outputs.unset(ref);
            }
        }
    }

    public Subject getDetections() {
        return $detections;
    }
}
