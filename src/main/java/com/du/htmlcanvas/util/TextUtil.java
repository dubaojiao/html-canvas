package com.du.htmlcanvas.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @Title
 * @ClassName TextUtil
 * @Author jsb_pbk
 * @Date 2018/9/14
 */
public class TextUtil {

    private static  final  String[]  STRINGS = new String[]{
            "山羊","水牛","小狗","小猪","小猫","西瓜","榴莲","小可爱","山羊","电脑","小汽车","","大卡车","客车","桌子","女人",
            "包包","大树","小草"
    };

    public static String getText(){
        Random random = new Random();
        int x = random.nextInt(10);
        return STRINGS[x];
    }


    public static String date(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }
}
