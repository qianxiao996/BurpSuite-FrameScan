## Burpsuit-FrameScan RPC Server 

## 开始使用

### 源码运行

```
//创建虚拟环境
conda create -n FrameScan-Grpc python=3.8
conda activate FrameScan-Grpc
导出requirements.txt
python -m pip freeze > requirements.txt
安装requirements.txt
python -m pip install -r requirements.txt

python rpc_server.py
python rpc_server.py 0.0.0.0:50051
```
### 二进制文件运行

#### Windwos

```
rpc_server.exe
rpc_server.exe 0.0.0.0:11122
```

其他平台请自行打包。

#### 打包exe

```
windows
nuitka --mingw64 --standalone  --onefile  rpc_server.py  --show-progress  --follow-imports  --include-package=argparse
```
编译选项

```
--nofollow-imports ：不编译代码中所有的import，比如keras，numpy之类的。
--mingw64 #默认为已经安装的vs2017去编译，否则就按指定的比如mingw(官方建议)
--standalone 独立环境，这是必须的(否则拷给别人无法使用)
--windows-disable-console 没有CMD控制窗口
--output-dir=out 生成exe到out文件夹下面去
--show-progress 显示编译的进度，很直观
--show-memory 显示内存的占用
--include-qt-plugins=sensible,styles 打包后PyQt的样式就不会变了
--plugin-enable=qt-plugins 需要加载的PyQt插件
--plugin-enable=tk-inter 打包tkinter模块的刚需
--plugin-enable=numpy 打包numpy,pandas,matplotlib模块的刚需
--plugin-enable=torch 打包pytorch的刚需
--plugin-enable=tensorflow 打包tensorflow的刚需
--windows-icon-from-ico=你的.ico 软件的图标
--windows-company-name=Windows下软件公司信息
--windows-product-name=Windows下软件名称
--windows-file-version=Windows下软件的信息
--windows-product-version=Windows下软件的产品信息
--windows-file-description=Windows下软件的作用描述
--windows-uac-admin=Windows下用户可以使用管理员权限来安装
--linux-onefile-icon=Linux下的图标位置
--onefile 像pyinstaller一样打包成单个exe文件
--include-package=复制比如numpy,PyQt5 这些带文件夹的叫包或者轮子
--include-module=复制比如when.py 这些以.py结尾的叫模块
```



## Java GRP代码生成


### 1、软件安装
下载protoc.exe 工具 ， 选择 win64.zip 下载地址：https://github.com/protocolbuffers/protobuf/releases

下载protoc-gen-grpc.exe 插件 ，选择搞版本下载 , 下载地址: https://repo.maven.apache.org/maven2/io/grpc/protoc-gen-grpc-java/

下载之后重命名软件名称，并放置在 D:/SoftWare/  （其他地方也可以）

### 2、编译生成java代码
命令解释：  
　　--java_out：生成之后输出java文件目录
　　--proto_path：proto文件依赖地址，可以写多个
　　--plugin：插件地址
　　user.proto：需要生成的proto协议文件

打开cmd执行：

#### 生成java代码

```shell
protoc.exe  --java_out=. --proto_path=.  scan.proto
```

#### 生成Grpc类

```shell
protoc.exe --plugin=protoc-gen-grpc-java.exe  --grpc-java_out=. --proto_path=.  scan.proto
```

将生成的java文件复制到burp.rpc目录即可。

#### 注意事项：

1、如果 执行过程中发现 import依赖的文件找不到，需要多指定几个 proto_path（如--proto_path:.   --proto_path:/common/base）

2、如果 需要生产多个类需要在 proto文件中加入

```shell
option java_multiple_files = true;
```


 3、如果 需要指定生成的类路径需要在 proto文件中加入

```
option java_package="com.xxxx.xxx.xxx";
```

## python生成

需要安装`grpcio-tools `模块

```shell
python3  -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. scan.proto
```

直接使用即可。