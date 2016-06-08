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

package com.pepperonas.jxaesprefs;

import com.pepperonas.jxaesprefs.utils.Crypt;
import com.pepperonas.jxaesprefs.utils.Log;
import com.pepperonas.jxaesprefs.utils.NumberFormatUtils;
import com.pepperonas.jxaesprefs.utils.TimeFormatUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import static java.util.prefs.Preferences.userNodeForPackage;

/**
 * @author Martin Pfeffer (pepperonas)
 */
public class AesPrefs {

    /**
     * Constants
     */
    private static final String TAG = "AesPrefs";

    private static final String TAIL = "=";
    public static final String AES_APP_LAUNCHES = "aes_app_launches";
    public static final String AES_INST_DATE = "aes_inst_date";

    /**
     * Member
     */
    private static Class<?> mClazz;
    private static String mPassword;

    private static long mIv;

    private static long mDuration = 0;


    public enum LogMode {
        NONE(-1), DEFAULT(0), GET(1), SET(2), ALL(3);

        private final int mode;


        LogMode(int i) {
            this.mode = i;
        }
    }


    private static LogMode mLog = LogMode.DEFAULT;


    public static void logMode(LogMode logMode) {
        mLog = logMode;
    }


    public static void init(Class<?> clazz, String password, LogMode logMode) {
        mLog = logMode;
        init(clazz, password);
    }


    public static void initCompleteConfig(Class<?> clazz, String password, LogMode logMode) {
        mLog = logMode;
        init(clazz, password);
        initOrIncrementLaunchCounter();
        initInstallationDate();
    }


    public static void init(Class<?> clazz, String password) {
        if (mLog != LogMode.NONE) {
            Log.i(TAG, "Initializing AesPrefs...");
        }

        mClazz = clazz;
        mPassword = password;
        mIv = System.currentTimeMillis();

        if (nodeExists("aes_iv")) {

            if (mLog != LogMode.NONE) {
                Log.i(TAG, "IV found {" + mIv + "}");
            }

            //  retrieving an IV we can rely on.
            mIv = Preferences.userNodeForPackage(mClazz).getLong("aes_iv", -1);

        } else {
            // this IV will be used to keep track of your preference keys.
            // preference values have their own IVs.
            if (mLog != LogMode.NONE) {
                Log.w(TAG, "New IV set {" + mIv + "}");
            }

            mIv = System.currentTimeMillis();
            Preferences.userNodeForPackage(mClazz).putLong("aes_iv", mIv);
        }
    }


    private static boolean nodeExists(String key) {
        return Preferences.userNodeForPackage(mClazz).get(key, null) != null;
    }


    public static void registerNodeChangeListener(NodeChangeListener nodeChangeListener) {
        userNodeForPackage(mClazz).addNodeChangeListener(nodeChangeListener);
    }


    public static void unregisterNodeChangeListener(NodeChangeListener nodeChangeListener) {
        userNodeForPackage(mClazz).removeNodeChangeListener(nodeChangeListener);
    }


    public static void registerPreferenceChangeListener(PreferenceChangeListener preferenceChangeListener) {
        userNodeForPackage(mClazz).addPreferenceChangeListener(preferenceChangeListener);
    }


    public static void unregisterPreferenceChangeListener(PreferenceChangeListener preferenceChangeListener) {
        userNodeForPackage(mClazz).removePreferenceChangeListener(preferenceChangeListener);
    }


    /**
     * Put.
     *
     * @param key   the key
     * @param value the value
     */
    public static void put(String key, String value) {
        long start = System.currentTimeMillis();

        long iv = System.currentTimeMillis();

        String encryptedKey = Crypt.encrypt(mPassword, key, mIv);
        String encryptedValue = Crypt.encrypt(mPassword, value, iv);
        Preferences.userNodeForPackage(mClazz).put(encryptedKey, encryptedValue);
        Preferences.userNodeForPackage(mClazz).putLong(encryptedKey + TAIL, iv);

        if (mLog == LogMode.ALL || mLog == LogMode.SET) {
            Log.d(TAG, "put " + key + " <- " + value);
        }

        mDuration += System.currentTimeMillis() - start;
    }


