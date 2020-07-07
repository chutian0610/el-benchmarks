package info.victorchu.ik;

import info.victorchu.ExpEngine;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.wltea.expression.ExpressionEvaluator;
import org.wltea.expression.datameta.Variable;
import org.wltea.expression.function.FunctionLoader;
import org.wltea.expression.function.SystemFunctions;

import java.io.File;
import java.util.*;
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
public class IkExpBenchMark {
    @State(Scope.Benchmark)
    public static class IkExpEngine implements ExpEngine {

        @Override
        public Object evaluate(String exp, Map<String, Object> env) {
            List<Variable> varialbes = new LinkedList<Variable>();
            env.forEach((k,v)->varialbes.add(Variable.createVariable(k,v)));
            return ExpressionEvaluator.evaluate(exp, varialbes);
        }
    }

    /**
     * 计算算术表达式
     * @param ikExpEngine
     * @throws Exception
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testSimpleLiteral(IkExpEngine ikExpEngine, Blackhole blackhole) {
        blackhole.consume(
                ikExpEngine.evaluate("1000+100.0*99-(600-3*15)/(((68-9)-3)*2-100)+10000%7*71",
                        Collections.emptyMap()));
    }

    /**
     * 计算逻辑表达式和三元表达式混合
     * @param ikExpEngine
     * @throws Exception
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testOptionalLiteral(IkExpEngine ikExpEngine,Blackhole blackhole) {
        blackhole.consume(
                ikExpEngine.evaluate("6.7-100>39.6 ? 5==5? 4+5:6-1 : !(100%3-39.0<27) ? 8*2-199: 100%3",
                        Collections.emptyMap()));
    }

    /**
     * 计算算术表达式，带有5个变量的表达式
     * @param ikExpEngine
     * @throws Exception
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testVariableExpression(IkExpEngine ikExpEngine,Blackhole blackhole) {
        Map<String, Object> env = new HashMap<String, Object>();
        int i = 100;
        float pi = 3.14f;
        double d = -3.9;
        int b = 4;
        boolean bool = false;
        env.put("i", i);
        env.put("pi", pi);
        env.put("d", d);
        env.put("b", b);
        env.put("bool", bool);
        blackhole.consume(ikExpEngine.evaluate(
                "pi*d+b-(1000-d*b/pi)/(pi+99-i*d)-i*pi*d/b", env));

    }
    /**
     * 计算算术表达式和逻辑表达式的混合，带有5个变量的表达式
     * @param ikExpEngine
     * @throws Exception
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testVariableAndBooleanExpression(IkExpEngine ikExpEngine,Blackhole blackhole) {
        Map<String, Object> env = new HashMap<String, Object>();
        int i = 100;
        float pi = 3.14f;
        double d = -3.9;
        int b = 4;
        boolean bool = false;
        env.put("i", i);
        env.put("pi", pi);
        env.put("d", d);
        env.put("b", b);
        env.put("bool", bool);
        blackhole.consume(
                ikExpEngine.evaluate(
                        "i*pi+(d*b-199)/(1-d*pi)-(2+100-i/pi)%99==i*pi+(d*b-199)/(1-d*pi)-(2+100-i/pi)%99",
                        env));

    }

    /**
     * 系统时间调用
     * @param ikExpEngine
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testSystemTime(IkExpEngine ikExpEngine,Blackhole blackhole){
        blackhole.consume(ikExpEngine.evaluate("$SYSDATE()", Collections.EMPTY_MAP));
    }

    /**
     * 截取字符串方法
     * @param ikExpEngine
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testSubString(IkExpEngine ikExpEngine,Blackhole blackhole){
        Map<String, Object> env = new HashMap<String, Object>();
        env.put("s", "hello world");
        env.put("d", 5);
        blackhole.consume(ikExpEngine.evaluate("$SUBSTRINGBEGIN(s,d)", env));
    }

    /**
     * 嵌套调用 subString 函数
     * @param ikExpEngine
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void testMultiSubString(IkExpEngine ikExpEngine,Blackhole blackhole){
        Map<String, Object> env = new HashMap<String, Object>();
        env.put("a", 1);
        env.put("s", "hello world");
        env.put("d", 5);
        env.put("e", 4);
        blackhole.consume(
                ikExpEngine.evaluate("$SUBSTRING($SUBSTRINGBEGIN(s,d),a,e)", env));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(IkExpBenchMark.class.getSimpleName())
                .output(System.getProperty("user.dir")+ File.separator+"benchmarks" + File.separator+"Benchmark-ik.log") // benchmark log
                .build();
        new Runner(options).run();
    }
}
