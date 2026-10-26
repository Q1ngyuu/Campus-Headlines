package com.campus.headlines.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.StringJoiner;

/**
 * Service 层日志切面：统一记录「调用了什么、传了什么、跑了多久、有没有抛异常」。
 *
 * <p>切点 {@code execution(* com.campus.headlines.service..*.*(..))} 覆盖 service 包及其子包
 * 的所有方法，业务代码里没有一行日志语句——这就是 AOP 的价值：
 * 横切关注点（日志、耗时、异常）集中在一处，加日志不用改 20 个类。</p>
 *
 * <p>几个实现细节：</p>
 * <ul>
 *   <li>耗时用 {@link Deque} 做栈：service 之间会互相调用（比如 ArticleService 调 CategoryService），
 *       单值 ThreadLocal 会被内层调用覆盖，栈才能正确配对；</li>
 *   <li>参数与返回值都做截断，避免一条日志刷几十行；</li>
 *   <li>DTO 入参只打印类名，不打印字段——注册/登录 DTO 里有密码，日志是最容易泄漏的地方；</li>
 *   <li>异常只记类型与消息，堆栈交给全局异常处理器与 Log4j2 的 error 级别去展开。</li>
 * </ul>
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    /** 日志里参数/返回值的最大字符数 */
    private static final int MAX_TEXT_LENGTH = 200;

    /** 单个字符串参数的最大字符数 */
    private static final int MAX_STRING_LENGTH = 80;

    /** 记录调用开始时间（栈式，支持嵌套调用） */
    private static final ThreadLocal<Deque<Long>> START_STACK =
            ThreadLocal.withInitial(ArrayDeque::new);

    /** 切点：service 包及子包下的所有方法 */
    @Pointcut("execution(* com.campus.headlines.service..*.*(..))")
    public void serviceMethods() {
        // 仅作为切点声明，方法体为空
    }

    /**
     * 前置通知：记录方法名与入参，并压入开始时间。
     */
    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        START_STACK.get().push(System.currentTimeMillis());
        log.info("[AOP-前] 调用 {}.{}() 入参={}",
                simpleClassName(joinPoint), joinPoint.getSignature().getName(), formatArgs(joinPoint.getArgs()));
    }

    /**
     * 返回通知：记录耗时与返回值摘要。
     */
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        long cost = popCost();
        log.info("[AOP-后] 完成 {}.{}() 耗时={}ms 返回={}",
                simpleClassName(joinPoint), joinPoint.getSignature().getName(), cost, describe(result));
    }

    /**
     * 异常通知：记录异常类型与消息。
     */
    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        long cost = popCost();
        log.error("[AOP-异常] {}.{}() 耗时={}ms 异常={} 消息={}",
                simpleClassName(joinPoint), joinPoint.getSignature().getName(), cost,
                ex.getClass().getSimpleName(), ex.getMessage());
    }

    // ------------------------------------------------------------------
    //  工具方法
    // ------------------------------------------------------------------

    /** 弹出开始时间并计算耗时 */
    private long popCost() {
        Deque<Long> stack = START_STACK.get();
        Long start = stack.poll();
        if (stack.isEmpty()) {
            START_STACK.remove();     // 防止线程池复用线程时残留
        }
        return start == null ? -1 : System.currentTimeMillis() - start;
    }

    private String simpleClassName(JoinPoint joinPoint) {
        return joinPoint.getTarget() == null
                ? joinPoint.getSignature().getDeclaringType().getSimpleName()
                : joinPoint.getTarget().getClass().getSimpleName();
    }

    /** 拼接入参，整体超长则截断 */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (Object arg : args) {
            joiner.add(describe(arg));
        }
        String text = joiner.toString();
        if (text.length() > MAX_TEXT_LENGTH) {
            return text.substring(0, MAX_TEXT_LENGTH) + "…(共 " + text.length() + " 字符)";
        }
        return text;
    }

    /** 把任意对象描述成一段短文本，避免把大对象/敏感字段写进日志 */
    private String describe(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof MultipartFile) {
            MultipartFile file = (MultipartFile) value;
            return "MultipartFile(name=" + file.getOriginalFilename() + ", size=" + file.getSize() + ")";
        }
        if (value instanceof CharSequence) {
            return "\"" + abbreviate(value.toString(), MAX_STRING_LENGTH) + "\"";
        }
        if (value instanceof Collection) {
            return value.getClass().getSimpleName() + "(size=" + ((Collection<?>) value).size() + ")";
        }
        // DTO 只记类型：注册/登录 DTO 含密码，字段不进日志
        String className = value.getClass().getSimpleName();
        if (className.endsWith("DTO")) {
            return className + "{…}";
        }
        if (value instanceof Number || value instanceof Boolean || value instanceof Enum) {
            return String.valueOf(value);
        }
        return abbreviate(String.valueOf(value), MAX_STRING_LENGTH);
    }

    /** 超长截断 */
    private String abbreviate(String text, int maxLength) {
        if (text == null) {
            return "null";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "…";
    }
}
