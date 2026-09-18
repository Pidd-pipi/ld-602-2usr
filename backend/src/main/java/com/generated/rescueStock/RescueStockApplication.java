package com.generated.rescueStock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

// 预占闭环运行在服务内存仓库之上（进程内单实例），不连接外部 DataSource，
// 因此排除 JPA/DataSource 自动装配，避免缺少 JDBC 驱动时启动失败。
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class RescueStockApplication {
  public static void main(String[] args) {
    SpringApplication.run(RescueStockApplication.class, args);
  }
}
