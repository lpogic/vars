package vars.vars;

public class Constant<T> implements Source<T> {

    T value;

    public Constant(T value) {
        this.value = value;
    }

    @Override
    public T get(Fun fun) {
        return value;
    }

    @Override
    public boolean attachOutput(Fun fun) {return false;}

    @Override
    public void detachOutput(Fun fun) {}

    @Override
    public String toString() {
        return "<" + value + ">";
    }
}
