支持加星加关注！代码不长，也是花费了我一上午的时间用来调试

# clear-unused-images-in-markdowns

需求：在写markdown笔记的时候，图片附件无法随着文本级联删除，这将会导致废弃的图片越来越多且难以清理

设计一个Java程序：
- 指定目标文件夹DIRECTORY_PATH，与回收站文件夹UNUSED_IMAGES_DIR
- 递归遍历DIRECTORY_PATH下所有的markdown文档 ，并解析所有md标准的图片文本`![]()`，对应的regex表达式为：`"!\\[.*?\\]\\((.*?)\\)"`
- 递归遍历DIRECTORY_PATH下所有的图片附件，对应的regex表达式为：`".png", ".jpg", ".jpeg", ".gif", ".bmp", ".svg"`
- 扩展点：如何排除扫描目录下的图片附件：EXCLUDE_IMAGES_DIR目录和UNUSED_IMAGES_DIR目录
- 通过比较图片名称来计算出未被md文档引用的图片，非永久删除，清理这部分文件至UNUSED_IMAGES_DIR

就这么一个小功能，我搜遍了全网都没找到好用的，要么写的很粗糙，要么用python/js等脚本语言写的，我自己难以维护，要么用的三方插件难以定制化

索性自己写个Java版本的
# 使用方式[UnusedImagesFinder.java](src%2Fmain%2Fjava%2Forg%2Flyflexi%2Fclearunusedimagesinmarkdowns%2FUnusedImagesFinder.java)
路径配置
```java
// 设置扫描根路径
private static final String DIRECTORY_PATH = "E:\\github\\vsNotes";
// 排除附件目录
private static final String EXCLUDE_IMAGES_DIR = "E:\\github\\vsNotes\\appendix-drawio";
//回收站
private static final String UNUSED_IMAGES_DIR = "E:\\github\\vsNotes\\unused-images";
```
运行[UnusedImagesFinder.java](src%2Fmain%2Fjava%2Forg%2Flyflexi%2Fclearunusedimagesinmarkdowns%2FUnusedImagesFinder.java)来看效果：

```shell
C:\Users\hasee\.jdks\corretto-17.0.9\bin\java.exe "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA 2024.1.1\lib\idea_rt.jar=55690:C:\Program Files\JetBrains\IntelliJ IDEA 2024.1.1\bin" -Dfile.encoding=UTF-8 -classpath E:\github\clear-unused-images-in-markdowns\target\classes;C:\Users\hasee\.m2\repository\org\springframework\boot\spring-boot-starter\3.2.1\spring-boot-starter-3.2.1.jar;C:\Users\hasee\.m2\repository\org\springframework\boot\spring-boot\3.2.1\spring-boot-3.2.1.jar;C:\Users\hasee\.m2\repository\org\springframework\spring-context\6.1.2\spring-context-6.1.2.jar;C:\Users\hasee\.m2\repository\org\springframework\spring-aop\6.1.2\spring-aop-6.1.2.jar;C:\Users\hasee\.m2\repository\org\springframework\spring-beans\6.1.2\spring-beans-6.1.2.jar;C:\Users\hasee\.m2\repository\org\springframework\spring-expression\6.1.2\spring-expression-6.1.2.jar;C:\Users\hasee\.m2\repository\io\micrometer\micrometer-observation\1.12.1\micrometer-observation-1.12.1.jar;C:\Users\hasee\.m2\repository\io\micrometer\micrometer-commons\1.12.1\micrometer-commons-1.12.1.jar;C:\Users\hasee\.m2\repository\org\springframework\boot\spring-boot-autoconfigure\3.2.1\spring-boot-autoconfigure-3.2.1.jar;C:\Users\hasee\.m2\repository\org\springframework\boot\spring-boot-starter-logging\3.2.1\spring-boot-starter-logging-3.2.1.jar;C:\Users\hasee\.m2\repository\ch\qos\logback\logback-classic\1.4.14\logback-classic-1.4.14.jar;C:\Users\hasee\.m2\repository\ch\qos\logback\logback-core\1.4.14\logback-core-1.4.14.jar;C:\Users\hasee\.m2\repository\org\apache\logging\log4j\log4j-to-slf4j\2.21.1\log4j-to-slf4j-2.21.1.jar;C:\Users\hasee\.m2\repository\org\apache\logging\log4j\log4j-api\2.21.1\log4j-api-2.21.1.jar;C:\Users\hasee\.m2\repository\org\slf4j\jul-to-slf4j\2.0.9\jul-to-slf4j-2.0.9.jar;C:\Users\hasee\.m2\repository\jakarta\annotation\jakarta.annotation-api\2.1.1\jakarta.annotation-api-2.1.1.jar;C:\Users\hasee\.m2\repository\org\springframework\spring-core\6.1.2\spring-core-6.1.2.jar;C:\Users\hasee\.m2\repository\org\springframework\spring-jcl\6.1.2\spring-jcl-6.1.2.jar;C:\Users\hasee\.m2\repository\org\yaml\snakeyaml\2.2\snakeyaml-2.2.jar;C:\Users\hasee\.m2\repository\org\slf4j\slf4j-api\2.0.9\slf4j-api-2.0.9.jar org.lyflexi.clearunusedimagesinmarkdowns.UnusedImagesFinder
21:02:03.791 [main] INFO org.lyflexi.clearunusedimagesinmarkdowns.UnusedImagesFinder -- 图片名称：1725879702314.png，图片原始路径：E:\github\vsNotes\git\image\git环境\1725879702314.png，已经被移动到指定目录：E:\github\vsNotes\unused-images\1725879702314.png
21:02:03.797 [main] INFO org.lyflexi.clearunusedimagesinmarkdowns.UnusedImagesFinder -- 图片名称：1725879726210.png，图片原始路径：E:\github\vsNotes\git\image\git环境\1725879726210.png，已经被移动到指定目录：E:\github\vsNotes\unused-images\1725879726210.png
21:02:03.798 [main] INFO org.lyflexi.clearunusedimagesinmarkdowns.UnusedImagesFinder -- 图片名称：R-(1).png，图片原始路径：E:\github\vsNotes\jdk\Java设计模式\image\regex\R-(1).png，已经被移动到指定目录：E:\github\vsNotes\unused-images\R-(1).png
21:02:03.798 [main] INFO org.lyflexi.clearunusedimagesinmarkdowns.UnusedImagesFinder -- 所有未被引用的图片已移动到E:\github\vsNotes\unused-images,总移动数量：3

Process finished with exit code 0

```


