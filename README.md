# mongodb-dao
maven repository


## Description
* MongoDB専用のDAO
* _idはinsertするときに自動で割り振られます

## Usage
* MemberDao.java
```
public class MemberDao extends AbstractMongoDao<Member> {
    public MemberDao() {
        super(Member.class);
    }
}
```

* Member.java
```
public class Member implements MongoObject {
    private Long _id;
    private String name;
    private Integer age;
    @DocumentConvertibleAnnotation
    private Address address
    
    // constructor
    // getter
    // setter
}
```

* Address.java
```
public class Address implements DocumentConvertible {
    private String postNumber;
    private String country;
    
    // constructor
    // getter
    // setter
}
```

* application-context.xml(Spring)
```
<!-- AbstractMongoDao -->
<bean id="mongoClient" class="com.mongodb.MongoClient" destroy-method="close">
    <constructor-arg type="java.lang.String" value="${mongodb.host}" />
    <constructor-arg type="int" value="${mongodb.port}" />
</bean>
<bean id="abstractMongoDao" class="miyakawalab.tool.mongo.dao.AbstractMongoDao" abstract="true"
      depends-on="mongoClient" init-method="init">
    <property name="mongoClient" ref="mongoClient" />
    <property name="dbName" value="${mongodb.db.name}" />
</bean>

<!-- Subject -->
<bean id="memberDao" class="package.MemberDao" parent="abstractMongoDao">
    <property name="collectionName" value="members" />
</bean>
```

## Install
* maven
```
<dependencies>
    <dependency>
        <groupId>miyakawalab.tool</groupId>
        <artifactId>mongodb-dao</artifactId>
        <version>${version}</version>
    </dependency>
</dependencies>
<repositories>
    <repository>
        <id>mongodb-dao</id>
        <url>https://raw.github.com/908ab/mongodb-dao/mvn-repo/</url>
    </repository>
</repositories>
```


## Version
> 1.0

> 1.1 MongoDaoインタフェースの導入 クラス名の変更等

