package kz.api.json;

/**
 * Created by baha on 6/10/15.
 */
public class ResultParameter {
    String name;
    Object value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ResultParameter{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
