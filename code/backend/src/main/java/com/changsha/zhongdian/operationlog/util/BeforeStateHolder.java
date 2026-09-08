package com.changsha.zhongdian.operationlog.util;

public class BeforeStateHolder {
    
    private static final ThreadLocal<Object> beforeState = new ThreadLocal<>();
    
    public static void set(Object state) {
        beforeState.set(state);
    }
    
    public static Object get() {
        return beforeState.get();
    }
    
    public static void clear() {
        beforeState.remove();
    }
}
