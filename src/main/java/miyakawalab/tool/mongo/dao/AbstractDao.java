package miyakawalab.tool.mongo.dao;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.Setter;
import miyakawalab.tool.mongo.domain.MongoObject;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.ws.rs.InternalServerErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// todo transaction
public abstract class AbstractDao<T extends MongoObject> {
    private MongoDatabase database;
    protected MongoCollection<Document> collection;
    // idのAutoIncrementを行うためのcollection
    private MongoCollection<Document> countersCollection;

    @Setter
    private MongoClient mongoClient;
    @Setter
    private String dbName;
    @Setter
    private String collectionName;
    private Class<T> tClass;

    public AbstractDao(Class<T> tClass) {
        this.tClass = tClass;
    }

    public void init() {
        this.database = this.mongoClient.getDatabase(this.dbName);
        this.collection = this.database.getCollection(this.collectionName);
        this.countersCollection = this.database.getCollection("counters");
        this.checkCountersCollection();
    }

    public Long insertOne(T object) {
        object.set_id(this.getNextSequence());
        this.collection.insertOne(object.toDocument());
        return object.get_id();
    }

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

    public long count() {
        return this.collection.count();
    }

    public Optional<T> findOne(Bson query) {
        MongoCursor<Document> cursor = this.collection.find(query).iterator();

        Optional<T> optional = Optional.empty();
        while (cursor.hasNext()) {
            optional = Optional.ofNullable(this.toObject(cursor.next()));
        }
        cursor.close();
        return optional;
    }

    public Optional<T> findOneById(Long id) {
        return this.findOne(Filters.eq("_id", id));
    }

    public List<T> findMany(Bson query) {
        MongoCursor<Document> cursor = this.collection.find(query).iterator();
        List<T> objectList = new ArrayList<>();
        while (cursor.hasNext()) {
            objectList.add(this.toObject(cursor.next()));
        }
        cursor.close();
        return objectList;
    }

    public List<T> findAll() {
        MongoCursor<Document> cursor = this.collection.find().iterator();

        List<T> objectList = new ArrayList<>();
        while (cursor.hasNext()) {
            objectList.add(this.toObject(cursor.next()));
        }
        cursor.close();
        return objectList;
    }

    public void updateOne(Bson query, T object) {
        this.collection.updateOne(query, new Document("$set", object.toDocument()));
    }

    public void updateOneById(Long id, T object) {
        this.updateOne(Filters.eq("_id", id), object);
    }

    public void updateMany(Bson query, List<T> objectList) {
        List<Document> documentList = objectList.stream()
            .map(T::toDocument)
            .collect(Collectors.toList());
        this.collection.updateMany(query, new Document("$set", documentList));
    }

    public void deleteOne(Bson query) {
        this.collection.deleteOne(query);
    }

    public void deleteOneById(Long id) {
        this.deleteOne(Filters.eq("_id", id));
    }

    public void deleteMany(Bson query) {
        this.collection.deleteMany(query);
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
