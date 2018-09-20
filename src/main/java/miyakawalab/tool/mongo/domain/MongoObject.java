package miyakawalab.tool.mongo.domain;


import miyakawalab.tool.mongo.annotation.MongoUpdateIgnore;

import javax.ws.rs.InternalServerErrorException;
import java.lang.reflect.Field;
import java.util.Arrays;

public interface MongoObject extends DocumentConvertible {
    public Long get_id();
    public void set_id(Long set_id);

    public default void update(MongoObject object) {
        Arrays.stream(this.getClass().getDeclaredFields())
            .filter(field -> !field.getName().equals("_id"))
            .filter(field -> !Util.hasAnnotation(field, MongoUpdateIgnore.class))
            .forEach(field -> {
                field.setAccessible(true);
                try {
                    Field updatedField = object.getClass().getDeclaredField(field.getName());
                    updatedField.setAccessible(true);
                    field.set(this, updatedField.get(object));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new InternalServerErrorException("can't update miyakawalab.tool.mongo object.");
                }
            });
    }
}
