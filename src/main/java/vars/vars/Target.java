package vars.vars;

public interface Target<T> {

    boolean press(Fun fun);
    void set(T value, Fun fun);
    void attachInput(Fun fun);
}
