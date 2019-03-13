package miyakawalab.tool.mongo.dao;

import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.Setter;
import miyakawalab.tool.mongo.entity.MongoObject;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.ws.rs.InternalServerErrorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

// todo transaction
public abstract class AbstractMongoDao<T extends MongoObject> implements MongoDao<T> {
    private MongoCollection<Document> collection;
    // idのAutoIncrementを行うためのcollection
    private MongoCollection<Document> countersCollection;

    @Setter
    private MongoClient mongoClient;
    @Setter
    private String dbName;
    @Setter
    private String collectionName;

    private Class<T> tClass;

    public AbstractMongoDao(Class<T> tClass) {
        this.tClass = tClass;
    }

    @Override
    public Long insertOne(T object) {
        object.set_id(this.getNextSequence());
        this.collection.insertOne(object.toDocument());
        return object.get_id();
    }

    @Override
    public void insertMany(List<T> objectList) {
        if (objectList.size() == 0) {
            return;
        }
        List<Document> documentList = objectList.stream()
            .peek(object -> object.set_id(this.getNextSequence()))
            .map(T::toDocument)
            .collect(Collectors.toList());
        this.collection.insertMany(documentList);
    }

    @Override
    public Long count(Bson bson) {
        return this.collection.count(bson);
    }

    @Override
    public Long countAll() {
        return this.collection.count();
    }

    @Override
    public Optional<T> findOne(Bson query) {
        MongoCursor<Document> cursor = this.collection.find(query).iterator();

        Optional<T> optional = Optional.empty();
        while (cursor.hasNext()) {
            optional = Optional.ofNullable(this.toObject(cursor.next()));
        }
        cursor.close();
        return optional;
    }

    @Override
    public Optional<T> findOneById(Long id) {
        return this.findOne(Filters.eq("_id", id));
    }

    @Override
    public List<T> findMany(Bson query) {
        MongoCursor<Document> cursor = this.collection.find(query).iterator();
        List<T> objectList = new ArrayList<>();
        while (cursor.hasNext()) {
            objectList.add(this.toObject(cursor.next()));
        }
        cursor.close();
        return objectList;
    }

    @Override
    public List<T> findAll() {
        MongoCursor<Document> cursor = this.collection.find().iterator();

        List<T> objectList = new ArrayList<>();
        while (cursor.hasNext()) {
            objectList.add(this.toObject(cursor.next()));
        }
        cursor.close();
        return objectList;
    }

    @Override
    public List<T> aggregate(Bson... queries) {
        AggregateIterable<Document> output = this.collection.aggregate(Arrays.asList(queries));
        return StreamSupport.stream(output.spliterator(), false)
            .map(this::toObject)
            .collect(Collectors.toList());
    }

    @Override
    public void updateOne(Bson query, T object) {
        this.collection.updateOne(query, new Document("$set", object.update()));
    }

    @Override
    public void updateOneById(Long id, T object) {
        object.set_id(id);
        this.updateOne(Filters.eq("_id", id), object);
    }

    @Override
    public void updateMany(Bson query, List<T> objectList) {
        List<Document> documentList = objectList.stream()
            .map(T::update)
            .collect(Collectors.toList());
        this.collection.updateMany(query, new Document("$set", documentList));
    }

    @Override
    public void deleteOne(Bson query) {
        this.collection.deleteOne(query);
    }

    @Override
    public void deleteOneById(Long id) {
        this.deleteOne(Filters.eq("_id", id));
    }

    @Override
    public void deleteMany(Bson query) {
        this.collection.deleteMany(query);
    }

    private void init() {
        MongoDatabase database = this.mongoClient.getDatabase(this.dbName);
        this.collection = database.getCollection(this.collectionName);
        this.countersCollection = database.getCollection("counters");
        this.checkCountersCollection();
    }

    private Long getNextSequence() {
        this.checkCountersCollection();
        Document find = new Document("_id", this.collectionName);
        Document update = new Document("$inc", new Document("seq", 1L));
        Document document = this.countersCollection.findOneAndUpdate(find, update);
        return document.getLong("seq");
    }

    // counters collectionにdocumentがあるかどうかのチェック
    // なかった場合は新しいdocumentをinsert
    private void checkCountersCollection() {
        MongoCursor<Document> cursor =
            this.countersCollection.find(new Document("_id", this.collectionName)).iterator();
        if (!cursor.hasNext()) {
            Document document = new Document();
            document.append("_id", this.collectionName);
            document.append("seq", 0L);
            this.countersCollection.insertOne(document);
        }
        cursor.close();
    }

    @SuppressWarnings("unchecked")
    private T toObject(Document document) {
        try {
            return (T) this.tClass.newInstance().fromDocument(document);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InternalServerErrorException("can't convert document to object.\n" + e.getMessage());
        }
    }
}
