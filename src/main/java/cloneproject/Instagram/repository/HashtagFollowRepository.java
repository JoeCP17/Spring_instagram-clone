package cloneproject.Instagram.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cloneproject.Instagram.entity.member.HashtagFollow;

public interface HashtagFollowRepository extends JpaRepository<HashtagFollow, Long>{
    
    boolean existsByMemberIdAndHashtagId(Long memberId, Long hashtagId);
    Optional<HashtagFollow> findByMemberIdAndHashtagId(Long memberId, Long hashtagId);

}
