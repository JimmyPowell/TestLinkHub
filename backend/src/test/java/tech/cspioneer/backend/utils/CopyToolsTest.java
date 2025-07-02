package tech.cspioneer.backend.utils;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CopyToolsTest {
    static class A { public String name; public int age; }
    static class B { public String name; public int age; }

    @Test
    void testCopyList_Normal() {
        A a1 = new A(); a1.name = "a1"; a1.age = 1;
        A a2 = new A(); a2.name = "a2"; a2.age = 2;
        List<A> list = Arrays.asList(a1, a2);
        List<B> copied = CopyTools.copyList(list, B.class);
        assertEquals(2, copied.size());
        assertNull(copied.get(0).name);
        assertEquals(0, copied.get(0).age);
        assertNull(copied.get(1).name);
        assertEquals(0, copied.get(1).age);
    }

    @Test
    void testCopyList_Empty() {
        List<A> list = new ArrayList<>();
        List<B> copied = CopyTools.copyList(list, B.class);
        assertTrue(copied.isEmpty());
    }

    @Test
    void testCopy_NullSource() {
        assertThrows(IllegalArgumentException.class, () -> CopyTools.copy(null, B.class));
    }

    @Test
    void testCopyList_Exception() {
        class C { private C() {} } // 无 public 构造器
        List<C> list = Arrays.asList(new C());
        List<B> copied = CopyTools.copyList(list, B.class);
        assertEquals(1, copied.size());
        assertNotNull(copied.get(0));
        assertNull(copied.get(0).name);
        assertEquals(0, copied.get(0).age);
    }

    // 有 getter/setter 的类
    static class C {
        private String name;
        private int age;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
    static class D {
        private String name;
        private int age;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    @Test
    void testCopyList_WithGetterSetter() {
        C c = new C(); c.setName("c1"); c.setAge(10);
        List<C> list = Arrays.asList(c);
        List<D> copied = CopyTools.copyList(list, D.class);
        assertEquals(1, copied.size());
        assertEquals("c1", copied.get(0).getName());
        assertEquals(10, copied.get(0).getAge());
    }

    @Test
    void testCopy_WithGetterSetter() {
        C c = new C(); c.setName("c2"); c.setAge(20);
        D d = CopyTools.copy(c, D.class);
        assertNotNull(d);
        assertEquals("c2", d.getName());
        assertEquals(20, d.getAge());
    }

    @Test
    void testCopyList_NewInstanceException() {
        class E { private E() {} }
        List<E> list = Arrays.asList(new E());
        List<D> copied = CopyTools.copyList(list, D.class);
        assertEquals(1, copied.size());
        assertNotNull(copied.get(0)); // D 有 public 构造器
        assertNull(copied.get(0).getName());
    }

    @Test
    void testCopy_BeanUtilsException() {
        assertThrows(IllegalArgumentException.class, () -> CopyTools.copy(null, D.class));
    }

    @Test
    void testCopyList_NullList() {
        assertThrows(IllegalArgumentException.class, () -> CopyTools.copyList(null, B.class));
    }

    @Test
    void testCopyList_ListWithNullElement() {
        List<A> list = Arrays.asList((A) null);
        assertThrows(IllegalArgumentException.class, () -> CopyTools.copyList(list, B.class));
    }

    @Test
    void testCopyList_NullClass() {
        List<A> list = Arrays.asList(new A());
        assertThrows(IllegalArgumentException.class, () -> CopyTools.copyList(list, null));
    }

    @Test
    void testCopy_NullClass() {
        A a = new A();
        assertThrows(IllegalArgumentException.class, () -> CopyTools.copy(a, null));
    }

    @Test
    void testCopy_NullSourceAndClass() {
        assertThrows(IllegalArgumentException.class, () -> CopyTools.copy(null, null));
    }
} 