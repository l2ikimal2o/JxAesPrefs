/*
 * Copyright (c) 2016 Martin Pfeffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pepperonas.jxaesprefs.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * @author Martin Pfeffer (pepperonas)
 */
public class Log {

    private static String mLogId = "";


    public static void setUniqueLogId(String logId) {
        mLogId = logId + " | ";
    }


    /**
     * Send a VERBOSE log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void v(String tag, String msg) {
        System.out.println("V/" + mLogId + tag + " - " + msg);
    }


    /**
     * Send a INFO log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void i(String tag, String msg) {
        System.out.println("I/" + mLogId + tag + " - " + msg);
    }


    /**
     * Send a DEBUG log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void d(String tag, String msg) {
        System.out.println("D/" + mLogId + tag + " - " + msg);
    }


    /**
     * Send a DEBUG log message.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param msg     The message you would like logged.
     * @param strings The list of String objects you would like logged.
     */
    public static void d(String tag, String msg, List<String> strings) {
        int i = 0;
        for (String s : strings) d(tag, msg + " [" + (i++) + "]" + s);
    }


    /**
     * Send a DEBUG log message.
     *
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param msg     The message you would like logged.
     * @param strings The array of String objects you would like logged.
     */
    public static void d(String tag, String msg, String[] strings) {
        int i = 0;
        for (String s : strings) d(tag, msg + " [" + (i++) + "]" + s);
    }


    /**
     * Send a WARN log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void w(String tag, String msg) {
        System.out.println("W/" + mLogId + tag + " - " + msg);
    }


    /**
     * Send a WARN log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log.
     */
    public static void w(String tag, String msg, Throwable tr) {
        System.out.println("W/" + mLogId + tag + " - " + msg + '\n' + getStackTraceString(tr));
    }


    /**
     * Send an ERROR log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void e(String tag, String msg) {
        System.out.println("E/" + mLogId + tag + " - " + msg);
    }


    /**
     * Send a ERROR log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log.
     */
    public static void e(String tag, String msg, Throwable tr) {
        System.out.println("E/" + mLogId + tag + " - " + msg + '\n' + getStackTraceString(tr));
    }


    /**
     * Send a What a Terrible Failure log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void wtf(String tag, String msg) {
        System.out.println("WTF/" + mLogId + tag + " - " + msg);
    }


    /**
     * Send a What a Terrible Failure log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log.
     */
    public static void wtf(String tag, String msg, Throwable tr) {
        System.out.println("WTF/" + mLogId + tag + " - " + msg + '\n' + getStackTraceString(tr));
    }


    public static void logHashMap(String tag, int i, Map<String, Object> params) {
        for (String name : params.keySet()) {
            String v = params.get(name).toString();
            Log.d(tag, "Map[" + (i++) + "] " + name + " = " + v);
        }
    }


    /**
     * Handy function to get a loggable stack trace from a Throwable.
     *
     * @param tr An exception to log.
     */
    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

}
