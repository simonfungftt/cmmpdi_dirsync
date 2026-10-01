package com.ctg.innovic.cmmpdi.dirsync.utils;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import org.springframework.ldap.core.DirContextAdapter;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.List;

public class LdifUtils {

    public static String toLdif(DirContextAdapter adapter) throws NamingException {
        String dn = adapter.getDn().toString();
        Attributes attrs = adapter.getAttributes();

        List<Attribute> unboundIdAttrs = new ArrayList<>();
        NamingEnumeration<? extends javax.naming.directory.Attribute> attrEnum = attrs.getAll();

        while (attrEnum.hasMore()) {
            javax.naming.directory.Attribute jndiAttr = attrEnum.next();
            String attrId = jndiAttr.getID();

            List<byte[]> byteValues = new ArrayList<>();
            List<String> stringValues = new ArrayList<>();

            NamingEnumeration<?> values = jndiAttr.getAll();
            while (values.hasMore()) {
                Object val = values.next();
                if (val instanceof byte[]) {
                    byteValues.add((byte[]) val);
                } else if (val != null) {
                    stringValues.add(val.toString());
                }
            }

            if (!byteValues.isEmpty()) {
                byte[][] rawValues = byteValues.toArray(new byte[0][]);
                unboundIdAttrs.add(new Attribute(attrId, rawValues));
            } else {
                unboundIdAttrs.add(new Attribute(attrId, stringValues));
            }
        }

        Entry entry = new Entry(dn, unboundIdAttrs);
        return String.join(System.lineSeparator(), entry.toLDIF());
    }
}