package com.ctg.innovic.cmmpdi.dirsync.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

public final class LdifCleaner {

    // Regex pattern for matching Non-Breaking Spaces (\u00A0), Zero-Width Spaces, and Soft Hyphens
    private static final Pattern INVISIBLE_WHITESPACE = Pattern.compile("[\\u00A0\\u200B\\u00AD]");

    // Pattern to clean up accidental spaces trailing comma delimiters in DN entries (e.g., ", DC=")
    private static final Pattern CLEAN_DN_SPACES = Pattern.compile(",\\s+(?=[A-Za-z]+=)");

    private LdifCleaner() {
        // Private constructor for utility class
    }

    /**
     * Cleans and normalizes an raw LDIF String.
     *
     * @param rawLdif Input LDIF text containing bad wraps or non-standard spaces.
     * @return Fully normalized LDIF string adhering to standard format.
     */
    public static String clean(String rawLdif) {
        if (rawLdif == null || rawLdif.isEmpty()) {
            return rawLdif;
        }

        // Step 1: Remove invisible spaces/non-breaking spaces (e.g., \u00A0) inserted by web tools
        String sanitized = INVISIBLE_WHITESPACE.matcher(rawLdif).replaceAll("");

        // Step 2: Fix LDIF RFC-compliant line wrapping (un-fold continuous lines)
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new StringReader(sanitized))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // If a line begins with a space or tab, it is a continuation line according to RFC 2849
                if (!line.isEmpty() && (line.startsWith(" ") || line.startsWith("\t"))) {
                    // Append trimmed content directly to the previous line
                    result.append(line.substring(1));
                } else {
                    if (result.length() > 0) {
                        result.append("\n");
                    }
                    result.append(line);
                }
            }
        } catch (IOException e) {
            // StringReader won't throw IOException in real usage
            throw new RuntimeException("Error processing LDIF content", e);
        }

        // Step 3: Remove unnecessary spaces after commas in Distinguished Names (e.g., ", DC=" -> ",DC=")
        return CLEAN_DN_SPACES.matcher(result.toString()).replaceAll(",");
    }

    public static void main(String[] args) {
        // Raw LDIF snippet with line break wraps and non-breaking spaces (\u00A0)
        String malformedLdif =
                "# VMS3UATAUDB02, AUD DB, Audit, Infrastructure Services, Servers, uat.cmmp.hksa\u00A0rg\n" +
                        "dn: CN=VMS3UATAUDB02,OU=AUD DB,OU=Audit,OU=Infrastructure Services,OU=Servers,\u00A0DC=uat,DC=cmmp,DC=hksarg\n" +
                        "objectClass: top\n" +
                        "objectClass: group\n" +
                        "cn: Administrators\n" +
                        "description: Administrators have complete and unrestricted access to the compu\n" +
                        " ter/domain\n" +
                        "member: CN=IfAdminGP,OU=Groups,OU=Infrastructure Services,OU=CMMP,DC=uat,DC=cm\n" +
                        " mp,DC=hksarg";

        System.out.println("--- Original Malformed Input ---");
        System.out.println(malformedLdif);

        String cleanedLdif = LdifCleaner.clean(malformedLdif);

        System.out.println("\n--- Normalized Output ---");
        System.out.println(cleanedLdif);
    }
}