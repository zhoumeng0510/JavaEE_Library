package demo.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhoumeng on
 * 2017.6.16.
 * 下午 08:32.
 */
public class DateTest {
    public static void main(String[] args) {
        /*
            table
            time Date -- 'yyyy-MM-dd'
         */

        Date date = new Date();
        System.out.println(date);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println(format.format(date));
    } // spaceX
}
