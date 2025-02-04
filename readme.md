# HMCL插件加载器

基于PF4J、ASM实现的HMCL插件加载器

## 特性

1. 简单的插件加载方式
2. 插件能通过ASM修改HMCL字节码
3. 可执行文件能够正常同步更新HMCL版本

## 原理

HMCL的Windows可执行文件版本分为exe文件头部分与jar文件内容部分</br>
EXE文件头通过执行`java -jar HMCL.exe`来执行jar文件内容部分</br>
通过自定义PF4J的ClassLoader实现了字节码增强功能</br>
将HMCL本体注册为PF4J插件, 在插件主类加载过程中配置字节码处理类</br>
PF4J再启动插件, 插件可以继续通过ASM修改启动器的字节码

## 编译

执行`./gradlew makeExecutables`</br>
编译后的可执行文件路径：`loader/build/libs/loader.exe`

注：编译需要访问Github下载EXE可执行文件外壳</br>
可以手动下载后放入`loader/build/tmp/makeExecutables/HMCLauncher.exe`

## 使用方式

目前硬编码插件加载路径为运行目录下`.minecraft/plugins`</br>
创建插件文件夹，将`HMCL.jar`与插件一同放入后，启动EXE可执行文件即可