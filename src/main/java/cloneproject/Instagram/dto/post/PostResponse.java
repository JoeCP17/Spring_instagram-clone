package cloneproject.Instagram.dto.post;

import cloneproject.Instagram.dto.comment.CommentDTO;
import cloneproject.Instagram.dto.member.MemberDTO;
import cloneproject.Instagram.entity.member.Member;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {

    private Long postId;
    private String postContent;
    private LocalDateTime postUploadDate;
    private MemberDTO member;
    private int postLikesCount;
    private boolean postBookmarkFlag;
    private boolean postLikeFlag;
    private boolean commentFlag;
    private String followingMemberUsernameLikedPost;
    private List<PostImageDTO> postImageDTOs = new ArrayList<>();
    private List<CommentDTO> commentDTOs = new ArrayList<>();

    @QueryProjection
    public PostResponse(Long postId, String postContent, LocalDateTime postUploadDate, Member member, int postLikesCount, boolean postBookmarkFlag, boolean postLikeFlag, boolean commentFlag) {
        this.postId = postId;
        this.postContent = postContent;
        this.postUploadDate = postUploadDate;
        this.member = new MemberDTO(member);
        this.postLikesCount = postLikesCount;
        this.postBookmarkFlag = postBookmarkFlag;
        this.postLikeFlag = postLikeFlag;
        this.commentFlag = commentFlag;
    }
}
