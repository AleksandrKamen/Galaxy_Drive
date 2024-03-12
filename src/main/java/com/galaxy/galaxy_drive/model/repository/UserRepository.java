package com.galaxy.galaxy_drive.model.repository;

import com.galaxy.galaxy_drive.model.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {


}
