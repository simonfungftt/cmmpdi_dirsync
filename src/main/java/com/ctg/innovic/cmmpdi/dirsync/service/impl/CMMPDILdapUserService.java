package com.ctg.innovic.cmmpdi.dirsync.service.impl;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.LdifUtils;
import com.ctg.innovic.cmmpdi.dirsync.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CMMPDILdapUserService {

    private static final Logger logger = LogManager.getLogger(CMMPDILdapUserService.class);
    private static final DateTimeFormatter LDAP_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss.S'Z'").withZone(ZoneOffset.UTC);

    //@Value("${cmmpdi.bdo.baseOU}")
    //private final String cmmpdiBDOBaseOU;

    //@Value("${cmmpdi.wgd.baseOU}")
    //private final String cmmpdiWGDBaseOU;

    //@Value("${execution.dryrun}")
    private final boolean executionDryrun;

    //@Autowired
    private final CMMPDILdapCacheService cmmpdiLdapCacheService;

    //@Autowired
    //@Qualifier("cmmpdiLdapTemplate")
    private final LdapTemplate ldapTemplate;

    //@Autowired
    private final CMMPDILdapQueryService cmmpdiLdapQueryService;

    public CMMPDILdapUserService(
            @Value("${execution.dryrun:false}") boolean executionDryrun,
            //@Value("${cmmpdi.bdo.baseOU}") String cmmpdiBDOBaseOU,
            //@Value("${cmmpdi.wgd.baseOU}") String cmmpdiWGDBaseOU,
            @Qualifier("cmmpdiLdapTemplate") LdapTemplate ldapTemplate,
            CMMPDILdapCacheService cacheService,
            CMMPDILdapQueryService cmmpdiLdapQueryService) {
        this.executionDryrun = executionDryrun;
        //this.cmmpdiBDOBaseOU = cmmpdiBDOBaseOU;
        //this.cmmpdiWGDBaseOU = cmmpdiWGDBaseOU;
        this.ldapTemplate = ldapTemplate;
        this.cmmpdiLdapCacheService = cacheService;
        this.cmmpdiLdapQueryService = cmmpdiLdapQueryService;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public List<CMMPDILdapUser> getAllUserUnderParamOU(@NotNull String pOU) {
        logEntering();

        String _logMessage = String.format("Trying to fetch CMMP-DI directory users under OU %s", pOU);
        logger.log(Level.INFO, _logMessage);

        LdapQuery query = LdapQueryBuilder.query()
                .base(pOU)
                .where(Constants.LDAP_FIELD_OBJECT_CLASS).is("inetOrgPerson");

        _logMessage = String.format("LDAP Base DN: %s, filter: %s", query.base(), query.filter());
        logger.log(Level.DEBUG, _logMessage);

        List<CMMPDILdapUser> result = ldapTemplate.find(query, CMMPDILdapUser.class);

        logger.info(String.format("%06d users have been fetched from directory", result.size()));

        return result;
    }

    public boolean createOrUpdateCMMPDIUser(@NotNull CMMPExchangeLdapUser exchangeUser) throws InvalidNameException {
        logEntering();

        String smtp = StringUtils.trimToEmpty(exchangeUser.getMail()).toLowerCase(Locale.ROOT);
        CMMPDILdapUser existingUser = this.cmmpdiLdapCacheService.getCMMPDIUserDnBySMTP(smtp);

        if (existingUser != null) {
            return updateCMMPDIUser(exchangeUser, existingUser);
        }

        // Remove colliding group if the SMTP address was previously assigned to a group
        CMMPDILdapGroup existingGroup = cmmpdiLdapCacheService.getCMMPDIGroupDnBySMTP(smtp);
        if (existingGroup != null) {
            logger.info("Detected SMTP address {} already used by group {}, removing group object.",
                    smtp, existingGroup.getDn());

            if (cmmpdiLdapQueryService.deleteCMMPDIBDOObjectByGroup(existingGroup)) {
                cmmpdiLdapCacheService.removeGroupCache(existingGroup);
            }
        }

        logger.info("Going to create user object with SMTP address = {}, original DN = {}",
                smtp.toLowerCase(Locale.ROOT), exchangeUser.getDn());
        return createCMMPDIUser(transformToCMMPDIUser(exchangeUser, null));
    }

    public boolean deleteCMMPDIUserByDN(@NotNull CMMPDILdapUser cmmpdiUser) {
        logEntering();

        Name dn = LdapNameBuilder.newInstance(cmmpdiUser.getDn()).build();
        logger.info("Going to delete user object with DN '{}' from WGD", dn);

        if (!executionDryrun) {
            //ldapTemplate.unbind(dn);
        }
        return true;
    }

    public boolean deleteCMMPDIUserBySMTP(@NotNull String smtpAddress) {
        logEntering();

        CMMPDILdapUser cmmpdiUser = cmmpdiLdapCacheService.getCMMPDIUserDnBySMTP(smtpAddress);
        return cmmpdiUser == null || deleteCMMPDIUserByDN(cmmpdiUser);
    }

    // -------------------------------------------------------------------------
    // Creation & Update Workflows
    // -------------------------------------------------------------------------

    protected boolean createCMMPDIUser(CMMPExchangeLdapUser exchangeUser) throws InvalidNameException {
        return createCMMPDIUser(transformToCMMPDIUser(exchangeUser, null));
    }

    protected boolean createCMMPDIUser(CMMPDILdapUser user) throws InvalidNameException {
        LdapNameBuilder builder = LdapNameBuilder.newInstance(Constants.BASE_OU_WO_STARTING_COMMAND);
        String[] ous = extractOus(user.getDn());
        logger.debug("OU array = {}", Arrays.toString(ous));

        for (int i = ous.length - 2; i >= 0; i--) {
            builder.add("ou", ous[i]);
        }
        String cn = getLocalPart(user.getEmail());
        Name dn = builder.add("cn", cn).build();

        DirContextAdapter context = buildBaseUserContext(dn, cn, user);
        context.setAttributeValue(Constants.LDAP_FIELD_DISTINGUISHEDNAME, user.getDistinguishedName());
        context.setAttributeValue(Constants.LDAP_FIELD_EXT7, String.valueOf(user.getExtensionAttribute7()));

        return bindAndCacheUser(context, user.getEmail(), false);
    }

    protected boolean createWGDCMMPDIUser(@NotNull CMMPDILdapUser user) throws InvalidNameException {

        String _userEmail = (user.getEmail());

        String cn = "wgd_" + getLocalPart(_userEmail);
        Name dn = LdapNameBuilder.newInstance()
                .add("OU", "CMMPDI-BDOs")
                .add("OU", "WGD")
                .add("OU", "User")
                .add("cn", cn)
                .build();

        DirContextAdapter context = buildBaseUserContext(dn, cn, user);

        // WGD users are created as disabled accounts
        context.addAttributeValue(Constants.LDAP_FIELD_USERACCOUNTCONTROL, "514");
        context.addAttributeValue(Constants.LDAP_FIELD_ACCTSTATUS, "D");

        context.addAttributeValue(Constants.LDAP_FIELD_OUPATH, "/");
        context.addAttributeValue(Constants.LDAP_FIELD_OUPATH, "/CMMPDI-BDOs");
        context.addAttributeValue(Constants.LDAP_FIELD_OUPATH, "/CMMPDI-BDOs/WGD");
        context.addAttributeValue(Constants.LDAP_FIELD_OUPATH, "/CMMPDI-BDOs/WGD/User");

        context.setAttributeValue(Constants.LDAP_FIELD_WHENCREATED, LDAP_TIMESTAMP_FORMAT.format(user.getWhenCreatedInstant()));
        context.setAttributeValue(Constants.LDAP_FIELD_WHENCHANGED, LDAP_TIMESTAMP_FORMAT.format(user.getWhenChangedInstant()));
        context.setAttributeValue(Constants.LDAP_FIELD_TELEPHONENUMBER, user.getTelephoneNumber());
        context.setAttributeValue(Constants.LDAP_FIELD_TITLE, user.getTitle());
        context.setAttributeValue(Constants.LDAP_FIELD_DISTINGUISHEDNAME, dn.toString());
        context.addAttributeValue(Constants.LDAP_FIELD_PROXY_ADDRESS, user.getProxyAddresses());

        if ( user.getUserCert17() != null ) {
            context.setAttributeValue(Constants.LDAP_FIELD_CERT17, toBase64Cert(user.getUserCert17()));
        }

        if ( user.getUserCert18() != null ) {
            context.setAttributeValue(Constants.LDAP_FIELD_CERT18, toBase64Cert(user.getUserCert18()));
        }

        if ( user.getgCert() != null ) {
            context.setAttributeValue(Constants.LDAP_FIELD_GCERT, toBase64Cert(user.getgCert()));
        }
        if ( user.getUserCert() != null ) {
            context.setAttributeValue(Constants.LDAP_FIELD_ECERT, toBase64Cert(user.getUserCert()));
        }

        dumpLdifIfDebug("debug/" + cn + ".ldif", context);
        return bindAndCacheUser(context, user.getEmail(), true);
    }

    protected boolean updateCMMPDIUser(CMMPExchangeLdapUser exchangeUser) {
        CMMPDILdapUser existingUser = cmmpdiLdapCacheService.getCMMPDIUserDnBySMTP(exchangeUser.getMail());
        return updateCMMPDIUser(exchangeUser, existingUser);
    }

    private boolean updateCMMPDIUser(CMMPExchangeLdapUser exchangeUser, CMMPDILdapUser cmmpdiUser) {
        if (cmmpdiUser == null) {
            return false;
        }
        int extAttr3 = parseIntOrDefault(cmmpdiUser.getExtensionAttribute3(), -1);
        // Only update eligible non-mailbox entries (extensionAttribute3 between 0 and 3)
        if (extAttr3 >= 0 && extAttr3 <= 3) {
            updateUserAttributes(exchangeUser, cmmpdiUser);
        }
        return true;
    }

    private void updateUserAttributes(CMMPExchangeLdapUser exchangeUser, CMMPDILdapUser cmmpdiUser) {
        logEntering();

        Name dn = LdapNameBuilder.newInstance(cmmpdiUser.getDn()).build();
        logger.info("Going to update user attributes for smtp='{}', DN='{}'...", cmmpdiUser.getEmail(), dn);

        List<ModificationItem> mods = new ArrayList<>();

        String srcDisplayName = exchangeUser.getDisplayName();
        String dstDisplayName = StringUtils.trimToEmpty(cmmpdiUser.getDisplayName());

        if (dstDisplayName.isEmpty() && StringUtils.isNotEmpty(srcDisplayName)) {
            logger.info("Detected display name difference, CMMP='{}', while CMMP-DI one is empty", srcDisplayName);
            mods.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("displayName", srcDisplayName)));
        } else if (srcDisplayName != null && !Objects.equals(srcDisplayName, dstDisplayName)) {
            logger.info("Detected display name difference, CMMP='{}', while CMMP-DI='{}'", srcDisplayName, cmmpdiUser.getDisplayName());
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("displayName", srcDisplayName)));
        }

        int exchangeExt1 = parseIntOrDefault(exchangeUser.getExtensionAttribute1(), -1);
        int cmmpdiExt1 = parseIntOrDefault(cmmpdiUser.getExtensionAttribute1(), -1);

        if (exchangeExt1 != cmmpdiExt1) {
            logger.info("Detected ExtensionAttribute1 difference, CMMP='{}', while CMMP-DI='{}'",
                    exchangeUser.getExtensionAttribute1(), cmmpdiUser.getExtensionAttribute1());
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    new BasicAttribute("extensionAttribute1", exchangeUser.getExtensionAttribute1())));
        }

        if (mods.isEmpty()) {
            logger.info("No difference is detected for user object '{}'", exchangeUser.getMail());
            return;
        }

        mods.forEach(mod -> logger.info(mod.toString()));
        if (!executionDryrun) {
            ldapTemplate.modifyAttributes(dn, mods.toArray(new ModificationItem[0]));
        }
    }

    // -------------------------------------------------------------------------
    // Shared Helpers
    // -------------------------------------------------------------------------

    private CMMPDILdapUser transformToCMMPDIUser(CMMPExchangeLdapUser src, CMMPDILdapUser existing) throws InvalidNameException {
        logEntering();

        CMMPDILdapUser target = (existing != null)
                ? existing
                : cmmpdiLdapCacheService.getCMMPDIUserDnBySMTP(src.getMail());

        if (target == null) {
            target = new CMMPDILdapUser();
        }

        target.setDn(new LdapName(src.getDn()));
        target.setDisplayName(src.getDisplayName());
        target.setSurname(src.getSn());
        target.setFirstName(src.getGivenName());
        target.setCommonName(src.getCn());
        target.setDistinguishedName(src.getDistinguishedName());
        target.setEmail(src.getMail());
        target.setTelephoneNumber(src.getTelephoneNumber());
        target.setPhysicalDeliveryOfficeName(src.getPhysicalDeliveryOfficeName());
        target.setTitle(src.getTitle());

        if (src.getProxyAddresses() != null && src.getProxyAddresses().length > 0) {
            String _buffer = String.join(",", src.getProxyAddresses());
            _buffer = _buffer.replaceAll("SMTP:", "");
            _buffer = _buffer.replaceAll("smtp:", "");
            target.setProxyAddresses(_buffer);
        }
        if (src.getUserCertificate() != null) {
            target.setUserCert(src.getUserCertificate());
        }
        if (src.getUserCertificate() != null) {
            target.setgCert(src.getUserCertificate());
        }

        target.setExtensionAttribute1(String.valueOf(src.getExtensionAttribute1()));
        target.setExtensionAttribute3(String.valueOf(src.getExtensionAttribute3()));
        target.setExtensionAttribute7(String.valueOf(src.getExtensionAttribute7()));

        return target;
    }

    private DirContextAdapter buildBaseUserContext(Name dn, String cn, @NotNull CMMPDILdapUser user) {
        logger.debug("Generate DN = {}", dn);

        DirContextAdapter context = new DirContextAdapter(dn);
        context.setUpdateMode(false);
        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "top");
        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "person");
        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "organizationalPerson");
        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "inetOrgPerson");
        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "user");

        context.setAttributeValue(Constants.LDAP_FIELD_CN, cn);
        context.setAttributeValue(Constants.LDAP_FIELD_SN, user.getSurname());
        context.setAttributeValue(Constants.LDAP_FIELD_GIVEN_NAME, user.getFirstName());
        context.setAttributeValue(Constants.LDAP_FIELD_DISPLAY_NAME, user.getDisplayName());
        context.setAttributeValue(Constants.LDAP_FIELD_MAIL, user.getEmail());
        context.setAttributeValue(Constants.LDAP_FIELD_EXT1, user.getExtensionAttribute1());
        context.setAttributeValue(Constants.LDAP_FIELD_EXT3, user.getExtensionAttribute3());

        context.setAttributeValue(Constants.LDAP_FIELD_PROXY_ADDRESS, user.getProxyAddresses());

        context.setAttributeValue(Constants.LDAP_FIELD_HIDEFROMADDRESSLISTS, user.getHideFromAddressLists() );

        context.setAttributeValue(Constants.LDAP_FIELD_USERACCOUNTCONTROL, "514");

        if ( user.getgCert() != null ) {
            context.setAttributeValue(Constants.LDAP_FIELD_GCERT, toBase64Cert(user.getgCert()));
        }

        if ( user.getUserCert() != null ) {
            context.setAttributeValue(Constants.LDAP_FIELD_ECERT, toBase64Cert(user.getUserCert()));
        }

        return context;
    }


    public static String toBase64Cert(byte[] derBytes) {
        if (derBytes == null || derBytes.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(derBytes);
    }

    private boolean bindAndCacheUser(DirContextAdapter context, String email, boolean skipIfExists) {
        logger.debug("Binding LDAP user context: {}", context);
        if (executionDryrun) {
            return true;
        }

        try {
            ldapTemplate.bind(context);
            cmmpdiLdapCacheService.addUserCache(email);
            return true;
        } catch (NameAlreadyBoundException | UncategorizedLdapException e) {
            if (skipIfExists && e.getMessage() != null && e.getMessage().contains("already exists")) {
                logger.warn("User {} already exists in target LDAP, skipping creation.", context.getDn());
                return false;
            }
            logger.error("Failed to bind user {}: {}", context.getDn(), e.getMessage(), e);
            return !skipIfExists;
        } catch (Exception e) {
            logger.error("Failed to bind user {}: {}", context.getDn(), e.getMessage(), e);
            return false;
        }
    }

    private void dumpLdifIfDebug(String filePath, DirContextAdapter context) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        try {
            Files.writeString(Paths.get(filePath), LdifUtils.toLdif(context));
        } catch (NamingException | IOException e) {
            logger.warn("Failed to write debug LDIF to {}: {}", filePath, e.getMessage());
        }
    }

    private String getLocalPart(String smtpAddress) {
        String trimmed = StringUtils.trimToNull(smtpAddress);
        if (trimmed == null) {
            return null;
        }

        try {
            InternetAddress address = new InternetAddress(trimmed);
            String parsed = address.getAddress();
            int atIndex = parsed != null ? parsed.indexOf('@') : -1;
            if (atIndex > 0) {
                return parsed.substring(0, atIndex).toLowerCase(Locale.ROOT);
            }
            logger.warn("SMTP address '{}' does not contain a valid local part", smtpAddress);
        } catch (AddressException ae) {
            logger.error("Cannot find local part from '{}'", smtpAddress, ae);
        }
        return null;
    }

    private String[] extractOus(String dnString) throws InvalidNameException {
        LdapName dn = new LdapName(dnString);
        List<String> ouList = new ArrayList<>();

        for (int i = dn.size() - 1; i >= 0; i--) {
            Rdn rdn = dn.getRdn(i);
            if ("OU".equalsIgnoreCase(rdn.getType())) {
                String value = rdn.getValue().toString();
                ouList.add("Users".equalsIgnoreCase(value) ? "User" : value);
            }
        }
        return ouList.toArray(new String[0]);
    }

    private static int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void logEntering() {
        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());
    }
}