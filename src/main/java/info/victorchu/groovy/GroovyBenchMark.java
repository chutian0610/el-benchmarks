package info.victorchu.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import info.victorchu.ExpEngine;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Author victor
 * @Email victorchu0610@outlook.com
 * @Data 2019/6/20
 * @Version 1.0
 * @Description TODO
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3)
@Measurement(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)
@Threads(4)
@Fork(1)
public class GroovyBenchMark {
    @State(Scope.Benchmark)
    public static class GroovyExpEngine implements ExpEngine {

        // for script cache
        private Map<String, Class> scriptCache = new ConcurrentHashMap<String, Class>();

        @Setup
        public void init(){
            // add custom define aviator functions
        }

        private static Binding fillParamsIntoBinding(Map<String, Object> context){
            Binding binding =new Binding(context);
            return binding;
        }
        private Class getScriptClassWithCache(String content){
            Class script = null;
            if(scriptCache.containsKey(content)){
                script = scriptCache.get(content);
            }else {
                script = getScript(content);
                scriptCache.put(content,script);
            }
            return script;
        }
        private Class getScript(String content){
            Class script = new GroovyShell().parse(content).getClass();
            return script;
        }

        @Override
        public Object evaluate(String exp, Map<String, Object> env) {
            Class script = getScriptClassWithCache(exp);
            Binding binding = fillParamsIntoBinding(env);
            return InvokerHelper.createScript(script, binding).run();
        }
    }

    /**
     * 计算算术表达式
     * @param groovyExpEngine
     * @throws Exception
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testSimpleLiteral(GroovyExpEngine groovyExpEngine, Blackhole blackhole) {
        blackhole.consume(
                groovyExpEngine.evaluate("1000+100.0*99-(600-3*15)/(((68-9)-3)*2-100)+10000%7*71",
                        Collections.emptyMap()));
    }

    /**
     * 计算逻辑表达式和三元表达式混合
     * @param groovyExpEngine
     * @throws Exception
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testOptionalLiteral(GroovyExpEngine groovyExpEngine,Blackhole blackhole) {
        blackhole.consume(
                groovyExpEngine.evaluate("6.7-100>39.6 ? 5==5? 4+5:6-1 : !(100%3-39.0<27) ? 8*2-199: 100%3",
                        Collections.emptyMap()));
    }

    /**
     * 计算算术表达式，带有5个变量的表达式
     * @param groovyExpEngine
     * @throws Exception
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testVariableExpression(GroovyExpEngine groovyExpEngine,Blackhole blackhole) {
        Map<String, Object> env = new HashMap<String, Object>();
        int i = 100;
        float pi = 3.14f;
        double d = -3.9;
        byte b = (byte) 4;
        boolean bool = false;
        env.put("i", i);
        env.put("pi", pi);
        env.put("d", d);
        env.put("b", b);
        env.put("bool", bool);
        blackhole.consume(groovyExpEngine.evaluate(
                "pi*d+b-(1000-d*b/pi)/(pi+99-i*d)-i*pi*d/b", env));

    }
    /**
     * 计算算术表达式和逻辑表达式的混合，带有5个变量的表达式
     * @param groovyExpEngine
     * @throws Exception
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testVariableAndBooleanExpression(GroovyExpEngine groovyExpEngine,Blackhole blackhole) {
        Map<String, Object> env = new HashMap<String, Object>();
        int i = 100;
        float pi = 3.14f;
        double d = -3.9;
        byte b = (byte) 4;
        boolean bool = false;
        env.put("i", i);
        env.put("pi", pi);
        env.put("d", d);
        env.put("b", b);
        env.put("bool", bool);
        blackhole.consume(
                groovyExpEngine.evaluate(
                        "i*pi+(d*b-199)/(1-d*pi)-(2+100-i/pi)%99==i*pi+(d*b-199)/(1-d*pi)-(2+100-i/pi)%99",
                        env));

    }

    /**
     * 系统时间调用
     * @param groovyExpEngine
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testSystemTime(GroovyExpEngine groovyExpEngine,Blackhole blackhole){
        blackhole.consume(groovyExpEngine.evaluate("new java.util.Date()", Collections.EMPTY_MAP));
    }

    /**
     * 截取字符串方法
     * @param groovyExpEngine
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testSubString(GroovyExpEngine groovyExpEngine ,Blackhole blackhole){
        Map<String, Object> env = new HashMap<String, Object>();
        Map<String, Object> env1 = new HashMap<String, Object>();
        env.put("b", env1);
        env.put("s", "hello world");
        env1.put("d", 5);
        blackhole.consume(groovyExpEngine.evaluate("s.substring(b.d)", env));
    }

    /**
     * 嵌套调用 subString 函数
     * @param groovyExpEngine
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testMultiSubString(GroovyExpEngine groovyExpEngine,Blackhole blackhole){
        Map<String, Object> env = new HashMap<String, Object>();
        Map<String, Object> env1 = new HashMap<String, Object>();
        Map<String, Object> env2 = new HashMap<String, Object>();
        env.put("a", 1);
        env.put("b", env1);
        env.put("s", "hello world");
        env1.put("c", env2);
        env1.put("d", 5);
        env2.put("e", 4);
        blackhole.consume(
                groovyExpEngine.evaluate("s.substring(b.d).substring(a,b.c.e)", env));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(GroovyBenchMark.class.getSimpleName())
                .output(System.getProperty("user.dir")+ File.separator+"benchmarks" + File.separator+"Benchmark-groovy.log") // benchmark log
                .build();
        new Runner(options).run();
    }
}
