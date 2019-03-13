package miyakawalab.tool.mongo.entity;


public interface MongoObject extends DocumentConvertible {
    public Long get_id();
    public void set_id(Long set_id);
}
