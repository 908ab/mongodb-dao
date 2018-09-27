package miyakawalab.tool.mongo.repository;

import lombok.Getter;
import lombok.Setter;
import miyakawa.tool.repository.base.RepositoryInterface;
import miyakawa.tool.repository.exception.DomainNotFoundException;
import miyakawalab.tool.mongo.dao.MongoDao;
import miyakawalab.tool.mongo.domain.MongoObject;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractMongoRepository<Domain, Mongo extends MongoObject> implements RepositoryInterface<Domain> {
    @Getter
    @Setter
    private MongoDao<Mongo> dao;

    @Override
    public Long insertOne(Domain domain) {
        return this.dao.insertOne(this.toMongo(domain));
    }

    @Override
    public void insertMany(List<Domain> domainList) {
        List<Mongo> mongoList = domainList.stream()
            .map(this::toMongo)
            .collect(Collectors.toList());
        this.dao.insertMany(mongoList);
    }

    @Override
    public Domain findById(Long id) throws DomainNotFoundException {
        Mongo mongo = this.dao.findOneById(id)
            .orElseThrow(DomainNotFoundException::new);
        return this.toDomain(mongo);
    }

    @Override
    public List<Domain> findAll() {
        return this.dao.findAll().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Long countAll() {
        return this.dao.countAll();
    }

    @Override
    public void updateById(Long id, Domain domain) {
        this.dao.updateOneById(id, this.toMongo(domain));
    }

    @Override
    public void deleteById(Long id) {
        this.dao.deleteOneById(id);
    }

    protected abstract Mongo toMongo(Domain domain);
    protected abstract Domain toDomain(Mongo mongo);
}
