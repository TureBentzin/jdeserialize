import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;


public class blobex implements Serializable {
    public int a;
    public String b;
    public blobex(int a) {
        String b = "zoo";
        this.a = a;
        this.b = b;
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        throw new IOException("woops");
    }

    public String toString() {
        return "[blobex a " + a + "  b " + b + "]";
    }
}
