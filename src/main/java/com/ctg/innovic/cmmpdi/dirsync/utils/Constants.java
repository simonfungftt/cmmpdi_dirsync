package com.ctg.innovic.cmmpdi.dirsync.utils;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final String ARGS_DELTA = "delta";
    public static final String ARGS_FULL = "full";
    public static final String ARGS_WGD = "wgd";

    public static final String LOGGING_ENTERING = "Entering ";
    public static final String LOGGER_LDAPQUERY_DEBUG = "LDAP Base DN: %s, filter: %s";



    public static final String CMMP_BASEOU = "OU=CMMPBDOs";
    public static final String CMMP_OU_TO_REPLACE_WITH = "OU=CMMPBDOs,DC=uat,DC=cmmp,DC=hksarg";


    public static final String BASE_OU_WO_STARTING_COMMAND = "OU=BDO,OU=CMMPDI-BDOs";

    public static final String BASE_OU = "," + BASE_OU_WO_STARTING_COMMAND;

    public static final String ERRORCODE_PREFIX = "DSYNC-ERR";

    public static final String LDAP_BASE_OU_BDO = "ou=bdo,ou=cmmpdi-bdos";
    public static final String LDAP_BASE_OU_WGD = "ou=wgd,ou=cmmpdi-bdos";


    public static final String LDAP_FIELD_OUPATH = "ouPath";
    public static final String LDAP_FIELD_OBJECT_CLASS = "objectClass";
    public static final String LDAP_FIELD_DISPLAY_NAME = "displayName";
    public static final String LDAP_FIELD_DISTINGUISHEDNAME = "distinguishedName";
    public static final String LDAP_FIELD_SN = "sn";
    public static final String LDAP_FIELD_CN = "cn";
    public static final String LDAP_FIELD_MAIL = "mail";
    public static final String LDAP_FIELD_TITLE = "title";
    public static final String LDAP_FIELD_GIVEN_NAME = "givenName";
    public static final String LDAP_FIELD_PROXY_ADDRESS = "proxyAddresses";
    public static final String LDAP_FIELD_DESCRIPTION = "description";
    public static final String LDAP_FIELD_MEMBER = "member";
    public static final String LDAP_FIELD_EXT1 = "extensionAttribute1";
    public static final String LDAP_FIELD_EXT3 = "extensionAttribute3";
    public static final String LDAP_FIELD_EXT7 = "extensionAttribute7";
    public static final String LDAP_FIELD_GCERT = "gCert";
    public static final String LDAP_FIELD_ECERT = "userCert";
    public static final String LDAP_FIELD_CERT17 = "userCert17";
    public static final String LDAP_FIELD_CERT18 = "userCert18";
    public static final String LDAP_FIELD_WHENCREATED = "whenCreated";
    public static final String LDAP_FIELD_WHENCHANGED = "whenChanged";
    public static final String LDAP_FIELD_MOBILEDATACLASSENTITLE = "mobileDataClassEntitle";
    public static final String LDAP_FIELD_OBJECTGUID = "objectGUID";
    public static final String LDAP_FIELD_INSTANCETYPE = "instanceType";
    public static final String LDAP_FIELD_OBJECTSID = "objectSid";
    public static final String LDAP_FIELD_MAILSERVPLAN = "mailServPlan";
    public static final String LDAP_FIELD_TELEPHONENUMBER = "telephoneNumber";
    public static final String LDAP_FIELD_USERACCOUNTCONTROL = "userAccountControl";
    public static final String LDAP_FIELD_EXTACCTYPE = "extAccType";
    public static final String LDAP_FIELD_USERPASSWORD = "userPassword";
    public static final String LDAP_FIELD_ACCOUNTEXPIRES = "accountExpires";
    public static final String LDAP_FIELD_PHYSICALDELIVERYOFFICENAME = "physicalDeliveryOfficeName";
    public static final String LDAP_FIELD_HIDEFROMADDRESSLISTS = "hideFromAddressLists";
    public static final String LDAP_FIELD_COUNTRYCODE = "countryCode";
    public static final String LDAP_FIELD_HOUSEIDENTIFIER = "houseIdentifier";
    public static final String LDAP_FIELD_ACCTSTATUS = "acctStatus";
    public static final String LDAP_FIELD_SAMACCOUNTNAME = "sAMAccountName";


    public static final int WGD_OPERATION_USER = 1;
    public static final int WGD_OPERATION_GROUP = 2;






    protected static Map<String, String> bd2ouMap = new HashMap<>();


    private Constants() {
    }

    public static String mapbd2ou(String bd) {

        return bd;
    }

}
