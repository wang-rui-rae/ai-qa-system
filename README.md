# **IDEA如何识别多module的项目成maven项目**
  * 把maven图标调出来 ALT+8>出现在IDEA的右侧边框
  * 这个时候就会把项目识别成maven了，更新依赖

# **在子module的启动类上出现【Cannot access org.springframework.context.ConfigurableApplicationContext】错误**
  * 这个错误时，通常是由于 模块化问题 或 依赖冲突 导致的，特别是当你使用 Java 9 或更高版本，或者项目依赖版本不匹配时。
根据你提供的要求，我将按照你给定的顺序，为你生成 Markdown 格式的代码和文字内容。

```java
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
````

-----

  * 问题排查1：
  * 检查并统一 Spring 依赖版本：


  * 问题排查1：
  * 检查并统一 Spring 依赖版本：
    * 如果你使用 Maven，确保 pom.xml 中所有 spring- 开头的依赖版本是统一的。最好使用 Spring Boot 的父级 POM (spring-boot-starter-parent) 来管理版本，因为它会自动处理所有兼容的依赖版本。
    * 检查所有子模块的依赖，确保它们没有覆盖或引入不兼容的 Spring 版本。
    * 运行 mvn dependency:tree 命令来查看项目的依赖树，找出任何版本冲突。
  
    ****************
    * 运行 mvn dependency:tree 命令
      * 出现问题如下：
      * [ERROR] Error executing Maven.
      * [ERROR] The specified user settings file does not exist: C:\Users\lnwdhboss\.m2\settings.xml
      * 解决：只需要追加一个\.m2\settings.xml文件即可
    ****************
      
  * 清除 Maven 缓存并重新加载项目：
    * 在命令行中运行 mvn clean install。
    * 在 IDEA 中，打开 Maven 工具窗口，点击 Reimport All Maven Projects 图标。
  
  * 解决：排查了依赖，最终发现是自己画蛇添足修改了 File > Project Structure...导致的，直接让idea识别即可，不用自己追加。
-----

# **Docker desktop安装之问题整理**
  * 需要配置的项目如下：
    1. 根据电脑的版本下载对应的docker
       就比如说windows有ARM64/AMD64
    2. 需要进行虚拟化的设置
       2.1 BIOS/UEFI 菜单中，找到与虚拟化相关的设置
           需要根据不同的主板来找到如何设定这个菜单
       2.2 启用 Hyper-V 和 WSL

    虽然做了上边的这些，但是我依然还没有解决这个问题：
    Virtualization support not detected
    Docker Desktop couldn’t start as virtualization support is not enabled on your machine.
    We’re piloting a new cloud-based solution to address this issue. If you’d like to try it out, join the Beta program.
-----

# **Mysql 安装之问题整理**
* 问题1：
    * 版本 ：
          * 社区版8.0.43.0
    * 问题 ：
          * 通过可视化工具（如DBeaver）来链接数据库的时候，出现Public Key Retrieval is not allowed问题
    * 原因 : 
          * MySQL 8.0 默认使用 caching_sha2_password 作为认证插件，它比旧的 mysql_native_password 更安全。但是，很多旧的客户端或驱动程序并不支持这个新的认证方式，导致连接失败。
    * 解决 ：
          * 在 DBeaver 中配置连接参数（推荐）
          * 这是最简单、最安全的解决方案，你不需要对服务器做任何更改。
          * 在 DBeaver 中，编辑你现有的 MySQL 连接。
          * 进入 "驱动属性" (Driver properties) 选项卡。
          * 在列表中找到并双击 allowPublicKeyRetrieval 属性。
          * 将值从 false 改为 true。
          * allowPublicKeyRetrieval 的作用是允许客户端从服务器获取公钥，从而使用新的安全认证方式。
          * 保存并重新测试连接。

* 问题2：
*  项目启动的时候出现：
*     Caused by: java.sql.SQLException: No database selected
*  解决：
*     建sechma：CREATE DATABASE 【ai_qa_system】;