    /**
     * Get string.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the string
     */

    public static String get(String key, String defaultValue) {
        long start = System.currentTimeMillis();
        String param = key;

        String _key = Crypt.encrypt(mPassword, key, mIv) + TAIL;
        long iv = Preferences.userNodeForPackage(mClazz).getLong(_key, 0);
        key = _key.substring(0, _key.length() - 1);

        if (!nodeExists(key)) {
            if (mLog != LogMode.NONE) {
                if (defaultValue == null) {
                    Log.e(TAG, "WARNING: Key '" + param + "' not found (return:  null)");
                } else
                    Log.e(TAG, "WARNING: Key '" + param + "' not found (return: " + (defaultValue.equals("") ? "\"\"" :
                            defaultValue) + ")");
            }
            return defaultValue;
        }

        try {
            mDuration += System.currentTimeMillis() - start;
            String value = Crypt.decrypt(mPassword, Preferences.userNodeForPackage(mClazz).get(key, ""), iv);
            if (mLog == LogMode.ALL || mLog == LogMode.GET) {
                Log.d(TAG, "get  " + param + " -> " + value);
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }


    /**
     * Put int.
     *
     * @param key   the key
     * @param value the value
     */
    public static void putInt(String key, int value) {
        long start = System.currentTimeMillis();

        long iv = System.currentTimeMillis();

        String encryptedKey = Crypt.encrypt(mPassword, key, mIv);
        String encryptedValue = Crypt.encrypt(mPassword, String.valueOf(value), iv);

        Preferences.userNodeForPackage(mClazz).put(encryptedKey, encryptedValue);
        Preferences.userNodeForPackage(mClazz).putLong(encryptedKey + TAIL, iv);

        if (mLog == LogMode.ALL || mLog == LogMode.SET) {
            Log.d(TAG, "putInt " + key + " <- " + value);
        }

        mDuration += System.currentTimeMillis() - start;
    }


    /**
     * Gets int.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the int
     */
    public static int getInt(String key, int defaultValue) {
        long start = System.currentTimeMillis();
        String param = key;


        String _key = Crypt.encrypt(mPassword, key, mIv) + TAIL;
        long iv = Preferences.userNodeForPackage(mClazz).getLong(_key, 0);
        key = _key.substring(0, _key.length() - 1);

        if (!nodeExists(key)) {
            if (mLog != LogMode.NONE) {
                Log.e(TAG, "WARNING: Key '" + param + "' not found (return: " + defaultValue + ")");
            }
            return defaultValue;
        }

        try {
            mDuration += System.currentTimeMillis() - start;
            int value = Integer.parseInt(Crypt.decrypt(mPassword, Preferences.userNodeForPackage(mClazz).get(key, ""), iv));
            if (mLog == LogMode.ALL || mLog == LogMode.GET) {
                Log.d(TAG, "getInt  " + param + " -> " + value);
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }


    /**
     * Put long.
     *
     * @param key   the key
     * @param value the value
     */
    public static void putLong(String key, long value) {
        long start = System.currentTimeMillis();

        long iv = System.currentTimeMillis();

        String encryptedKey = Crypt.encrypt(mPassword, key, mIv);
        String encryptedValue = Crypt.encrypt(mPassword, String.valueOf(value), iv);

        Preferences.userNodeForPackage(mClazz).put(encryptedKey, encryptedValue);
        Preferences.userNodeForPackage(mClazz).putLong(encryptedKey + TAIL, iv);

        if (mLog == LogMode.ALL || mLog == LogMode.SET) {
            Log.d(TAG, "putLong " + key + " <- " + value);
        }

        mDuration += System.currentTimeMillis() - start;
    }


    /**
     * Gets long.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the long
     */
    public static long getLong(String key, long defaultValue) {
        long start = System.currentTimeMillis();
        String param = key;

        String _key = Crypt.encrypt(mPassword, key, mIv) + TAIL;
        long iv = Preferences.userNodeForPackage(mClazz).getLong(_key, 0);
        key = _key.substring(0, _key.length() - 1);

        if (!nodeExists(key)) {
            if (mLog != LogMode.NONE) {
                Log.e(TAG, "WARNING: Key '" + param + "' not found (return: " + defaultValue + ")");
            }
            return defaultValue;
        }

        try {
            mDuration += System.currentTimeMillis() - start;
            long value = Long.parseLong(Crypt.decrypt(mPassword, Preferences.userNodeForPackage(mClazz).get(key, ""), iv));
            if (mLog == LogMode.ALL || mLog == LogMode.GET) {
                Log.d(TAG, "getLong  " + param + " -> " + value);
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }


    /**
     * Put double.
     *
     * @param key   the key
     * @param value the value
     */
    public static void putDouble(String key, double value) {
        long start = System.currentTimeMillis();

        long iv = System.currentTimeMillis();

        String encryptedKey = Crypt.encrypt(mPassword, key, mIv);
        String encryptedValue = Crypt.encrypt(mPassword, String.valueOf(value), iv);

        Preferences.userNodeForPackage(mClazz).put(encryptedKey, encryptedValue);
        Preferences.userNodeForPackage(mClazz).putLong(encryptedKey + TAIL, iv);

        if (mLog == LogMode.ALL || mLog == LogMode.SET) {
            Log.d(TAG, "putDouble " + key + " <- " + value);
        }

        mDuration += System.currentTimeMillis() - start;
    }


    /**
     * Gets double.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the double
     */
    public static double getDouble(String key, double defaultValue) {
        long start = System.currentTimeMillis();
        String param = key;

        String _key = Crypt.encrypt(mPassword, key, mIv) + TAIL;
        long iv = Preferences.userNodeForPackage(mClazz).getLong(_key, 0);
        key = _key.substring(0, _key.length() - 1);

        if (!nodeExists(key)) {
            if (mLog != LogMode.NONE) {
                Log.e(TAG, "WARNING: Key '" + param + "' not found (return: " + defaultValue + ")");
            }
            return defaultValue;
        }

        try {
            mDuration += System.currentTimeMillis() - start;
            double value = Double.parseDouble(Crypt.decrypt(mPassword, Preferences.userNodeForPackage(mClazz).get(key, ""), iv));
            if (mLog == LogMode.ALL || mLog == LogMode.GET) {
                Log.d(TAG, "getDouble  " + param + " -> " + value);
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }


    /**
     * Put float.
     *
     * @param key   the key
     * @param value the value
     */
    public static void putFloat(String key, float value) {
        long start = System.currentTimeMillis();

        long iv = System.currentTimeMillis();

        String encryptedKey = Crypt.encrypt(mPassword, key, mIv);
        String encryptedValue = Crypt.encrypt(mPassword, String.valueOf(value), iv);

        Preferences.userNodeForPackage(mClazz).put(encryptedKey, encryptedValue);
        Preferences.userNodeForPackage(mClazz).putLong(encryptedKey + TAIL, iv);

        if (mLog == LogMode.ALL || mLog == LogMode.SET) {
            Log.d(TAG, "putFloat " + key + " <- " + value);
        }

        mDuration += System.currentTimeMillis() - start;
    }


    /**
     * Gets float.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the float
     */
    public static float getFloat(String key, float defaultValue) {
        long start = System.currentTimeMillis();
        String param = key;

        String _key = Crypt.encrypt(mPassword, key, mIv) + TAIL;
        long iv = Preferences.userNodeForPackage(mClazz).getLong(_key, 0);
        key = _key.substring(0, _key.length() - 1);

        if (!nodeExists(key)) {
            if (mLog != LogMode.NONE) {
                Log.e(TAG, "WARNING: Key '" + param + "' not found (return: " + defaultValue + ")");
            }
            return defaultValue;
        }

        try {
            mDuration += System.currentTimeMillis() - start;
            float value = Float.parseFloat(Crypt.decrypt(mPassword, Preferences.userNodeForPackage(mClazz).get(key, ""), iv));
            if (mLog == LogMode.ALL || mLog == LogMode.GET) {
                Log.d(TAG, "getFloat  " + param + " -> " + value);
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }


    /**
     * Put boolean.
     *
     * @param key   the key
     * @param value the value
     */
    public static void putBoolean(String key, boolean value) {
        long start = System.currentTimeMillis();

        long iv = System.currentTimeMillis();

        String encryptedKey = Crypt.encrypt(mPassword, key, mIv);
        String encryptedValue = Crypt.encrypt(mPassword, String.valueOf(value), iv);

        Preferences.userNodeForPackage(mClazz).put(encryptedKey, encryptedValue);
        Preferences.userNodeForPackage(mClazz).putLong(encryptedKey + TAIL, iv);

        if (mLog == LogMode.ALL || mLog == LogMode.SET) {
            Log.d(TAG, "putBoolean " + key + " <- " + value);
        }

        mDuration += System.currentTimeMillis() - start;
    }


    /**
     * Gets boolean.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the boolean
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        long start = System.currentTimeMillis();
        String param = key;

        String _key = Crypt.encrypt(mPassword, key, mIv) + TAIL;
        long iv = Preferences.userNodeForPackage(mClazz).getLong(_key, 0);
        key = _key.substring(0, _key.length() - 1);

        if (!nodeExists(key)) {
            if (mLog != LogMode.NONE) {
                Log.e(TAG, "WARNING: Key '" + param + "' not found (return: " + defaultValue + ")");
            }
            return defaultValue;
        }

        try {
            mDuration += System.currentTimeMillis() - start;
            boolean value = Boolean.parseBoolean(Crypt.decrypt(mPassword, Preferences.userNodeForPackage(mClazz).get(key, ""),
                    iv));
            if (mLog == LogMode.ALL || mLog == LogMode.GET) {
                Log.d(TAG, "getBoolean  " + param + " -> " + value);
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }


    public static void storeArray(String key, List<String> values) {
        long start = System.currentTimeMillis();

        long iv = System.currentTimeMillis();

        String encryptedKey = Crypt.encrypt(mPassword, key, mIv);
        Preferences sp = Preferences.userNodeForPackage(mClazz);

        sp.putInt(encryptedKey + "_size", values.size());
        sp.putLong(encryptedKey + TAIL, iv);

        for (int i = 0; i < values.size(); i++) {
            String encryptedValue = Crypt.encrypt(mPassword, values.get(i), iv);
            sp.put(encryptedKey + "_" + i, encryptedValue);
        }

        mDuration += System.currentTimeMillis() - start;
    }


    public static List<String> restoreArray(String key) {
        long start = System.currentTimeMillis();

        Preferences sp = Preferences.userNodeForPackage(mClazz);

        String _key = Crypt.encrypt(mPassword, key, mIv) + TAIL;
        long iv = sp.getLong(_key, 0);
        key = _key.substring(0, _key.length() - 1);
        int size = sp.getInt(key + "_size", 0);

        List<String> strings = new ArrayList<String>();
        for (int i = 0; i < size; i++) {

            if (nodeExists((_key + "_" + i)) && mLog != LogMode.NONE) {
                Log.e(TAG, "WARNING: Key '" + key + "_" + i + "' not found.\n" +
                        "Return value: " + "new ArrayList<String>(0)");
                mDuration += System.currentTimeMillis() - start;
                return new ArrayList<String>();
            }

            strings.add(Crypt.decrypt(mPassword, sp.get(key + "_" + i, ""), iv));
        }
        mDuration += System.currentTimeMillis() - start;
        return strings;
    }


    public static String getEncryptedContent() {
        String result = null;

        Preferences sp = Preferences.userNodeForPackage(mClazz);
        try {
            String[] keys = sp.keys();
            for (String key : keys) {
                Object value = sp.get(key, null);
                result += key + " : " + value + "\n";
            }
        } catch (Exception e) {
            Log.e(TAG, "getEncryptedContent ");
        }
        return result;
    }


    public static String getEncryptedKey(String key) {
        String _key = Crypt.encrypt(mPassword, key, mIv) + TAIL;
        return _key.substring(0, _key.length() - 1);
    }


    /**
     * Count entries int.
     *
     * @return the int
     */
    public static int countEntries() {
        try {
            return Preferences.userNodeForPackage(mClazz).keys().length;
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * Delete all.
     */
    public static void deleteAll() {
        try {
            userNodeForPackage(mClazz).clear();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }


    /**
     * Init or increment launch counter.
     */
    public static void initOrIncrementLaunchCounter() {
        LogMode tmp = mLog;
        mLog = LogMode.NONE;
        if (getInt("aes_app_launches", -1) == -1) {
            // first launch insert 0
            putInt("aes_app_launches", 0);
        } else {
            putInt("aes_app_launches", (getInt("aes_app_launches", 0) + 1));
        }
        mLog = tmp;
    }


    /**
     * Gets launch counter.
     *
     * @return the launch counter
     */
    public static int getLaunchCounter() {
        return AesPrefs.getInt("aes_app_launches", 0);
    }


    /**
     * Init installation date.
     */
    public static void initInstallationDate() {
        LogMode tmp = mLog;
        mLog = LogMode.NONE;
        if (getLong("aes_inst_date", -1) == -1) {
            putLong("aes_inst_date", System.currentTimeMillis());
        }
        mLog = tmp;
    }


    /**
     * Gets installation date.
     *
     * @return the installation date
     */
    public static long getInstallationDate() {
        return getLong("aes_inst_date", 0L);
    }


    /**
     * Print installation date.
     */
    public static void printInstallationDate() {
        LogMode tmp = mLog;
        mLog = LogMode.NONE;
        Log.i(TAG, "Installation date: " + TimeFormatUtils.formatTime(getLong("aes_inst_date", 0L), TimeFormatUtils
                .DEFAULT_FORMAT));
        mLog = tmp;
    }


    /**
     * Reset execution time.
     */
    public static void resetExecutionTime() {
        mDuration = 0L;
    }


    /**
     * Gets execution time.
     *
     * @return the execution time
     */
    public static long getExecutionTime() {
        return mDuration;
    }


    /**
     * Print execution time.
     */
    public static void printExecutionTime() {
        Log.i(TAG, "Execution time: " + String.valueOf(NumberFormatUtils.decimalPlaces((double) mDuration / 1000, 3) + " sec."));
    }


    public static class Version {

        private static String ARTIFACT_ID = "jxaesprefs";


        /**
         * @return The library name.
         */
        public static String getLibararyName() {
            return "JxAesPrefs";
        }


        /**
         * @return The version number.
         */
        public static String getVersion() {
            Properties prop = new Properties();
            InputStream in = Main.class.getClassLoader().getResourceAsStream("project.properties");
            try {
                prop.load(in);
                in.close();
                return prop.getProperty("version");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "0";
        }


        /**
         * @return The version info.
         */
        public static String getVersionInfo() {
            return ARTIFACT_ID + "-" + getVersion();
        }


        /**
         * @return The name of the .jar-file.
         */
        public static String getJarName() {
            return ARTIFACT_ID + "-" + getVersion() + ".jar";
        }


        /**
         * @return The license text.
         */
        public static String getLicense() {
            return "Copyright (c) 2016 Martin Pfeffer\n" +
                    " \n" +
                    "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "you may not use this file except in compliance with the License.\n" +
                    "You may obtain a copy of the License at\n" +
                    " \n" +
                    "     http://www.apache.org/licenses/LICENSE-2.0\n" +
                    " \n" +
                    "Unless required by applicable law or agreed to in writing, software\n" +
                    "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "See the License for the specific language governing permissions and\n" +
                    "limitations under the License.";
        }

    }

}