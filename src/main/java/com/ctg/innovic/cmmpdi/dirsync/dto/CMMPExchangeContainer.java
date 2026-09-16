package com.ctg.innovic.cmmpdi.dirsync.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
public class CMMPExchangeContainer {

    private List<CMMPExchangeLdapUser> users = new ArrayList<>();

    private List<CMMPExchangeLdapGroup> groups = new ArrayList<>();

    private List<CMMPExchangeOrganizationalUnit> organizationalUnits = new ArrayList<>();

    public List<CMMPExchangeLdapGroup> getGroups() {
        return groups;
    }

    public List<CMMPExchangeLdapUser> getUsers() {
        return users;
    }

    public List<CMMPExchangeOrganizationalUnit> getOrganizationalUnits() {
        return organizationalUnits;
    }
}
