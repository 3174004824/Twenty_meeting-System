package czl.cz.service;

import czl.cz.domain.User;

import java.util.List;

public interface UserService {
    User selectUserByAccount(String account);

    Integer insertUser(String openid,String nickname);

    Integer updateImage(String openid,String avatarUrl,String nickname);

    Boolean updateInfo(String openid,String stunum, String partment,String name);

    Boolean updateSteps(String stunum,Integer todayStepsnum,Integer totalStepsnum,String timeStamp);

    Boolean InsertsuccessInfo(String stunum,String timestamp);

    List<User> getTwentyRank(String timeStamp);

    Integer selectSelfTotalRank(String stunum);

    Integer selectSelfRank(String stunum,String timeStamp);

    List<User> getTotalRank();

    List<User> getSuccessTwentyRank();

    Integer getSelfSuccessRank(String stunum);

}
