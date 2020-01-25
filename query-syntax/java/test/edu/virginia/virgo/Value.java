package edu.virginia.virgo;

public class Value {

    public static Value VOID = new Value(new Object());

    final Object value;

    public Value(Object value) {
        this.value = value;
    }

    public String asString() {
        return String.valueOf(value);
    }

    public Value[] asArray() {
        return ((Value[])value);
    }

    public boolean isArray() {
        return value instanceof Value[];
    }

    @Override
    public int hashCode() {

        if(value == null) {
            return 0;
        }

        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if(value == o) {
            return true;
        }

        if(value == null || o == null || o.getClass() != value.getClass()) {
            return false;
        }

        Value that = (Value)o;

        return this.value.equals(that.value);
    }

    @Override
    public String toString() {
        if (isArray())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (Value element : this.asArray())
            {
                sb.append(element.toString());
            }
            sb.append("]");
            return(sb.toString());
        }
        else
        {
            return String.valueOf(value);
        }
    }
}