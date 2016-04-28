package com.ivigilate.android.library.classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by nuno.freire on 21/04/16.
 */
public class ApiResponse<T> {
    public long timestamp;
    public T data;
}
