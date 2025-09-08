import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.List;

public class blobproxy implements Serializable, InvocationHandler {
    public int a;
    public String b;
    public blobproxy(int a) {
        String b = "zoo";
        this.a = a;
        this.b = b;
    }

    public static void main(String[] args) {
        try {
            blobproxy bp = new blobproxy(55);
            Class<?> pclass = Proxy.getProxyClass(blobproxy.class.getClassLoader(),
                    List.class, Iterator.class);
            Constructor<?> constructor = pclass.getConstructor(InvocationHandler.class);
            List s = (List) constructor.newInstance(new Object[]{bp});
            System.out.println("s.get: " + s.get(2));
            Iterator it = (Iterator) s;
            System.out.println("it.next: " + it.next());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) {
        System.out.println("invoke: proxy " + proxy.getClass()
                + "  method " + method.toString());
        return "foo";
    }

    public String toString() {
        return "[blobproxy a " + a + "  b " + b + "]";
    }
}
