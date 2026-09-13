package com.ctg.innovic.cmmpdi.dirsync.dto;

import lombok.Data;
import org.springframework.ldap.odm.annotations.Attribute;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class CMMPExchangeLdapGroup {

    public CMMPExchangeLdapGroup() {
        this.memberDNs = new ArrayList<>();
    }

    private String dn;
    private String email;

    private List<String> memberDNs;
}
