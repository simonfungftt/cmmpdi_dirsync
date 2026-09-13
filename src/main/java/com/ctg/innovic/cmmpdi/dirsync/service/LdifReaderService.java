package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeContainer;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeOrganizationalUnit;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Service
public class LdifReaderService {

    private static Logger logger = LogManager.getLogger(LdifReaderService.class);


    public CMMPExchangeContainer parseLdifFile(String ldifFilePath) throws IOException {

        Path path = Paths.get(ldifFilePath);
        String content = Files.readString(path, StandardCharsets.UTF_8);

        return this.parseLdif(content);
    }

    public CMMPExchangeContainer parseLdif(String ldifContent) {
        CMMPExchangeContainer container = new CMMPExchangeContainer();

        try (InputStream inputStream = new ByteArrayInputStream(ldifContent.getBytes(StandardCharsets.UTF_8));
             LDIFReader ldifReader = new LDIFReader(inputStream)) {

            Entry entry;
            while ((entry = ldifReader.readEntry()) != null) {
                // Determine object type based on objectClass values
                String[] objectClasses = entry.getAttributeValues("objectClass");
                boolean isUser = objectClasses != null && Arrays.asList(objectClasses).contains("user");
                boolean isOu = objectClasses != null && Arrays.asList(objectClasses).contains("organizationalUnit");

                if (isUser) {
                    container.getUsers().add(mapToUser(entry));
                } else if (isOu) {
                    container.getOrganizationalUnits().add(mapToOu(entry));
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

    private byte[] getBytes(Entry entry, String attributeName) {
        Attribute attr = entry.getAttribute(attributeName);
        return attr != null ? attr.getValueByteArray() : null;
    }

    private java.util.List<String> getValuesList(Entry entry, String attributeName) {
        Attribute attr = entry.getAttribute(attributeName);
        return attr != null ? Arrays.asList(attr.getValues()) : java.util.Collections.emptyList();
    }
}