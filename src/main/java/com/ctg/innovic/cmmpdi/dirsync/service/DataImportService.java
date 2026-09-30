package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.dto.ExchangeADUser;
import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataImportService {

    private static Logger logger = LogManager.getLogger(DataImportService.class);


    private UserService userService;


    public void importUserData(String fileName) {

        try {

            logger.info("Start to import directory information into target LDAP...");

            List<ExchangeADUser> _userList = readUserDataFromCsv(fileName);

            for  ( ExchangeADUser _user : _userList ) {

                logger.info("Going to create in internal LDAP for _user.getMail() " + _user.getMail());

                userService.createUser( _user.getMail() );
            }
        }
        catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }


    private List<ExchangeADUser> readUserDataFromCsv(String fileName) throws FileNotFoundException {

        List<ExchangeADUser> _users = new ArrayList<>();

        try (Reader reader = new BufferedReader(new FileReader(fileName))) {
            CsvToBean<ExchangeADUser> csvToBean = new CsvToBeanBuilder(reader)
                    .withType(ExchangeADUser.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            _users = csvToBean.parse();

            for (ExchangeADUser _user : _users) {

                Gson gson = new GsonBuilder().setFormattingStyle(FormattingStyle.COMPACT).create();

                //logger.debug( gson.toJson(_user) );
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return _users;
    }


}
