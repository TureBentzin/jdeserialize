package bluh.zuh;

import java.io.Serializable;


public class blob4 implements Serializable {
    private final int a;
    private final String b;
    private final inner i;
    private final int[] ai;
    private final istatic ist;
    public blob4(int a) {
        String b = "zoo";
        this.a = a;
        this.b = b;
        this.ist = new istatic(a, b);
        this.i = new inner(a + 1, b);
        this.ai = new int[]{a + 1, a + 2, a + 3, a + 4, a + 5,};
    }

    public String toString() {
        return "[blob4 a " + a + "  b " + b + "  i " + i.toString() + "]";
    }

    public static class istatic implements Serializable {
        private final int isa;
        private final String isb;

        public istatic(int isa, String isb) {
            this.isa = isa;
            this.isb = isb;
        }
    }

    private class inner implements Serializable {
        public inneri zooi;
        public inneri yooi;
        private final int ia;
        private final String ib;
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
