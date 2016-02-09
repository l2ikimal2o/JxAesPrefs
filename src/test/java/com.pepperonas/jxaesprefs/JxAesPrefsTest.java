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

import com.pepperonas.jxaesprefs.utils.Log;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Martin Pfeffer (pepperonas)
 */
public class JxAesPrefsTest extends TestCase {

    private static final String TAG = "JxAesPrefsTest";


    protected void setUp() {
        AesPrefs.init(JxAesPrefsTest.class, "password", AesPrefs.LogMode.ALL);
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testLaunchCounter() {
        assertEquals(0, AesPrefs.getLaunchCounter());
        AesPrefs.initOrIncrementLaunchCounter();
        AesPrefs.initOrIncrementLaunchCounter();
        AesPrefs.initOrIncrementLaunchCounter();

        assertTrue(AesPrefs.getLaunchCounter() > 1);
    }


    public void testInitInstallationDate() {
        AesPrefs.initInstallationDate();
        Log.d(TAG, "testInitInstallationDate " + new Date(AesPrefs.getInstallationDate()));
    }


    public void testString() {
        AesPrefs.put("string_key", "Test String");
        assertEquals("Test String", AesPrefs.get("string_key", "defaultValue"));
    }


    public void testInt() {
        AesPrefs.putInt("int_key", 2015);
        assertEquals(2015, AesPrefs.getInt("int_key", 0));
    }


    public void testBoolean() {
        AesPrefs.putBoolean("boolean_key", false);
        assertEquals(false, AesPrefs.getBoolean("boolean_key", true));
    }


    public void testFloat() {
        AesPrefs.putFloat("float_key", 1.75f);
        assertEquals(1.75f, AesPrefs.getFloat("float_key", 0.0f));
    }


    public void testDouble() {
        AesPrefs.putDouble("double_key", 9.99999999D);
        assertEquals(9.99999999D, AesPrefs.getDouble("double_key", 0));
    }


    public void testLong() {
        AesPrefs.putLong("long_key", 123456789101112L);
        assertEquals(123456789101112L, AesPrefs.getLong("long_key", 0));
    }


    public void testInitString() {
        AesPrefs.initString("init_string_key", "Test initial string");
        AesPrefs.initString("init_string_key", "Wipe initial string");
        assertEquals("Test initial string", AesPrefs.get("init_string_key", "defaultValue"));
    }


    public void testInitInt() {
        AesPrefs.initInt("init_int_key", 42);
        AesPrefs.initInt("init_int_key", -2);
        assertEquals(42, AesPrefs.getInt("init_int_key", 0));
    }


    public void testInitBoolean() {
        AesPrefs.initBoolean("init_boolean_key", true);
        AesPrefs.initBoolean("init_boolean_key", false);
        assertEquals(true, AesPrefs.getBoolean("init_boolean_key", false));
    }


    public void testInitFloat() {
        AesPrefs.initFloat("init_float_key", 1.23f);
        AesPrefs.initFloat("init_float_key", 3.21f);
        assertEquals(1.23f, AesPrefs.getFloat("init_float_key", 0.0f));
    }


    public void testInitDouble() {
        AesPrefs.initDouble("init_double_key", 15.151515D);
        AesPrefs.initDouble("init_double_key", 51.515151D);
        assertEquals(15.151515D, AesPrefs.getDouble("init_double_key", 15.151515D));
    }


    public void testInitLong() {
        AesPrefs.initLong("init_long_key", 98765432111L);
        AesPrefs.initLong("init_long_key", 12345678999L);
        assertEquals(98765432111L, AesPrefs.getLong("init_long_key", 98765432111L));
    }


    public void testStoreArray() {
        List<String> stringsToStore = new ArrayList<String>(100);
        for (int i = 0; i < 100; i++) {
            stringsToStore.add("Value at " + i);
        }
        AesPrefs.storeArray("string_array_key", stringsToStore);
        List<String> stringsToRestore = AesPrefs.restoreArray("string_array_key");
        for (int i = 0; i < stringsToStore.size(); i++) {
            Log.d(TAG, "testStoreArray " + stringsToRestore.get(i));
        }
        assertEquals(stringsToStore.get(20), stringsToRestore.get(20));
    }


    public void testGetEncryptedKey() {
        assertTrue(AesPrefs.getEncryptedKey("string_key").length() != 0);
    }


    public void testXGetEncryptedContent() {
        String s = AesPrefs.getEncryptedContent();
        Log.d(TAG, "testGetEncryptedContent\n" + s + "\n");
        assertTrue(s.length() != 0);
    }


    public void testYGetExecutionTime() {
        Log.e(TAG, "testGetExecutionTime " + AesPrefs.getExecutionTime() + " ms");
        assertTrue(AesPrefs.getExecutionTime() > 1 && AesPrefs.getExecutionTime() < 1000);
        AesPrefs.resetExecutionTime();
        assertTrue(AesPrefs.getExecutionTime() < 10);
    }


    public void testZDeleteAll() {
        Log.d(TAG, "testDeleteAll " + AesPrefs.countEntries() + " (before)");
        AesPrefs.deleteAll();
        Log.d(TAG, "testDeleteAll " + AesPrefs.countEntries() + " (after)");
        assertEquals(0, AesPrefs.countEntries());
    }
}
