package info.victorchu.aviator;

import com.googlecode.aviator.AviatorEvaluator;
import info.victorchu.ExpEngine;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3)
@Measurement(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)
@Threads(4)
@Fork(1)
public class AviatorBenchMark {

    @State(Scope.Benchmark)
    public static class AviatorExpEngine implements ExpEngine {

        @Setup
        public void init(){
            // add custom define aviator functions
        }
        @Override
        public Object evaluate(String exp, Map<String, Object> env) {
            // aviator already implement script caching
            return AviatorEvaluator.compile(exp, true).execute(env);
        }
    }

    /**
     * 计算算术表达式
     * @param aviatorExpEngine
     * @throws Exception
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testSimpleLiteral(AviatorExpEngine aviatorExpEngine, Blackhole blackhole) {
        blackhole.consume(
                aviatorExpEngine.evaluate("1000+100.0*99-(600-3*15)/(((68-9)-3)*2-100)+10000%7*71",
                        Collections.emptyMap()));
    }

    /**
     * 计算逻辑表达式和三元表达式混合
     * @param aviatorExpEngine
     * @throws Exception
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testOptionalLiteral(AviatorExpEngine aviatorExpEngine,Blackhole blackhole) {
        blackhole.consume(
                aviatorExpEngine.evaluate("6.7-100>39.6 ? 5==5? 4+5:6-1 : !(100%3-39.0<27) ? 8*2-199: 100%3",
                        Collections.emptyMap()));
    }

    /**
     * 计算算术表达式，带有5个变量的表达式
     * @param aviatorExpEngine
     * @throws Exception
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testVariableExpression(AviatorExpEngine aviatorExpEngine,Blackhole blackhole) {
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
        blackhole.consume(aviatorExpEngine.evaluate(
                "pi*d+b-(1000-d*b/pi)/(pi+99-i*d)-i*pi*d/b", env));

    }
    /**
     * 计算算术表达式和逻辑表达式的混合，带有5个变量的表达式
     * @param aviatorExpEngine
     * @throws Exception
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testVariableAndBooleanExpression(AviatorExpEngine aviatorExpEngine,Blackhole blackhole) {
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
                aviatorExpEngine.evaluate(
                "i*pi+(d*b-199)/(1-d*pi)-(2+100-i/pi)%99==i*pi+(d*b-199)/(1-d*pi)-(2+100-i/pi)%99",
                env));

    }

    /**
     * 系统时间调用
     * @param aviatorExpEngine
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testSystemTime(AviatorExpEngine aviatorExpEngine,Blackhole blackhole){
        blackhole.consume(aviatorExpEngine.evaluate("sysdate()", Collections.EMPTY_MAP));
    }

    /**
     * 截取字符串方法
     * @param aviatorExpEngine
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testSubString(AviatorExpEngine aviatorExpEngine,Blackhole blackhole){
        Map<String, Object> env = new HashMap<String, Object>();
        Map<String, Object> env1 = new HashMap<String, Object>();
        env.put("b", env1);
        env.put("s", "hello world");
        env1.put("d", 5);
        blackhole.consume(aviatorExpEngine.evaluate("string.substring(s,b.d)", env));
    }

    /**
     * 嵌套调用 subString 函数
     * @param aviatorExpEngine
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testMultiSubString(AviatorExpEngine aviatorExpEngine,Blackhole blackhole){
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
                aviatorExpEngine.evaluate("string.substring(string.substring(s,b.d),a,b.c.e)", env));
    }

    public static void main(String[] args) throws RunnerException, IOException {
        Options options = new OptionsBuilder()
                .include(AviatorBenchMark.class.getSimpleName())
                .output(System.getProperty("user.dir")+ File.separator+"benchmarks" + File.separator+"Benchmark-aviator.log") // benchmark log
                .build();
        new Runner(options).run();
    }

}
