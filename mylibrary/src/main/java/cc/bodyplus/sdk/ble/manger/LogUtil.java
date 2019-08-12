package cc.bodyplus.sdk.ble.manger;

import android.os.Looper;
import android.util.Log;

import java.util.Hashtable;

/**
 * Created by Shihoo.Wang 2019/3/26
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class LogUtil {
    public static boolean logFlag = true;

    public final static String tag = "[bodyplus]";
    private final static int logLevel = Log.VERBOSE;
    private static Hashtable<String, LogUtil> sLoggerTable = new Hashtable<String, LogUtil>();
    private String mClassName;

    private static LogUtil wshLog;


    private static final String WSH = "@wsh@";

    private LogUtil(String name) {
        mClassName = name;
    }


    private static LogUtil getLogger(String className) {
        LogUtil classLogger = (LogUtil) sLoggerTable.get(className);
        if (classLogger == null) {
            classLogger = new LogUtil(className);
            sLoggerTable.put(className, classLogger);
        }
        return classLogger;
    }

    public static LogUtil wshLog(){
        if (wshLog == null){
            wshLog = new LogUtil(WSH);
        }
        return wshLog;
    }

    private String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }
            return mClassName + "[ " + Thread.currentThread().getName() + ": "
                    + st.getFileName() + ":" + st.getLineNumber() + " "
                    + st.getMethodName() + " - isMain :"+ (Looper.myLooper() == Looper.getMainLooper())+" ]";
        }
        return null;
    }

    public void i(Object str) {
        if (logFlag) {
            if (logLevel <= Log.INFO) {
                String name = getFunctionName();
                if (name != null) {
                    Log.i(tag, name + " - " + str);
                } else {
                    Log.i(tag, str.toString());
                }
            }
        }

    }

    public void d(Object str) {
        if (logFlag) {
            if (logLevel <= Log.DEBUG) {
                String name = getFunctionName();
                if (name != null) {
                    Log.d(tag, name + " - " + str);
                } else {
                    Log.d(tag, str.toString());
                }
            }
        }
    }



    public void v(Object str) {
        if (logFlag) {
            if (logLevel <= Log.VERBOSE) {
                String name = getFunctionName();
                if (name != null) {
                    Log.v(tag, name + " - " + str);
                } else {
                    Log.v(tag, str.toString());
                }
            }
        }
    }

    public void w(Object str) {
        if (logFlag) {
            if (logLevel <= Log.WARN) {
                String name = getFunctionName();
                if (name != null) {
                    Log.w(tag, name + " - " + str);
                } else {
                    Log.w(tag, str.toString());
                }
            }
        }
    }

    public void e(Object str) {
        if (logFlag) {
            if (logLevel <= Log.ERROR) {
                String name = getFunctionName();
                if (name != null) {
                    Log.e(tag, name + " - " + str);
                } else {
                    Log.e(tag, str.toString());
                }
            }
        }
    }

    public void e(Exception ex) {
        if (logFlag) {
            if (logLevel <= Log.ERROR) {
                Log.e(tag, "error", ex);
            }
        }
    }

    public void e(String log, Throwable tr) {
        if (logFlag) {
            String line = getFunctionName();
            Log.e(tag, "{Thread:" + Thread.currentThread().getName() + "}"
                    + "[" + mClassName + line + ":] " + log + "\n", tr);
        }
    }
}
