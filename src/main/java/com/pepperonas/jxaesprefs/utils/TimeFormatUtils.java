/*
 * Copyright (c) 2015 Martin Pfeffer
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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Martin Pfeffer (pepperonas)
 */
public class TimeFormatUtils {

    private static final String TAG = "TimeFormatUtils";

    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";


    public static String formatTime(long time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = new Date(time);
        return sdf.format(date);
    }

}
