package com.inksetter.twist;

import com.inksetter.twist.expression.NullValueException;
import com.inksetter.twist.expression.TypeMismatchException;
import com.inksetter.twist.expression.UnrecognizedMethodException;
import com.inksetter.twist.parser.ScriptSyntaxException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for working with Java objects, collections and maps from scripts.
 */
public class JavaInteropTest {

    public static class Person {
        private String name = "Bob";
        private int age = 30;
        private final List<String> tags = new ArrayList<>();

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public List<String> getTags() { return tags; }
        public boolean isAdult() { return age >= 18; }
        public String greet(Object other) { return "hi " + other; }
        public String describe(CharSequence prefix, Number n) { return prefix + ":" + n; }
    }

    private static Object exec(String script, Map<String, Object> vars) throws TwistException {
        return Twist.parseScript(script).execute(new SimpleScriptContext(vars, Map.of()));
    }

    @Test
    public void testMethodWithObjectParameter() throws TwistException {
        List<Object> list = new ArrayList<>();
        exec("list.add(1); list.add('two'); list.add([3])", Map.of("list", list));
        assertEquals(List.of(1, "two", List.of(3)), list);
    }

    @Test
    public void testMethodWithSupertypeParameters() throws TwistException {
        assertEquals("hi Al", exec("p.greet('Al')", Map.of("p", new Person())));
        assertEquals("x:5", exec("p.describe('x', 5)", Map.of("p", new Person())));
        assertEquals("x:1.5", exec("p.describe('x', 1.5)", Map.of("p", new Person())));
    }

    @Test
    public void testMethodWithPrimitiveParameters() throws TwistException {
        assertEquals("cde", exec("s.substring(2, 5)", Map.of("s", "abcdefg")));
        assertEquals('c', exec("s.charAt(2)", Map.of("s", "abcdefg")));
    }

    @Test
    public void testMethodWithNullArgument() throws TwistException {
        assertEquals("hi null", exec("p.greet(null)", Map.of("p", new Person())));
    }

    @Test
    public void testMethodOverloadSelection() throws TwistException {
        assertEquals(2, exec("s.indexOf('c')", Map.of("s", "abcabc")));
        assertEquals(5, exec("s.indexOf('c', 3)", Map.of("s", "abcabc")));
    }

    @Test
    public void testUnknownMethod() {
        assertThrows(UnrecognizedMethodException.class, () -> exec("s.noSuchMethod()", Map.of("s", "x")));
        // Right name, wrong argument types
        assertThrows(UnrecognizedMethodException.class, () -> exec("s.substring('a')", Map.of("s", "x")));
    }

    @Test
    public void testMethodOnNull() {
        assertThrows(NullValueException.class, () -> exec("x = null; x.foo()", Map.of()));
        assertThrows(NullValueException.class, () -> exec("x = null; x.foo", Map.of()));
    }

    @Test
    public void testCollectionMethods() throws TwistException {
        assertEquals(3, exec("[3, 1, 2].size()", Map.of()));
        assertEquals(true, exec("['a', 'b'].contains('b')", Map.of()));
        assertEquals(List.of(1, 2), exec("l = [1]; l.add(2); l", Map.of()));
        assertEquals(1, exec("m = {a: 1}; m.get('a')", Map.of()));
        assertEquals(true, exec("m = {a: 1}; m.containsKey('a')", Map.of()));
    }

    @Test
    public void testStringMethods() throws TwistException {
        assertEquals("b", exec("'a,b,c'.split(',')[1]", Map.of()));
        assertEquals("ORLD", exec("name.toUpperCase().substring(1)", Map.of("name", "World")));
        assertEquals("xxx", exec("'x'.repeat(3)", Map.of()));
    }

    @Test
    public void testBeanProperties() throws TwistException {
        Person p = new Person();
        Map<String, Object> vars = Map.of("p", p);
        assertEquals("Bob", exec("p.name", vars));
        assertEquals(true, exec("p.adult", vars));
        exec("p.name = 'Alice'; p.age = 12", vars);
        assertEquals("Alice", p.getName());
        assertEquals(12, p.getAge());
        assertEquals(false, exec("p.adult", vars));
        exec("p.age += 10", vars);
        assertEquals(22, p.getAge());
    }

    @Test
    public void testNestedBeanCollections() throws TwistException {
        Person p = new Person();
        exec("p.tags.add('a'); p.tags.add('b')", Map.of("p", p));
        assertEquals(List.of("a", "b"), p.getTags());
        assertEquals("b", exec("p.tags[1]", Map.of("p", p)));
    }

    @Test
    public void testMapMembers() throws TwistException {
        assertEquals(2, exec("cfg = {db: {host: 'h', ports: [1, 2]}}; cfg.db.ports[1]", Map.of()));
        assertEquals("x", exec("cfg = {db: {host: 'h'}}; cfg.db.host = 'x'; cfg.db.host", Map.of()));
        assertEquals(1, exec("m = {}; m.newKey = 1; m.newKey", Map.of()));
        assertNull(exec("cfg = {}; cfg.missing", Map.of()));
    }

    @Test
    public void testMapMembersWriteThroughToJava() throws TwistException {
        Map<String, Object> m = new HashMap<>();
        exec("m.a = 1; m.b = m.a + 1", Map.of("m", m));
        assertEquals(Map.of("a", 1, "b", 2), m);
    }

    @Test
    public void testArrayElements() throws TwistException {
        String[] arr = {"one", "two"};
        assertEquals("two", exec("arr[1]", Map.of("arr", arr)));
        exec("arr[0] = 'uno'", Map.of("arr", arr));
        assertEquals("uno", arr[0]);
        assertEquals(List.of("z", "b"), exec("l = ['a', 'b']; l[0] = 'z'; l", Map.of()));
        assertEquals(3, exec("[[1, 2], [3]][1][0]", Map.of()));
    }

    @Test
    public void testIndexOutOfRange() {
        assertThrows(IndexOutOfBoundsException.class, () -> exec("[1, 2][5]", Map.of()));
    }

    @Ignore("Known gap: [...] does not look up map keys")
    @Test
    public void testMapIndexing() throws TwistException {
        assertEquals(1, exec("m = {'a b': 1}; m['a b']", Map.of()));
        assertEquals(2, exec("m = {}; m['y'] = 2; m.y", Map.of()));
    }

    @Test
    public void testIndexingNonList() {
        assertThrows(TypeMismatchException.class, () -> exec("m = {a: 1}; m['a']", Map.of()));
    }

    @Test
    public void testBraceAtStatementStartIsBlock() throws TwistException {
        // At the start of a statement, { opens a block rather than a map literal
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseScript("{a: 1}.get('a')"));
        assertEquals(Map.of("a", 1), exec("return {a: 1}", Map.of()));
    }
}
