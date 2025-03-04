package cloneproject.Instagram.dto.member;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.querydsl.core.annotations.QueryProjection;

import cloneproject.Instagram.dto.post.MiniProfilePostDTO;
import cloneproject.Instagram.dto.post.PostImageDTO;
import cloneproject.Instagram.vo.Image;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@ApiModel("유저 미니프로필 응답 모델")
@Getter
@Setter
@Builder
@AllArgsConstructor
public class MiniProfileResponse {

    @ApiModelProperty(value = "유저네임", example = "dlwlrma")
    private String memberUsername;

    @ApiModelProperty(value = "프로필사진")
    private Image memberImage;

    @ApiModelProperty(value = "이름", example = "이지금")
    private String memberName;

    @ApiModelProperty(value = "웹사이트", example = "http://localhost:8080")
    private String memberWebsite;

    @ApiModelProperty(value = "팔로잉 여부", example = "true")
    private boolean isFollowing;

    @ApiModelProperty(value = "팔로워 여부", example = "false")
    private boolean isFollower;

    @ApiModelProperty(value = "차단 여부", example = "false")
    private boolean isBlocking;

    @ApiModelProperty(value = "차단당한 여부", example = "false")
    private boolean isBlocked;

    @ApiModelProperty(value = "포스팅 수", example = "90")
    private Long memberPostsCount;

    @ApiModelProperty(value = "팔로워 수", example = "100")
    private Long memberFollowersCount;

    @ApiModelProperty(value = "팔로잉 수", example = "100")
    private Long memberFollowingsCount;

    @ApiModelProperty(value = "최근 게시물 3개")
    private List<MiniProfilePostDTO> memberPosts;

    @ApiModelProperty(value = "내 팔로잉 중 해당 유저를 팔로우 하는 사람", example = "dlwlrma")
    private String followingMemberFollow;

    @ApiModelProperty(value = "본인 여부", example = "false")
    private boolean isMe;

    private boolean hasStory;

    @QueryProjection
    public MiniProfileResponse(String username, String name, Image image, boolean isFollowing, boolean isFollower,
                               boolean isBlocking, boolean isBlocked, Long postsCount,
                               Long followingsCount, Long followersCount, boolean isMe) {
        this.memberUsername = username;
        this.memberName = name;
        this.memberImage = image;
        this.isFollowing = isFollowing;
        this.isFollower = isFollower;
        this.isBlocking = isBlocking;
        this.isBlocked = isBlocked;
        this.memberPostsCount = postsCount;
        this.memberFollowingsCount = followingsCount;
        this.memberFollowersCount = followersCount;
        this.isMe = isMe;
        this.hasStory = false;
    }

    public void setMemberPosts(List<PostImageDTO> postImageDTOs) {
        final Map<Long, List<PostImageDTO>> postDTOMap = postImageDTOs.stream()
                .collect(Collectors.groupingBy(PostImageDTO::getPostId));
        List<MiniProfilePostDTO> results = new ArrayList<MiniProfilePostDTO>();
        postDTOMap.forEach((id, p) -> results.add(
                MiniProfilePostDTO.builder()
                        .postId(id)
                        .postImageUrl(p.get(0).getPostImageUrl())
                        .build()
        ));
        this.memberPosts = results;
    }

    public void checkBlock() {
        if (this.isBlocked || this.isBlocking) {
            this.memberPostsCount = 0L;
            this.memberFollowersCount = 0L;
            this.memberFollowingsCount = 0L;
            this.memberPosts = null;
        }
    }

    public void setFollowingMemberFollow(String followingMemberFollow) {
        this.followingMemberFollow = followingMemberFollow;
    }
}
