# HMCL插件加载器

基于PF4J、JavaAgent实现的HMCL插件加载器

## 特性

1. 简单的插件加载方式
2. 插件能通过asm修改HMCL字节码
3. 可执行文件能够正常同步更新HMCL版本

## 原理

HMCL的Windows可执行文件版本分为exe文件头部分与jar文件内容部分</br>
exe文件头通过执行`java -jar HMCL.exe`来执行jar文件内容部分</br>
而版本更新功能属于jar文件内容部分且只检查并修改jar文件内容部分</br>
所以说通过修改exe挂载javaagent, 既可实现内嵌jar文件正常更新</br>
javaagent挂载后通过asm修改启动类, 挂载pf4j插件系统</br>
pf4j再加载插件, 插件可以继续通过asm修改启动器的字节码

## 编译过程

编译使用Github Action, 目前仅提供手动触发</br>
1. 在项目Action页面左边菜单中找到并选择编译任务
2. 在编译任务页面找到`Run workflow`点击并选择分支
3. 编译任务克隆HMCL官方仓库源码
4. 将`patch`文件夹中的补丁应用到官方仓库
5. 配置编译环境并编译`HMCLauncher.exe`可执行文件壳
6. 将编译完成的可执行文件壳拷贝到本项目指定位置
7. 执行`./gradlew packageDistDir`
8. gradle通过maven仓库下载最新版本HMCL构建并与之前准备好的可执行文件头拼接
9. 编译插件加载器与例子插件并与可执行文件打包
10. 将文件上传到Action的Artifact中

## Agent单独使用方式

编译agent模块获取`agent/build/lib/agent.jar`</br>
将agent.jar放入`HMCL同级目录`</br>
编译example模块获取`example/build/lib/example-1.0-SNAPSHOT.jar`</br>
将example-1.0-SNAPSHOT.jar放入`agent.jar同级目录/plugins`中</br>
通过命令行添加参数启动HMCL
```shell
java -javaagent:agent.jar -jar HMCL-3.5.3.230.exe
```
