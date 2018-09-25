# mongodb-dao
maven repository


## Description
MongoDB専用のDAO

## Usage
```
public class MemberDao extends AbstractDao<Member> {
    public MemberDao() {
        super(Member.class);
    }
}
```

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

```
public class Address implements DocumentConvertible {
    private String postNumber;
    private String country;
    
    // constructor
    // getter
    // setter
}
```

## Install
* maven
```
<dependencies>
    <dependency>
        <groupId>miyakawalab.tool</groupId>
        <artifactId>mongodb-dao</artifactId>
        <version>1.0</version>
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

