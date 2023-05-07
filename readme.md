# HMCL插件加载器

## 原理

HMCL的exe版本更新只修改exe文件内嵌的jar文件内容</br>
可以通过修改exe挂载javaagent, 在不修改exe文件内嵌jar文件的前提下保证正常更新</br>
javaagent挂载后通过asm修改启动类, 挂载pf4j插件系统</br>
pf4j加载插件, 插件可以继续通过asm修改启动器的字节码

## 使用方式

编译agent模块获取`agent/build/lib/agent.jar`</br>
将agent.jar放入`HMCL同级目录`</br>
编译example模块获取`example/build/lib/example-1.0-SNAPSHOT.jar`</br>
将example-1.0-SNAPSHOT.jar放入`HMCL同级目录/.minecraft/plugins`中</br>
通过命令行添加参数启动HMCL
```shell
java -javaagent:agent.jar -jar HMCL-3.5.3.230.exe
```

## 待处理

1. 提供支持自动javaagent挂载的HMCL可执行文件
2. 通过读取配置文件获取`.minecraft`的正确路径
