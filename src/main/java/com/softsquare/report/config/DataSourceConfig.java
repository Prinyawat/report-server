package com.softsquare.report.config;

import javax.sql.DataSource;

import com.softsquare.report.core.utils.BeanUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

@Configuration
public class DataSourceConfig extends HikariConfig{

	@Autowired
	private Environment env;

	@Primary
	@Bean(name = "primary-db")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource primaryDataSource() {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
		dataSource.setJdbcUrl(env.getProperty("spring.datasource.url"));
		dataSource.setUsername(env.getProperty("spring.datasource.username"));
		dataSource.setPassword(env.getProperty("spring.datasource.password"));
		if(BeanUtils.isNotEmpty(env.getProperty("spring.datasource.maximumPoolSize"))){
			dataSource.setMaximumPoolSize(new Integer(env.getProperty("spring.datasource.maximumPoolSize")));
		}
		return dataSource;
	}

	@Bean(name = "second-db")
	@ConfigurationProperties(prefix = "spring.second-db")
	public DataSource secondDataSource() {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName(env.getProperty("spring.second-db.driver-class-name"));
		dataSource.setJdbcUrl(env.getProperty("spring.second-db.url"));
		dataSource.setUsername(env.getProperty("spring.second-db.username"));
		dataSource.setPassword(env.getProperty("spring.second-db.password"));
		if(BeanUtils.isNotEmpty(env.getProperty("spring.second-db.maximumPoolSize"))){
			dataSource.setMaximumPoolSize(new Integer(env.getProperty("spring.second-db.maximumPoolSize")));
		}
		return dataSource;
	}

}
