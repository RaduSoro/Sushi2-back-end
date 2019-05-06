package comp1206.sushi.common;

import java.io.Serializable;

public class ComplexMessage implements Serializable {
    private Object object;
    private String instruction;

    public ComplexMessage(Object object, String instruction) {
        this.object = object;
        this.instruction = instruction;
    }

    public Object getObject() {
        return this.object;
    }

    public String getInstruction() {
        return this.instruction;
    }
}
