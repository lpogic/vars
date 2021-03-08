package vars.vars;

public interface Fun {

    class Const {}
    Const SELF = new Const();

    void execute();
    Fun press();
    boolean press(boolean direct);
    void attach();
    void attach(boolean forcePress);
    void detach();
    void detach(Object key);
    Fun reduce(boolean execute);
    boolean cycleTest(Fun fun);
    boolean isDetection();
}
