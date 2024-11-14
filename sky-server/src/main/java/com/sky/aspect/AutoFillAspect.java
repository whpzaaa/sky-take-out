package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component
@Aspect
@Slf4j
public class AutoFillAspect {
    //切点表达式的方法：mapper包中的所有方法和带有autofill注解
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut(){}
    //方法执行前设置创建和更新时间 用户
    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行公共字段自动填充");
        //利用joinpoint调用getsignature方法并且强转获取切面方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //利用签名获取方法 再获取方法上的autofill注解
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        //获取注解属性 即operationType的类型 update/insert
        OperationType value = autoFill.value();
        //利用joinpoint获取形参列表（数组） 目的是获得形参中传入的实体类
        Object[] args = joinPoint.getArgs();
        //如果数组为空 则立即返回
        if (args == null && args.length == 0) {
            return;
        }
        //约定形参数组第一个元素为实体类对象
        Object object = args[0];
        //提前获取当前时间和操作用户id
        LocalDateTime now = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();
        //如果是insert方法
        if (value == OperationType.INSERT) {
            //设置四个字段
            //根据对象反射获取类 再获取set方法
            Method setCreateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
            Method setCreateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
            Method setUpdateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
            Method setUpdateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
            //调用set方法 并传入对象和参数
            setCreateTime.invoke(object,now);
            setCreateUser.invoke(object,id);
            setUpdateTime.invoke(object,now);
            setUpdateUser.invoke(object,id);
        //如果为update方法
        } else if (value == OperationType.UPDATE) {
            //设置两个字段
            Method setCpdateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
            Method setUpdateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
            setCpdateTime.invoke(object,now);
            setUpdateUser.invoke(object,id);

        }
    }

}
