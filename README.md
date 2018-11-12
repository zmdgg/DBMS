# DBMS
数据库系统，基于简单的正则，eclipse运行，内置了一个用户test，密码123；进入后可创建新用户

## 各模块详细说明
* 1．主函数模块
DBMS入口，运行MainSystem类进入数据库管理系统。首先调用Initialize的createUserInfo()和createDBPath()方法，分别生成存储用户信息的数据库文件和初始化数据库文件存储路径；然后，调用登录子模块判断登录，若登录成功则调用getInput()函数获取SQL语句，然后调用解析SQL语句子模块进行解析，如果没有语句错误则调用执行语句子模块执行该SQL语句，若该语句是更新数据表则需要先通过数据表检查更新子模块，才能执行。
* 2．登录子模块
调用welcomeView()函数显示登录界面，并记录输入的登录名和密码，然后调用UserInfo类的isLogin()函数获取userInfo.dbf文件中的用户信息，进行验证，若通过验证则给出提示信息并进入解析SQL语句子模块。
* 3．解析SQL语句子模块
While循环调用getInput()方法获取SQL语句，并调用praseSQL()方法解析出操作类型，然后调用相应方法解析和检查错误，如果没有语句错误再进行权限检查，若通过检查则进入语句执行模块；此外如果是对数据表更新，则在执行之前需要调用CheckData类的相应方法检查。
* 4．数据表检查更新子模块
主要是对INSERT、UPDATE、DELETE等语句进行检查，先获取数据表的属性信息，然后再进行实体完整性和参照完整性检查，如果通过则进入执行语句子模块；否则，给出相应提示信息。
* 5．执行语句子模块
对数据库文件进行操作，调用Utils类的各种相应方法来执行SQL语句，并输出执行结果。

## 数据库文件目录结构说明
* 1．目录结构图 
![目录结构图 ]()
* 2．说明
userInfo.dbf存放用户信息，userInfo.out存放用户信息表的约束；DBMS文件夹下有一个数据库文件夹，其下可以有任意个数据库名文件夹；authorityInfo.out是存放某个数据库的用户访问权限；表文件夹下有任意个表明文件夹和一个FKInfo.out以及一个index.out文件，其中FKInfo.out是存放外键约束信息的文件；每个表名文件夹下都有一个表名.dbf和一个表名.out文件，分别存放该数据表的表中数据和属性信息。
