package com.wildace.roulette.repositories;

import com.wildace.roulette.domain.documents.WheelResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WheelResultRepository extends MongoRepository<WheelResult, String> {
    List<WheelResult> findAllByTableId(String tableId);
}
