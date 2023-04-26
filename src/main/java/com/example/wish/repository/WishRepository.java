package com.example.wish.repository;

import com.example.wish.entity.*;
import com.example.wish.entity.meta_model.Profile_;
import com.example.wish.entity.meta_model.Wish_;
import com.example.wish.model.search_request.WishSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long>, JpaSpecificationExecutor<Wish> {

    List<Wish> findByOwnProfileId(long ownProfileId);

    Optional<List<Wish>> findByOwnProfileUid(String uid);

    Page<Wish> findAll(Pageable pageable);

    int countByStatus(WishStatus wishStatus);

    Page<Wish> findAllByTagsIn(Set<Tag> tags, Pageable pageable);

    List<Wish> findByOwnProfileIdAndStatus(Long profileId, WishStatus status);

    @Modifying
    @Query("DELETE Wish c WHERE c.id = ?1")
    void deleteById(long id);

    Page<Wish> findByStatus(WishStatus aNew, Pageable pageable);

    class Specs {
        public static Specification<Wish> bySearchRequest(final WishSearchRequest searchRequest) {
            return (root, query, cb) -> {
                final List<Predicate> predicates = new ArrayList<>();

                final Join<Wish, Profile> profile = root.join(Wish_.OWN_PROFILE);

                if (searchRequest.getStatus() != null) {
                    predicates.add(cb.equal(root.get(Wish_.STATUS), searchRequest.getStatus()));
                }

                if (searchRequest.getPriorities() != null && searchRequest.getPriorities().size() > 0) {
                    final List<Predicate> predicatesForPriority = new ArrayList<>();
                    for (Priority pr : searchRequest.getPriorities()) {
                        predicatesForPriority.add(cb.equal((root.get(Wish_.PRIORITY)), pr));
                    }
                    predicates.add(cb.or(predicatesForPriority.toArray(new Predicate[0])));
                }

                if (searchRequest.getTags() != null && searchRequest.getTags().size() > 0) {

                    final List<Predicate> predicatesForTag = new ArrayList<>();
                    for (Tag tag : searchRequest.getTags()) {
                        predicatesForTag.add(cb.isMember(tag, root.get(Wish_.TAGS)));
                    }
                    predicates.add(cb.and(predicatesForTag.toArray(new Predicate[0])));
                }

                if (searchRequest.getProfileSex() != null) {
                    Predicate predicate = cb.equal(profile.get(Profile_.SEX), searchRequest.getProfileSex());
                    predicates.add(predicate);
                }

                if (searchRequest.getCountry() != null) {
                    predicates.add(cb.equal(profile.get(Profile_.COUNTRY), searchRequest.getCountry()));
                }

                if (searchRequest.getTitle() != null) {
                    predicates.add(cb.or(cb.like(root.get(Wish_.TITLE), "%" + searchRequest.getTitle() + "%"),
                            cb.like(root.get(Wish_.DESCRIPTION), "%" + searchRequest.getTitle() + "%")));
                }

                if (searchRequest.getToDate() != null && searchRequest.getFromDate() != null) {

                    Predicate predicate = cb.between(profile.get(Profile_.BIRTHDAY), searchRequest.getFromDate(), searchRequest.getToDate() );
                    predicates.add(predicate);
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };
        }

    }
}

//  if (searchRequest.getMaxAge() != null && searchRequest.getMinAge() != null) {
//
//                    LocalDate currentDate = LocalDate.now();
//                    LocalDate minLocalDate = currentDate.minusYears(searchRequest.getMaxAge());
//                    LocalDate maxLocalDate = currentDate.minusYears(searchRequest.getMinAge()+1);
//
//                    java.sql.Date sqlDateMin = java.sql.Date.valueOf(minLocalDate);
//                    java.sql.Date sqlDateMax = java.sql.Date.valueOf(maxLocalDate);
//
//
//                    Predicate betweenPredicate = cb.between(profile.get("birthday"), sqlDateMax, sqlDateMin);
//                    Predicate notNullPredicate = cb.isNotNull(profile.get("birthday"));
//
//                    predicates.add(betweenPredicate) ;
//
//                    predicates.add(cb.and(betweenPredicate, notNullPredicate));
//                }