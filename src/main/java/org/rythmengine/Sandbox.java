/**
 * Copyright (C) 2013-2016 The Rythm Engine project
 * for LICENSE and other details see:
 * https://github.com/rythmengine/rythmengine
 */
package org.rythmengine;

import org.rythmengine.logger.ILogger;
import org.rythmengine.logger.Logger;
import org.rythmengine.sandbox.RythmSecurityManager;
import org.rythmengine.sandbox.SandboxExecutingService;

import java.io.File;
import java.util.Map;
import java.util.Stack;

/**
 * A wrapper of Rythm engine and make sure the rendering is happen in Sandbox mode
 */
public class Sandbox {

    private static final ILogger logger = Logger.get(Sandbox.class);

    private static final InheritableThreadLocal<Boolean> sandboxMode = new InheritableThreadLocal<Boolean>();

    static boolean sandboxMode() {
        Boolean mode = sandboxMode.get();
        return null == mode ? false : mode;
    }
    
    RythmEngine engine;
    SandboxExecutingService secureExecutor = null;
    
    private static boolean sandboxLive = false;

    /**
     * Turn off sandbox mode. Used by Rythm unit testing program
     * @param code
     */
    public static void turnOffSandbox(String code) {
        if (!sandboxLive) return;
        rsm().forbiddenIfCodeNotMatch(code);
        sandboxLive = false;
        System.setSecurityManager(null);
    }

    public Sandbox(RythmEngine engine, SandboxExecutingService executor) {
        this.engine = engine;
        this.secureExecutor = executor;
        sandboxLive = true;
        restrictedZone.set(new Stack<Boolean>());
    }
    
    private static RythmSecurityManager rsm() {
        return (RythmSecurityManager)System.getSecurityManager();
    }

    private RythmEngine engine() {
        if (null != engine) return engine;
        return Rythm.engine();
    }

    private Map<String, Object> userContext;

    public Sandbox setUserContext(Map<String, Object> context) {
        this.userContext = context;
        return this;
    }

    public String render(String template, Object... args) {
        sandboxMode.set(true);
        try {
            return secureExecutor.execute(userContext, template, args);
        } finally {
            sandboxMode.remove();
        }
    }

    public String render(File file, Object... args) {
        sandboxMode.set(true);
        try {
            return secureExecutor.execute(userContext,file, args);
        } finally {
            sandboxMode.remove();
        }
    }

    public static String hasAccessToRestrictedClasses(RythmEngine engine, String code) {
        for (String s : engine.conf().restrictedClasses()) {
            if (code.contains(s)) return s;
        }
        return null;
    }
    
    private final static ThreadLocal<Stack<Boolean>> restrictedZone = new ThreadLocal<Stack<Boolean>>(){
        @Override
        protected Stack<Boolean> initialValue() {
            return new Stack<Boolean>();
        }
    };
    
    public final static void enterRestrictedZone(String code) {
        if (!sandboxLive || !sandboxMode()) return;
        rsm().forbiddenIfCodeNotMatch(code);
        restrictedZone.get().push(true);
    }

    public final static void enterSafeZone(String code) {
        if (!sandboxLive || !sandboxMode()) return;
        rsm().forbiddenIfCodeNotMatch(code);
        restrictedZone.get().push(false);
    }
    
    public final static void leaveCurZone(String code) {
        if (!sandboxLive || !sandboxMode()) return;
        rsm().forbiddenIfCodeNotMatch(code);
        Stack<Boolean> stack = restrictedZone.get();
        if (stack.isEmpty()) {
            throw new IllegalStateException("EMPTY ZONE");
        }
        stack.pop();
    }
    
    public final static boolean isRestricted() {
        if (!sandboxLive || !sandboxMode()) return false;
        Stack<Boolean> stack = restrictedZone.get();
        if (stack.isEmpty()) return false;
        return stack.peek();
    }

}
