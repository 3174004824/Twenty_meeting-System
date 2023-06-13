package czl.cz.dao;

import czl.cz.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface LockUserDao {

    @Select("select * from lock_user where openid = #{openid}")
    Set<User> selectByOpenid(String openid);

}
