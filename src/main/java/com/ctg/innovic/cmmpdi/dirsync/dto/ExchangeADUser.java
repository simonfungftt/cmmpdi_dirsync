package com.ctg.innovic.cmmpdi.dirsync.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ExchangeADUser {

    @CsvBindByName(column = "displayName")
    private String displayName;

    @CsvBindByName(column = "mail")
    private String mail;

    public String getMail() {
        return mail;
    }
}
