import java.io.Serializable;

public class blob2 implements Serializable {
    private final int a;
    private final String b;
    private final inner i;
    private final int[] ai;
    public blob2(int a, String b) {
        this.a = a;
        this.b = b;
        this.i = new inner(a + 1, b);
        this.ai = new int[]{a + 1, a + 2, a + 3, a + 4, a + 5,};
    }

    public String toString() {
        return "[blob2 a " + a + "  b " + b + "  i " + i.toString() + "]";
    }

    public class inner implements Serializable {
        private final int ia;
        private final String ib;
        private final inner2 ii;

        public inner(int ia, String ib) {
            this.ia = ia;
            this.ib = ib;
            this.ii = new inner2(ia + 1, ib);
        }

        public String toString() {
            return "[inner ia " + ia + "  ib " + ib + "  ii " + ii.toString() + "]";
        }

        public class inneri implements Serializable {
        }

        public class inner2 extends inneri implements Serializable {
            private final int ia2;
            private final String ib2;

            public inner2(int ia2, String ib2) {
                super();
                this.ia2 = ia2;
                this.ib2 = ib2;
            }

            public String toString() {
                return "[inner2 a: " + a + " ia2 " + ia2 + "  ib2 " + ib2 + "]";
            }
        }
    }
}
