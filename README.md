elasticsearch 5.2.0 增加Native(Java) Scripts
============================================

参考文档：https://www.elastic.co/guide/en/elasticsearch/reference/5.2/modules-scripting-native.html#modules-scripting-native

1.新建一个maven工程，添加依赖jar包，
    <<properties>
             <elasticsearch.version>5.2.0</elasticsearch.version>
    </peoperties>
    <dependency>
        <groupId>org.elasticsearch</groupId>
        <artifactId>elasticsearch</artifactId>
        <version>${elasticsearch.version}</version>
        <scope>compile</scope>
    </dependency>

2.新建一个类，继承 Plugin ,实现接口 ScriptPlugin;
2.新建一个类，实现接口 AbstractDoubleSearchScript;
3.新建一个类，继承 AbstractDoubleSearchScript;

4.添加plugin-descriptor.properties

5.执行命令：
    mvn clean
    mvn package


6.进入target
7.将对应的jar还是propertiescopy之后放入到elasticsearch 5.2.0/plugins/你自己到文件夹
8.重启elasticsearch

9.elasticsearch的query使用如下：
    {
        "function_score": {
            "query" : {
                //bool exists must range match 等正常使用
            }
            },
            "script_score": {
                "script": {
                    "inline": "gormen_es_note_sort_script",
                    "lang" : "native"
                }
            }
        }
    }