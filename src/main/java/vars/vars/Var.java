package vars.vars;

import suite.suite.Subject;
import suite.suite.action.Action;

import java.lang.ref.WeakReference;
import java.util.function.BiPredicate;

import static suite.suite.$uite.$;

public abstract class Var<T> implements Source<T>, Target<T> {

    /**
     * Leniwa implementacja (instant = false) czeka z wywołaniem funkcji do czasu użycia get().
     * Stan zmiennych położonych wyżej jest zawsze brany ostatni przed użyciem get(), a funkcje wywoływane tylko raz.
     *
     * Jeśli każdy stan zmiennych wyższych ma zostać zarejestrowany, należy użyć implementacji instant = true.
     */

    Subject $inputs = $();
    Subject $outputs = $();
    Subject $detections;

    public Var(boolean instant) {
        if(!instant) $detections = $();
    }

    void inspect() {
        if($detections != null && $detections.present()) {
            $detections.eachAs(Fun.class).forEach(Fun::execute);
            $detections = $();
        }
    }

    public abstract T get();
    public abstract void set(Object value);

    public boolean press(Fun fun) {
        if($detections == null) {
            fun.execute();
            return true;
        } else {
            boolean pressOutputs = $detections.absent();
            $detections.sate(fun);
            if(pressOutputs) {
                for(var $ : $outputs) {
                    WeakReference<Fun> ref = $.asExpected();
                    Fun f = ref.get();
                    if(f != null && f != fun && f.press(false)) return true;
                }
            }
            return false;
        }
    }

    public boolean attachOutput(Fun fun) {
        $outputs.sate(new WeakReference<>(fun));
        return $detections != null && $detections.present();
    }

    public void detachOutput(Fun fun) {
        for(var $ : $outputs) {
            WeakReference<Fun> ref = $.asExpected();
            Fun f = ref.get();
            if(f == null || f == fun) {
                $outputs.unset(ref);
            }
        }
    }

    public void attachInput(Fun fun) {
        $inputs.set(fun);
    }

    public void detach() {
        $outputs = $();
    }

    boolean cycleTest(Fun fun) {
        for(var $ : $outputs) {
            WeakReference<Fun> ref = $.asExpected();
            Fun f = ref.get();
            if(f == null){
                $outputs.unset($.raw());
            } else if(f == fun || f.cycleTest(fun)) return true;
        }
        return false;
    }

    public boolean isInstant() {
        return $detections == null;
    }

    public Var<T> select(BiPredicate<T, T> selector) {
        return suppress(selector.negate());
    }

    public Var<T> suppress(BiPredicate<T, T> suppressor) {
        Var<T> suppressed = new SimpleVar<>(get(), false);
        Funs.suppress(this, suppressed, suppressor).press();
        return suppressed;
    }

    public Var<T> suppressIdentity() {
        Var<T> suppressed = new SimpleVar<>(get(), false);
        Funs.suppressIdentity(this, suppressed).press();
        return suppressed;
    }

    public Var<T> suppressEquality() {
        Var<T> suppressed = new SimpleVar<>(get(), false);
        Funs.suppressEquality(this, suppressed).press();
        return suppressed;
    }

    public<V extends T> Fun assign(Source<V> source) {
        return Funs.assign(source, this);
    }

    public MonoFun act(Subject $source, Action act) {
        return Funs.act($source, act, this);
    }

    public MonoFun exp(Subject $source, Exp exp) {
        return Funs.exp($source, exp, this);
    }

    public MonoFun exp(Subject $source, String exp) {
        return Funs.exp($source, exp, this);
    }

    public WeakVar<T> weak() {
        return new WeakVar<>(this);
    }

    abstract T value();

    @Override
    public String toString() {
        if($detections != null && $detections.present())
            return "(" + value() + ")";
        else return "<" + value() + ">";
    }

    public String address() {
        return super.toString();
    }

    public Subject getInputs() {
        return $inputs;
    }
}
