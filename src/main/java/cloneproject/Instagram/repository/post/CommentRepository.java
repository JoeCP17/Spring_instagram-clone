package cloneproject.Instagram.repository.post;

import cloneproject.Instagram.entity.comment.Comment;
import cloneproject.Instagram.entity.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryQuerydsl{

    @Query(value = "select c from Comment c join fetch c.member where c.id = :id")
    Optional<Comment> findWithMemberById(@Param("id") Long id);

    @Query(value = "select c from Comment c join fetch c.post p join fetch p.member where c.id = :id")
    Optional<Comment> findWithPostAndMemberById(@Param("id") Long id);

    List<Comment> findAllByPost(Post post);
}
