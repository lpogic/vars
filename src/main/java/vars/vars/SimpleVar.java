package vars.vars;

import java.lang.ref.WeakReference;

public final class SimpleVar<T> extends Var<T> {

    T value;

    public SimpleVar(T value, boolean instant) {
        super(instant);
        this.value = value;
    }

    @Override
    T value() {
        return value;
    }

    @Override
    public T get() {
        inspect();
        return value;
    }

    public T get(Fun fun) {
        return get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void set(Object value) {
        this.value = (T)value;
        for(var $ : $outputs) {
            WeakReference<Fun> ref = $.asExpected();
            Fun fun = ref.get();
            if(fun != null) {
                fun.press(true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void set(Object value, Fun fun) {
        this.value = (T)value;
        if($detections != null) $detections.unset(fun); // Jeśli wywołana w gałęzi równoległej, oznacz jako wykonana.
        for(var $ : $outputs) {
            WeakReference<Fun> ref = $.asExpected();
            Fun f = ref.get();
            if(f != null && f != fun) {
                f.press(true);
            }
        }
    }
}
