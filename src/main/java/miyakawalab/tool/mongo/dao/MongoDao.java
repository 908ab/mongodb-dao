package miyakawalab.tool.mongo.dao;

import org.bson.conversions.Bson;

import java.util.List;
import java.util.Optional;

public interface MongoDao<T> {
    Long insertOne(T object);
    void insertMany(List<T> objectList);
    Long count(Bson bson);
    Long countAll();
    Optional<T> findOne(Bson query);
    Optional<T> findOneById(Long id);
    List<T> findMany(Bson query);
    List<T> findAll();
    List<T> aggregate(Bson... queries);
    void updateOne(Bson query, T object);
    void updateOneById(Long id, T object);
    void updateMany(Bson query, List<T> objectList);
    void deleteOne(Bson query);
    void deleteOneById(Long id);
    void deleteMany(Bson query);
}
