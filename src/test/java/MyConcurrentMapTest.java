import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyConcurrentMapTest {

    @Test
    void testPutAndGet() {
        MyConcurrentMap<String, Integer> map = new MyConcurrentMap<>();
        assertNull(map.put("one", 1));
        assertEquals(1, map.get("one"));
        assertEquals(1, map.size());

        assertEquals(1, map.put("one", 2));
        assertEquals(2, map.get("one"));
        assertEquals(1, map.size());
    }

    @Test
    void testRemove() {
        MyConcurrentMap<String, Integer> map = new MyConcurrentMap<>();
        map.put("one", 1);
        assertEquals(1, map.remove("one"));
        assertNull(map.get("one"));
        assertEquals(0, map.size());

        assertNull(map.remove("one"));
    }

    @Test
    void testSize() {
        MyConcurrentMap<String, Integer> map = new MyConcurrentMap<>();
        map.put("one", 1);
        map.put("two", 2);
        assertEquals(2, map.size());

        map.remove("one");
        assertEquals(1, map.size());
    }

    @Test
    void testConcurrency() throws InterruptedException {
        MyConcurrentMap<String, Integer> map = new MyConcurrentMap<>();

        Runnable putTask = () -> {
            for (int i = 0; i < 1000; i++) {
                map.put("key" + i, i);
            }
        };

        Runnable getTask = () -> {
            for (int i = 0; i < 1000; i++) {
                map.get("key" + i);
            }
        };

        Thread thread1 = new Thread(putTask);
        Thread thread2 = new Thread(getTask);
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        assertEquals(1000, map.size());
    }
}
