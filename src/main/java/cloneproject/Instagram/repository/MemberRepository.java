package cloneproject.Instagram.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import cloneproject.Instagram.entity.member.Member;

public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member>, MemberPostRepositoryQuerydsl, MemberRepositoryQuerydsl {

    Optional<Member> findByUsername(String username);

    Optional<Member> findById(Long id);

    List<Member> findAll(Specification<Member> spec);

    boolean existsByUsername(String username);

    List<Member> findAllByUsernameIn(Collection<String> usernames);
}
