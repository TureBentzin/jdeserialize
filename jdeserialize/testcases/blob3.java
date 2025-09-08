import java.io.Serializable;

public class blob3 implements Serializable {
    private final int[][] amdi;
    private final int[] ai;
    private final String[] foo;
    private final int ix = 0x12345678;

    public blob3(int a) {
        this.amdi = new int[10][3];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 3; j++) {
                amdi[i][j] = a + i + j;
            }
        }
        this.ai = new int[]{a + 1, a + 2, a + 3, a + 4, a + 5,};
        this.foo = new String[]{"one", "two", "three", "four"};
    }

    public String toString() {
        return "[blob3]";
    }
}
