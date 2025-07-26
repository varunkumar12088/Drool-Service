package com.academy.drool.repository;

import com.academy.drool.domain.Drool;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DroolRepository extends MongoRepository<Drool, String> {

    List<Drool> findAllByProductName(String productName);
}
