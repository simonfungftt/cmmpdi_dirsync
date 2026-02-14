package com.ctg.innovic.cmmpdi.dirsync;

import com.ctg.innovic.cmmpdi.dirsync.service.DataImportService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.ctg.innovic.cmmpdi.dirsync.service"})
public class DirsyncApplication {

	private static Logger logger =  LogManager.getLogger(DirsyncApplication.class);

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(DirsyncApplication.class);
		application.setBannerMode(Banner.Mode.OFF);
		application.run(args);

////		ApplicationContext _context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
//
//		UserService _userService = new UserService();
//
//		List<Person> _list = _userService.listUsers();
//
//		for(Person _p : _list) {
//
//			Gson gson = new GsonBuilder().setPrettyPrinting().create();
//
//			logger.info( gson.toJson(_p) );
//		}

		DataImportService _serivce = new DataImportService();

		_serivce.importUserData("C:\\Tmp\\DPO_activeDirectoryUser_initial_e.csv");

	}

}
