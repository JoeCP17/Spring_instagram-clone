package cloneproject.Instagram.repository;

import java.util.List;
import java.util.Map;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import cloneproject.Instagram.dto.alarm.AlarmDTO;
import cloneproject.Instagram.dto.alarm.AlarmType;
import cloneproject.Instagram.dto.alarm.QAlarmDTO;
import lombok.RequiredArgsConstructor;

import static cloneproject.Instagram.entity.comment.QComment.comment;
import static cloneproject.Instagram.entity.alarms.QAlarm.alarm;

@RequiredArgsConstructor
public class AlarmRepositoryQuerydslImpl implements AlarmRepositoryQuerydsl{
    
    private final JPAQueryFactory queryFactory;

    @Override
    public List<AlarmDTO> getAlarms(Long loginedMemberId){

        Map<Long, AlarmDTO> resultAboutComment = queryFactory
                                    .from(alarm)
                                    .where(alarm.target.id.eq(loginedMemberId).and(alarm.type.eq(AlarmType.POST_COMMENT_ALARM)))
                                    .transform(GroupBy.groupBy(alarm.id).as(new QAlarmDTO(
                                        alarm.id,
                                        alarm.type, 
                                        alarm.agent.username, 
                                        alarm.target.username, 
                                        alarm.itemId, 
                                        alarm.createdAt)));

        Map<Long, Long> postIds = queryFactory
                                    .from(alarm)
                                    .where(alarm.target.id.eq(loginedMemberId).and(alarm.type.eq(AlarmType.POST_COMMENT_ALARM)))
                                    .transform(GroupBy.groupBy(alarm.id).as(
                                        JPAExpressions
                                            .select(comment.post.id)
                                            .from(comment)
                                            .where(comment.id.eq(alarm.itemId))        
                                    ));
        
        resultAboutComment.forEach((key, alarm)->alarm.getItemIds().put("postId", postIds.get(key)));

        List<AlarmDTO> result = queryFactory
                                    .select(new QAlarmDTO(
                                        alarm.id,
                                        alarm.type, 
                                        alarm.agent.username, 
                                        alarm.target.username, 
                                        alarm.itemId, 
                                        alarm.createdAt))

                                    .from(alarm)
                                    .where(alarm.target.id.eq(loginedMemberId).and(alarm.type.ne(AlarmType.POST_COMMENT_ALARM)))
                                    .fetch();

        result.addAll(resultAboutComment.values()); // ? 최적화 방법 떠오르지않음
        return result;
    }

}
