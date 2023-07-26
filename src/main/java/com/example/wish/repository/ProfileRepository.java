package com.example.wish.repository;

import com.example.wish.entity.Profile;
import com.example.wish.entity.meta_model.Profile_;
import com.example.wish.model.search_request.ProfileSearchRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long>,
        JpaSpecificationExecutor<Profile> {

    Optional<Profile> findById(Long id);

    Optional<Profile> findByEmail(String email);

    int countByUid(String uid);

    Optional<Profile> findByPhone(String phone);

    Optional<Profile> findByUid(String uid);

    @Modifying
    @Query("UPDATE Profile p " +
            "SET p.enabled = TRUE WHERE p.email = ?1")
    int enableProfile(String email);

    class Specs {
        public static Specification<Profile> bySearchRequest(final ProfileSearchRequest searchRequest) {
            return (root, query, cb) -> {
                final List<Predicate> predicates = new ArrayList<>();

                if (searchRequest.getFirstname() != null && searchRequest.getLastname() != null) {
                    predicates.add(cb.and(cb.like(root.get(Profile_.FIRSTNAME), "%" + searchRequest.getFirstname() + "%"),
                            cb.like(root.get(Profile_.LASTNAME), "%" + searchRequest.getLastname() + "%")));
                }

                if (searchRequest.getFirstname() == null && searchRequest.getLastname() != null) {
                    predicates.add(cb.like(root.get(Profile_.LASTNAME), "%" + searchRequest.getLastname() + "%"));
                }

                if (searchRequest.getFirstname() != null && searchRequest.getLastname() == null) {
                    predicates.add(cb.like(root.get(Profile_.FIRSTNAME), "%" + searchRequest.getFirstname() + "%"));
                }

                if (searchRequest.getProfileStatus() != null) {
                    predicates.add(cb.equal(root.get(Profile_.STATUS), searchRequest.getProfileStatus()));
                }

                if (searchRequest.getStatusLevel() != null) {
                    predicates.add(cb.equal(root.get(Profile_.STATUS_LEVEL), searchRequest.getStatusLevel()));
                }

                if (searchRequest.getCountry() != null) {
                    predicates.add(cb.equal(root.get(Profile_.COUNTRY_CODE), searchRequest.getCountry()));
                }

                if (searchRequest.getProfileSex() != null) {
                    Predicate predicate = cb.equal(root.get(Profile_.SEX), searchRequest.getProfileSex());
                    predicates.add(predicate);
                }

                if (searchRequest.getToDate() != null && searchRequest.getFromDate() != null) {

                    Predicate predicate = cb.between(root.get(Profile_.BIRTHDAY), searchRequest.getFromDate(), searchRequest.getToDate());
                    predicates.add(predicate);
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };
        }
    }
}


//                if(searchRequest.isParametersForWish()) {
//
//                    final Join<Profile, Wish> wish = root.join(Profile_.OWN_WISHES);
//
//                    if (searchRequest.getWishStatus() != null) {
//                        predicates.add(cb.equal(wish.get(Wish_.STATUS), searchRequest.getWishStatus()));
//                    }
//
//                    if (searchRequest.getPriorities() != null && searchRequest.getPriorities().size() > 0) {
//
//                        final List<Predicate> predicatesForPriority = new ArrayList<>();
//                        for (Priority pr : searchRequest.getPriorities()) {
//                            predicatesForPriority.add(cb.equal((wish.get(Wish_.PRIORITY)), pr));
//                        }
//                        predicates.add(cb.or(predicatesForPriority.toArray(new Predicate[0])));
//                    }
//
//                    if (searchRequest.getTags() != null && searchRequest.getTags().size() > 0) {
//
//                        final List<Predicate> predicatesForTag = new ArrayList<>();
//                        for (Tag tag : searchRequest.getTags()) {
//                            predicatesForTag.add(cb.isMember(tag, wish.get(Wish_.TAGS)));
//                        }
//                        predicates.add(cb.and(predicatesForTag.toArray(new Predicate[0])));
//                    }
//               }
//     query.distinct(true);