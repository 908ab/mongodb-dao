package miyakawalab.tool.mongo.entity;

import miyakawalab.tool.mongo.annotation.DocumentConvertibleAnnotation;
import miyakawalab.tool.mongo.annotation.DocumentConvertibleListAnnotation;
import miyakawalab.tool.mongo.annotation.MongoUpdateIgnore;
import org.bson.Document;

import javax.ws.rs.InternalServerErrorException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static miyakawalab.tool.mongo.entity.Util.camelToSnake;
import static miyakawalab.tool.mongo.entity.Util.hasAnnotation;

public interface DocumentConvertible {
    @SuppressWarnings("unchecked")
    public default Document toDocument() {
        Document document = new Document();
        Arrays.stream(this.getClass().getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);
            try {
                Object object = field.get(this);
                if (hasAnnotation(field, DocumentConvertibleAnnotation.class)) {
                    // documentの中にdocumentがある場合の処理
                    document.append(
                        camelToSnake(field.getName()),
                        ((DocumentConvertible) object).toDocument()
                    );
                } else if (hasAnnotation(field, DocumentConvertibleListAnnotation.class)) {
                    // documentの中にdocumentのlistがある場合の処理
                    document.append(camelToSnake(
                        field.getName()),
                        ((List<DocumentConvertible>) object).stream()
                            .map(DocumentConvertible::toDocument)
                            .collect(Collectors.toList())
                    );
                } else {
                    document.append(camelToSnake(field.getName()), object);
                }
            } catch (IllegalAccessException e) {
                throw new InternalServerErrorException("can't convert object to document.\n" + e.getMessage());
            }
        });
        return document;
    }

    public default Document update() {
        Document document = new Document();
        Arrays.stream(this.getClass().getDeclaredFields()).filter(field -> !field.getName().equals("_id")).filter(field -> !Util.hasAnnotation(field, MongoUpdateIgnore.class)).forEach(field -> {
            field.setAccessible(true);
            try {
                Object object = field.get(this);
                if (hasAnnotation(field, DocumentConvertibleAnnotation.class)) {
                    // documentの中にdocumentがある場合の処理
                    document.append(
                        camelToSnake(field.getName()),
                        ((DocumentConvertible) object).toDocument()
                    );
                } else if (hasAnnotation(field, DocumentConvertibleListAnnotation.class)) {
                    // documentの中にdocumentのlistがある場合の処理
                    document.append(camelToSnake(
                        field.getName()),
                        ((List<DocumentConvertible>) object).stream()
                            .map(DocumentConvertible::toDocument)
                            .collect(Collectors.toList())
                    );
                } else {
                    document.append(camelToSnake(field.getName()), object);
                }
            } catch (IllegalAccessException e) {
                throw new InternalServerErrorException("can't convert object to document.\n" + e.getMessage());
            }
        });
        return document;
    }

    @SuppressWarnings("unchecked")
    public default DocumentConvertible fromDocument(Document document) {
        Arrays.asList(this.getClass().getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);
            try {
                if (hasAnnotation(field, DocumentConvertibleAnnotation.class)) {
                    // documentの中にdocumentがある場合の処理
                    Document subDocument = document.get(camelToSnake(field.getName()), Document.class);
                    DocumentConvertible object = (DocumentConvertible) field.getType().newInstance();
                    field.set(this, object.fromDocument(subDocument));
                } else if (hasAnnotation(field, DocumentConvertibleListAnnotation.class)) {
                    // documentの中にdocumentのlistがある場合の処理
                    ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                    Class<? extends DocumentConvertible> listType = (Class<? extends DocumentConvertible>) parameterizedType.getActualTypeArguments()[0];

                    List<Document> subDocumentList = (List<Document>) document.get(camelToSnake(field.getName()), List.class);
                    List<DocumentConvertible> subObjectList = subDocumentList.stream()
                        .map(d -> {
                            try {
                                return listType.newInstance().fromDocument(d);
                            } catch (IllegalAccessException | InstantiationException e) {
                                throw new InternalServerErrorException("can't convert document to object in list.\n" + e.getMessage());
                            }
                        })
                        .collect(Collectors.toList());
                    field.set(this, subObjectList);
                } else {
                    field.set(this, document.get(camelToSnake(field.getName()), field.getType()));
                }
            } catch (IllegalAccessException | InstantiationException e) {
                throw new InternalServerErrorException("can't convert document to object.\n" + e.getMessage());
            }
        });
        return this;
    }
}
