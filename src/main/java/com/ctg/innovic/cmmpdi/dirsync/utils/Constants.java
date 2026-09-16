package com.ctg.innovic.cmmpdi.dirsync.utils;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    public final static String ERRORCODE_PREFIX = "DSYNC-ERR";
    public final static String BASE_OU_WO_STARTING_COMMAND = "dc=cmmp-di-test,dc=ctg";
    public final static String BASE_OU = ",dc=cmmp-di-test,dc=ctg";

    public static Map<String, String> bd2ouMap = new HashMap<>();


    // OU=ITB-OGCIO,OU=CMMPBDOs,DC=uat,DC=cmmp,DC=hksarg

    public static String mapbd2ou(String bd) {

        return bd;

//        if ( bd2ouMap.isEmpty() == true ) {
//
//            bd2ouMap.put("BDOA", "OU_of_BDOA");
//        }

//        return bd2ouMap.get( bd.toUpperCase());
    }

}
