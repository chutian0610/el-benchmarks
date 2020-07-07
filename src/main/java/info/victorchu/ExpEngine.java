package info.victorchu;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @Author victor
 * @Email victorchu0610@outlook.com
 * @Data 2019/6/15
 * @Version 1.0
 * @Description TODO
 */
public interface ExpEngine {

    Object evaluate(String exp, Map<String, Object> env);
}
