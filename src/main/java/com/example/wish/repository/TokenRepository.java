package com.example.wish.repository;


import com.example.wish.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("""
            select t from Token t inner join Profile p on t.profile.id=p.id 
            where p.id= :profileId and (t.expired= false or t.revoked=false)
            """)
    List<Token> findAllValidTokensByProfile(long profileId);

    Optional<Token> findByToken(String token);


}
