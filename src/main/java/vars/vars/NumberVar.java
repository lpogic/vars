package vars.vars;

import suite.suite.Subject;
import static suite.suite.$uite.*;

import java.lang.ref.WeakReference;

public final class NumberVar extends Var<Number> {

    public static NumberVar add(Object a, Object b) {
        return Vars.exp($$(a, b), Exp::sum);
    }

    public static NumberVar sum(Subject $) {
        return Vars.exp($, Exp::sum);
    }

    public static NumberVar sub(Object a, Object b) {
        return Vars.exp($$(a, b), Exp::sub);
    }

    Number value;

    public NumberVar(Object value, boolean instant) {
        super(instant);
        value(value);
    }

    @Override
    Number value() {
        return value;
    }

    void value(Object v) {
        if(v instanceof Number) this.value = (Number) v;
        else if(v instanceof Boolean) this.value = (Boolean) v ? 1 : -1;
        else this.value = null;
    }

    @Override
    public Number get() {
        inspect();
        return value;
    }

    public Number get(Fun fun) {
        return get();
    }

    @Override
    public void set(Object value) {
        value(value);
        for(var $ : $outputs) {
            WeakReference<Fun> ref = $.asExpected();
            Fun fun = ref.get();
            if(fun != null) {
                fun.press(true);
            }
        }
    }

    @Override
    public void set(Number value, Fun fun) {
        this.value = value;
        if($detections != null)$detections.unset(fun); // Jeśli wywołana w gałęzi równoległej, oznacz jako wykonana.
        for(var $ : $outputs) {
            WeakReference<Fun> ref = $.asExpected();
            Fun f = ref.get();
            if(f != null && f != fun) {
                f.press(true);
            }
        }
    }

    public byte getByte() {
        return get().byteValue();
    }

    public short getShort() {
        return get().shortValue();
    }

    public int getInt() {
        return get().intValue();
    }

    public long getLong() {
        return get().longValue();
    }

    public float getFloat() {
        return get().floatValue();
    }

    public double getDouble() {
        return get().doubleValue();
    }

    public boolean getBoolean(boolean trueOn0) {
        double d = get().doubleValue();
        return d > 0 || (d == .0 && trueOn0);
    }
}
