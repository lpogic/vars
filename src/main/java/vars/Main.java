package vars;

import vars.vars.*;

import static suite.suite.$uite.*;

public class Main {

    public static void main(String[] args) {
        var v1 = Vars.number(3);
        var v2 = Vars.number(0);
        var v3 = Vars.act($$(v1), $ -> $($.in(0).asInt() + 4)); //Vars.exp($$(v1), "sum(a,a,a + 20)");
//        MultiFun.exp($$(v1), "b = a; c = a + 4", $$(v2, v3));//.press();

        System.out.println(v3.get());
        System.out.println(v2.get());
        v1.set(-8);
        System.out.println(v3.get());
    }

}
