package com.ctg.innovic.cmmpdi.dirsync.utils;

/**
 * Application-wide constants for Directory Sync.
 *
 * <p>Note: Environment-specific values (e.g., UAT/PROD Base DNs) should be
 * loaded from external configuration rather than hardcoded here.
 */
public final class Constants {

    public static final String ERROR_CODE_PREFIX = "DSYNC-ERR";

    private Constants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public enum SyncArg {
        DELTA("delta"),
        FULL_USER("full-user"),
        FULL_GROUP("full-group"),
        FULL_WGD("full-wgd"),
        WGD("wgd"),
        EXPORT("export");

        private final String value;

        SyncArg(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum WgdOperation {
        USER(1),
        GROUP(2);

        private final int code;

        WgdOperation(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public static final class Logging {
        public static final String ENTERING = "Entering ";
        public static final String LDAP_QUERY_DEBUG = "LDAP Base DN: %s, filter: %s";

        private Logging() {}
    }

    public static final class LdapOu {
        public static final String CMMP_BASE = "OU=CMMPBDOs";
        public static final String BASE_WITHOUT_LEADING_COMMA = "OU=BDO,OU=CMMPDI-BDOs";
        public static final String BASE = "," + BASE_WITHOUT_LEADING_COMMA;

        public static final String BDO = "ou=bdo,ou=cmmpdi-bdos";
        public static final String WGD = "ou=wgd,ou=cmmpdi-bdos";

        // TODO: Move to application.properties / environment config (contains UAT DC)
        @Deprecated
        public static final String CMMP_OU_TO_REPLACE_WITH = "OU=CMMPBDOs,DC=uat,DC=cmmp,DC=hksarg";

        private LdapOu() {}
    }

    public static final class LdapField {
        public static final String OU_PATH = "ouPath";
        public static final String OBJECT_CLASS = "objectClass";
        public static final String DISPLAY_NAME = "displayName";
        public static final String DISTINGUISHED_NAME = "distinguishedName";
        public static final String SN = "sn";
        public static final String CN = "cn";
        public static final String MAIL = "mail";
        public static final String TITLE = "title";
        public static final String GIVEN_NAME = "givenName";
        public static final String PROXY_ADDRESSES = "proxyAddresses";
        public static final String DESCRIPTION = "description";
        public static final String MEMBER = "member";
        public static final String EXTENSION_ATTRIBUTE_1 = "extensionAttribute1";
        public static final String EXTENSION_ATTRIBUTE_3 = "extensionAttribute3";
        public static final String EXTENSION_ATTRIBUTE_7 = "extensionAttribute7";
        public static final String G_CERT = "gCert";
        public static final String USER_CERT = "userCert";
        public static final String USER_CERT_17 = "userCert17";
        public static final String USER_CERT_18 = "userCert18";
        public static final String WHEN_CREATED = "whenCreated";
        public static final String WHEN_CHANGED = "whenChanged";
        public static final String MOBILE_DATA_CLASS_ENTITLE = "mobileDataClassEntitle";
        public static final String OBJECT_GUID = "objectGUID";
        public static final String INSTANCE_TYPE = "instanceType";
        public static final String OBJECT_SID = "objectSid";
        public static final String MAIL_SERV_PLAN = "mailServPlan";
        public static final String TELEPHONE_NUMBER = "telephoneNumber";
        public static final String USER_ACCOUNT_CONTROL = "userAccountControl";
        public static final String EXT_ACC_TYPE = "extAccType";
        public static final String USER_PASSWORD = "userPassword";
        public static final String ACCOUNT_EXPIRES = "accountExpires";
        public static final String PHYSICAL_DELIVERY_OFFICE_NAME = "physicalDeliveryOfficeName";
        public static final String HIDE_FROM_ADDRESS_LISTS = "hideFromAddressLists";
        public static final String COUNTRY_CODE = "countryCode";
        public static final String HOUSE_IDENTIFIER = "houseIdentifier";
        public static final String ACCT_STATUS = "acctStatus";
        public static final String SAM_ACCOUNT_NAME = "sAMAccountName";

        private LdapField() {}
    }
}