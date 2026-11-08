package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.unboundid.util.NotNull;

import java.time.Instant;
import java.util.List;

public interface CMMPLdapService {

    CMMPExchangeLdapGroup dipGroupBySMTP(@NotNull String pCN);

    CMMPExchangeLdapUser dipUserBySMTP(@NotNull String pCN);

    List<CMMPExchangeLdapGroup> getLastModifiedGroupUnderOU(@NotNull String pOU, @NotNull Instant pLastSyncTime);

    List<CMMPExchangeLdapGroup> getAllGroupUnderOU(@NotNull String pOU);

    List<CMMPExchangeLdapUser> getLastModifiedUserUnderOU(@NotNull String pOU, @NotNull Instant pLastSyncTime);

    List<CMMPExchangeLdapUser> getAllUserUnderOU(@NotNull String pOU);
}
