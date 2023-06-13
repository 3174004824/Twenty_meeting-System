package czl.cz.dao;

import czl.cz.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDao {

    @Select("select * from user where openid = #{openid}")
    User selectUserByAccount(String openid);

    @Insert("insert into user(openid,nickname) values(#{openid},#{nickname})")
    Integer insertUser(String openid,String nickname);

    @Update("update user set avatarUrl = #{avatarUrl},nickname = #{nickname} where openid = #{openid}")
    Integer updateImage(String openid,String avatarUrl,String nickname);

    @Update("update user set stunum = #{stunum},partment = #{partment},name = #{name} where openid = #{openid}")
    Integer updateInfo(String openid,String stunum,String name,String partment);

    @Update("update user set todaystepsnum = #{todayStepsnum},totalstepsnum = #{totalStepsnum},timeStamp = #{timeStamp} where openid = #{openid}")
    Integer updateSteps(String openid,Integer todayStepsnum,Integer totalStepsnum,String timeStamp);

    //@Select("select ROWINDEX from (SELECT user.*,ROW_NUMBER() over(order by todaystepsnum DESC) as ROWINDEX from user where timeStamp = #{timeStamp}) as temp where openid = #{openid}")
    @Select("select ROWINDEX from (SELECT user.*,ROW_NUMBER() over(order by todaystepsnum DESC) as ROWINDEX from user where timeStamp = #{timeStamp}) as temp where openid = #{openid}")
    Integer selectSelfRank(String openid,String timeStamp);

    //@Select("select ROWINDEX from (SELECT user.openid,ROW_NUMBER() over(order by totalstepsnum DESC) as ROWINDEX from user) as temp where openid = #{openid}")
    @Select("select ROWINDEX from (SELECT user.openid,ROW_NUMBER() over(order by totalstepsnum DESC) as ROWINDEX from user) as temp where openid = #{openid}")
    Integer selectSelfTotalRank(String openid);

    @Insert("Insert into successInfo values(#{stunum},#{timestamp})")
    Integer InsertsuccessInfo(String stunum,String timestamp);

    @Select("SELECT openid,nickname,avatarUrl,todaystepsnum FROM user where timeStamp = #{timeStamp} and totalstepsnum < 266500 ORDER BY todaystepsnum DESC LIMIT 20")
    List<User> getTwentyRank(String timeStamp);

    @Select("SELECT openid,nickname,avatarUrl,totalstepsnum FROM user where totalstepsnum < 266500 ORDER BY totalstepsnum DESC LIMIT 20")
    List<User> getTotalRank();

    @Select("select nickname,avatarUrl,totalstepsnum,timeStamp from user WHERE openid IN (select openid from successInfo order by successtime DESC) limit 20")
    List<User> getSuccessTwentyRank();

    //@Select("select ROWINDEX from (select successInfo.*,ROW_NUMBER() over(order by successtime DESC) as ROWINDEX from successInfo) as temp where openid = #{openid}")
    @Select("select ROWINDEX from (select successInfo.*,ROW_NUMBER() over(order by successtime DESC) as ROWINDEX from successInfo) as temp where openid = #{openid}")
    Integer getSelfSuccessRank(String openid);

}
