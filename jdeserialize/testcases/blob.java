import java.io.Serializable;

public class blob implements Serializable {
    private final int a;
    private final String b;
    private final inner i;
    public blob(int a, String b) {
        this.a = a;
        this.b = b;
        this.i = new inner(a + 1, b);
    }

    public String toString() {
        return "[blob a " + a + "  b " + b + "  i " + i.toString() + "]";
    }

    public class inner implements Serializable {
        private final int ia;
        private final String ib;

        public inner(int ia, String ib) {
            this.ia = ia;
            this.ib = ib;
        }

        public String toString() {
            return "[inner ia " + ia + "  ib " + ib + "]";
        }
    }
}
