
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liuwl
 * @since 2019/12/18
 */
public class Test {
    public static void main(String[] args) {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        map.put("a","aaaaa");
        map.remove("a");
        System.out.println(map.toString());
    }
}
