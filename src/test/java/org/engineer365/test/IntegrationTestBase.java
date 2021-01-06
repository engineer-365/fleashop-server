/*
 * MIT License
 *
 * Copyright (c) 2020 engineer365.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.engineer365.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.engineer365.common.rest.FeignConfig;
import org.engineer365.common.rest.FeignConfigProps;
import org.testcontainers.containers.DockerComposeContainer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * 集成测试的基类。
 *
 * 和Spring Boot (https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-testing)
 * 里描述的做法不一样，我们使用容器化测试的方式做集成测试，容器化测试的主要优点是待测试目标和实际环境几乎一致
 *
 * 1）使用testcontainers的docker-compose模块在docker里启动启动服务器进程和相关的MySQL等。testcontainers的一个缺点是依赖于原生的控制
 *    docker，而且需要root权限启动，这使得将来在k8s环境中运行集成测试变得困难，所以将来可能会改成podman等别的容器方案
 * 2）测试过程以实际的API调用为中心，不使用任何mock。包括数据准备工作，也是由各个测试案例自己通过调用API完成
 *
 * 这个基类做了以下事情：
 * 1）基于对API接口规范的约定封装了一些浅层次的对RestAssured的使用方法。后续会改为使用Open Feign。
 * 2) 连接testcontainers启动的待测试容器
 * 3）初始化数据：MySQL, ...
 *
 * @Param T - feign client class
 */
@lombok.Getter
@lombok.Setter
public abstract class IntegrationTestBase<T> {

  T apiClient;

  /**
   * 读取测试用系统环境变量
   */
  static Properties ENV = new Properties();
  static {
    try (var fis = new FileInputStream("dev/fleashop.env");) {
      ENV.load(fis);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /** RESTful API的基础路径 */
  final String basePath;

  protected IntegrationTestBase(Class<T> clientClass, String basePath) {
    this.basePath = basePath;
    
    this.apiClient = createApiClient(clientClass);
  }

  public T createApiClient(Class<T> clientClass) {
    var feign = createFeignConfig();
  
    String uri = String.format("http://%s:%d", getServerHost(), getServerPort());
    return feign.build(clientClass, uri);
  }

  public FeignConfig createFeignConfig() {
    var r = new FeignConfig();

    r.setProps(new FeignConfigProps());
    r.init();

    return r;
  }

  public TestContainersFactory containersFactory() {
    return TestContainersFactory.DEFAULT;
  }

  protected abstract DockerComposeContainer<?> containers();

  public String getServerHost() {
    return containers().getServiceHost("server", containersFactory().getServerPort());
  }

  public int getServerPort() {
    return containers().getServicePort("server", containersFactory().getServerPort());
  }

  public int getServerManagementPort() {
    return containers().getServicePort("server", containersFactory().getServerManagePort());
  }

  public String getMySQLHost() {
    return containers().getServiceHost("mysql", containersFactory().getMySQLPort());
  }

  public int getMySQLPort() {
    return containers().getServicePort("mysql", containersFactory().getMySQLPort());
  }

  public DataSource getDataSource() {
    var cfg = new HikariConfig();

    cfg.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?%s",
        getMySQLHost(), getMySQLPort(),
        ENV.getProperty("MYSQL_DATABASE"), ENV.getProperty("MYSQL_JDBC_OPTIONS")));
    cfg.setUsername(ENV.getProperty("MYSQL_USER"));
    cfg.setPassword(ENV.getProperty("MYSQL_PASSWORD"));

    return new HikariDataSource(cfg);
  }

  public void truncateTable(String tableName) {
    try (var conn = getDataSource().getConnection(); var stmt = conn.createStatement();) {
      stmt.executeUpdate("truncate table " + tableName);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  

}
