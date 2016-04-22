package com.joesnason.pttreader.filter;

/**
 * Created by joesnason on 2016/4/22.
 */
public class filter {


    static public String filter_Color_Control(String ori_data){
        return ori_data.replaceAll("\u001B\\[[;\\d]*m", "");
    }


    static public String filter_all_Control(String ori_data){

        return ori_data.replaceAll("\u001B\\[[;\\d]*m", "").replaceAll("\u001B\\[[;\\d]*H","").replaceAll("\u001B\\[[;\\d]*K","");
    }
}
