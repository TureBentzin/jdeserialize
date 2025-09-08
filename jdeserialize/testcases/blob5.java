import java.io.Serializable;

@blobann(id = 0, sfoo = "blob5ann", cl = String.class)
public class blob5 implements Serializable {
    private final int a;
    private final String b;
    private final inner i;
    private final int[] ai;
    private final istatic ist;
    public blob5(int a) {
        String b = "zoo";
        this.a = a;
        this.b = b;
        this.ist = new istatic(a, b);
        this.i = new inner(a + 1, b);
        this.ai = new int[]{a + 1, a + 2, a + 3, a + 4, a + 5,};
    }

    public String toString() {
        return "[blob5 a " + a + "  b " + b + "  i " + i.toString() + "]";
    }

    public inner getinner() {
        return i;
    }

    public istatic getist() {
        return ist;
    }

    @blobann(id = 5, sfoo = "sSsfoo", cl = Integer.class)
    public String someStuff() {
        return "someStuff: " + b;
    }

    public static class istatic implements Serializable {
        @blobann(id = 0, sfoo = "isa", cl = Integer.class)
        private final int isa;
        private final String isb;

        public istatic(int isa, String isb) {
            this.isa = isa;
            this.isb = isb;
        }
    }

    @blobann(id = 1, sfoo = "inner", cl = Double.class)
    private class inner implements Serializable {
        public inneri zooi;
        public inneri yooi;
        private final int ia;
        private final String ib;
        @blobann(id = 11, sfoo = "ii", cl = inner2.class)
        private final inner2 ii;

        public inner(int ia, String ib) {
            this.ia = ia;
            this.ib = ib;
            this.ii = new inner2(ia + 1, ib);
            class woopinneri extends inneri {
                final int x = 5;

                void duh() {
                    super.iii = 3;
                }
            }
            this.zooi = new woopinneri();
            this.yooi = new inneri() {
                final int yx = 6;

                void yduh() {
                    super.iii = 4;
                }
            };
        }

        public String toString() {
            return "[inner ia " + ia + "  ib " + ib + "  ii " + ii.toString() + "]";
        }

        public class inneri implements Serializable {
            private int iii;
        }

        @blobann(id = 2, sfoo = "inner2", cl = Object.class)
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
