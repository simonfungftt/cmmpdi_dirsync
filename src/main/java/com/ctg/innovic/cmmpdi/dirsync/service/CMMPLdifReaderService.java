package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeContainer;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeOrganizationalUnit;
import com.ctg.innovic.cmmpdi.dirsync.utils.LdifCleaner;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CMMPLdifReaderService {

    private static Logger logger = LogManager.getLogger(CMMPLdifReaderService.class);

    @Autowired
    private CMMPCacheService cmmpCacheService;


    private static final Pattern LDIF_KEY_VALUE_PATTERN =
            Pattern.compile("^(?!ref:|search:|result:|#)[a-zA-Z][a-zA-Z0-9-]*::?\\s*.*$");



    public CMMPExchangeContainer parseLdifFile(String ldifFilePath) throws IOException {

        try {

            Path path = Paths.get(ldifFilePath);

            String content = Files.readString(path, StandardCharsets.UTF_8);
            content = LdifCleaner.clean(content);
            content = cleanLdifString(content);

            Path filePath = Path.of(ldifFilePath + ".bak");
            Files.writeString(filePath, content);

            return this.parseLdif(content);
        }
        catch(RuntimeException re) {
            logger.error("Exception in processing file " + ldifFilePath + ", " + re.getMessage());
            throw re;
        }
    }


    private CMMPExchangeContainer parseLdif(String ldifContent) {
        CMMPExchangeContainer container = new CMMPExchangeContainer();

        try (InputStream inputStream = new ByteArrayInputStream(ldifContent.getBytes(StandardCharsets.UTF_8));

             LDIFReader ldifReader = new LDIFReader(inputStream)) {


            Entry entry;

            int _userCount = 1;
            int _groupCount = 1;

            while ((entry = ldifReader.readEntry()) != null) {

                try {

                    // Determine object type based on objectClass values
                    String[] objectClasses = entry.getAttributeValues("objectClass");
                    boolean isUser = objectClasses != null && Arrays.asList(objectClasses).contains("user");
                    boolean isGroup = objectClasses != null && Arrays.asList(objectClasses).contains("group");
                    boolean isOu = objectClasses != null && Arrays.asList(objectClasses).contains("organizationalUnit");

                    if (isUser) {
                        logger.debug("Scanned " + _userCount++ + " users");
                        container.getUsers().add(mapToUser(entry));
                    } else if (isGroup) {
                        logger.debug("Scanned " + _groupCount++ + " groups");
                        container.getGroups().add(mapToGroup(entry));
                    } else if (isOu) {
                        container.getOrganizationalUnits().add(mapToOu(entry));
                    }
                }
                catch (Exception e) {
                    logger.error("Exception in parseLdif, " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading LDIF into memory: " + e.getMessage(), e);
        }

        return container;
    }


    private CMMPExchangeOrganizationalUnit mapToOu(Entry entry) {
        CMMPExchangeOrganizationalUnit ou = new CMMPExchangeOrganizationalUnit();
        ou.setDn(entry.getDN());
        ou.setOu(entry.getAttributeValue("ou"));
        ou.setObjectGUID(getBytes(entry, "objectGUID"));

        // Multi-valued fields
        ou.setObjectClasses(getValuesList(entry, "objectClass"));
        ou.setOuPaths(getValuesList(entry, "ouPath"));

        return ou;
    }

    /**
     * Parses LDIF text content directly into in-memory Model objects.
     */
    private CMMPExchangeLdapUser mapToUser(Entry entry) {

//        for ( Attribute _each : entry.getAttributes() ) {
//            logger.debug(_each.getName() + " " + _each.getValue());
//        }

        CMMPExchangeLdapUser user = new CMMPExchangeLdapUser();
        user.setDn(entry.getDN());
        user.setCn(entry.getAttributeValue("cn"));
        user.setSn(entry.getAttributeValue("sn"));
        user.setGivenName(entry.getAttributeValue("givenName"));
        user.setTitle(entry.getAttributeValue("title"));
        user.setDisplayName(entry.getAttributeValue("displayName"));
//        user.setSAMAccountName(entry.getAttributeValue("sAMAccountName"));
        user.setMail(entry.getAttributeValue("mail"));
        user.setCountryCode(Integer.valueOf(entry.getAttributeValue("countryCode")));
//        user.setUserAccountControl(entry.getAttributeValue("userAccountControl"));
//        user.setPwdLastSet(entry.getAttributeValue("pwdLastSet"));

        // Binary and Certificate fields loaded directly into byte arrays
//        user.setObjectGUID(getBytes(entry, "objectGUID"));
//        user.setObjectSid(getBytes(entry, "objectSid"));
//        user.setUserPassword(getBytes(entry, "userPassword"));
//        user.setUserCert17(getBytes(entry, "userCert17"));
//        user.setUserCert18(getBytes(entry, "userCert18"));

        // Multi-valued fields
//        user.setObjectClasses(getValuesList(entry, "objectClass"));
//        user.setOuPaths(getValuesList(entry, "ouPath"));

        return user;
    }



    private CMMPExchangeLdapGroup mapToGroup(Entry entry) {
//
//                for ( Attribute _each : entry.getAttributes() ) {
//            logger.debug(_each.getName() + " " + _each.getValue());
//        }

        CMMPExchangeLdapGroup group = new CMMPExchangeLdapGroup();

        group.setDn(entry.getDN());
        group.setCn(entry.getAttributeValue("cn"));
        group.setDisplayName(entry.getAttributeValue("displayName"));
        group.setMail(entry.getAttributeValue("mail"));

        if ( entry.getAttributeValue("extensionAttribute1") != null )
            group.setExtensionAttribute1(Integer.valueOf(entry.getAttributeValue("extensionAttribute1")));

        if ( entry.getAttributeValue("extensionAttribute3") != null )
            group.setExtensionAttribute3(Integer.valueOf(entry.getAttributeValue("extensionAttribute3")));
//        group.setExtensionAttribute5(Integer.valueOf(entry.getAttributeValue("extensionAttribute5")));
//        group.setExtensionAttribute6(Integer.valueOf(entry.getAttributeValue("extensionAttribute6")));
//        group.setExtensionAttribute7(Integer.valueOf(entry.getAttributeValue("extensionAttribute7")));
//        group.setExtensionAttribute10(Integer.valueOf(entry.getAttributeValue("extensionAttribute10")));

        Attribute memberAttr = entry.getAttribute("member");
        if (memberAttr != null) {

            for ( String _member : Arrays.asList(memberAttr.getValues())) {
                if ( this.cmmpCacheService.lookupSmtpByCMMPDN( _member) != null ) {
                    group.getMemberDNs().add(this.cmmpCacheService.lookupSmtpByCMMPDN(_member));
                }
            }
        }

        return group;
    }



    private byte[] getBytes(Entry entry, String attributeName) {
        Attribute attr = entry.getAttribute(attributeName);
        return attr != null ? attr.getValueByteArray() : null;
    }


    private java.util.List<String> getValuesList(Entry entry, String attributeName) {
        Attribute attr = entry.getAttribute(attributeName);
        return attr != null ? Arrays.asList(attr.getValues()) : java.util.Collections.emptyList();
    }


    public static String cleanLdifString(String input) {
        return input.lines()
                .map(String::trim)
                .filter(line -> line.isEmpty() || LDIF_KEY_VALUE_PATTERN.matcher(line).matches())
                .collect(Collectors.joining("\n"));
    }

}