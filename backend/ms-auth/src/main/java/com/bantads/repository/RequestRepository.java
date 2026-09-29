package com.bantads.repository;

import com.bantads.entity.Request;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface RequestRepository extends MongoRepository<Request, ObjectId> {
    boolean existsBySagaIdAndType(UUID sagaId, String type);
}
