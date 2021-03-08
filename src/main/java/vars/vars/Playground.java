package vars.vars;

import suite.suite.Subject;
import suite.suite.action.Action;
import suite.suite.action.Impression;
import suite.suite.action.Statement;

import java.util.function.Function;

public abstract class Playground {

    protected Monitor ground = new Monitor();

    public Fun intent(Subject $source, Action action, Subject $target) {
        return ground.intent($source, action, $target);
    }

    public Fun intent(Subject $source, Statement statement) {
        return ground.intent($source, statement);
    }

    public Fun intent(Subject $source, Impression impression) {
        return ground.intent($source, impression);
    }

    public <V> Fun intent(Subject $source, Function<Subject, V> function, Target<V> target) {
        return ground.intent($source, function, target);
    }

    public Fun instant(Subject $source, Action action, Subject $target) {
        return ground.instant($source, action, $target);
    }

    public Fun instant(Subject $source, Impression impression) {
        return ground.instant($source, impression);
    }

    public <V> Fun instant(Subject $source, Function<Subject, V> function, Target<?> target) {
        return ground.instant($source, function, target);
    }

    public void play() {
        ground.release();
    }
}
