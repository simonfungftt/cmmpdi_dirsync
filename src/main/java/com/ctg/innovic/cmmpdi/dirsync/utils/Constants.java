package com.ctg.innovic.cmmpdi.dirsync.utils;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final String LOGGING_ENTERING = "Entering ";

    public static final String CMMP_OU_TO_REPLACE_WITH = "OU=CMMPBDOs,DC=uat,DC=cmmp,DC=hksarg";

    public static final String BASE_OU_WO_STARTING_COMMAND = "OU=BDO,OU=CMMPDI-BDOs";

    public static final String BASE_OU = "," + BASE_OU_WO_STARTING_COMMAND;

    public static final String ERRORCODE_PREFIX = "DSYNC-ERR";

    public static final String LDAP_BASE_OU_BDO = "ou=bdo,ou=cmmpdi-bdos";

    public static final String LDAP_BASE_OU_WGD = "ou=wgd,ou=cmmpdi-bdos";

    public static final String LDAP_FIELD_OBJECT_CLASS = "objectClass";

    public static final String LDAP_FIELD_DISPLAY_NAME = "displayName";

    public static final String LDAP_FIELD_GIVEN_NAME = "givenName";

    public static final String LDAP_FIELD_COUNTRY_CODE = "countryCode";

    public static final String LDAP_FIELD_MEMBER = "member";

    public static final String LDAP_FIELD_EXT1 = "extensionAttribute1";

    public static final String LDAP_FIELD_EXT3 = "extensionAttribute1";

    public static final String LDAP_FIELD_EXT7 = "extensionAttribute7";

    public static final String LDAP_FIELD_GCERT = "gCert";

    public static final String LDAP_FIELD_ECERT = "userCert";

    public static final String LDAP_FIELD_CERT17 = "userCert17";

    public static final String LDAP_FIELD_CERT18 = "userCert18";








    protected static Map<String, String> bd2ouMap = new HashMap<>();


    private Constants() {
    }

    // OU=ITB-OGCIO,OU=CMMPBDOs,DC=uat,DC=cmmp,DC=hksarg

    public static String mapbd2ou(String bd) {

        return bd;
    }

}
