package czl.cz.service.impl;

import czl.cz.dao.UserDao;
import czl.cz.domain.User;
import czl.cz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public User selectUserByAccount(String openid) {
        User user = userDao.selectUserByAccount(openid);
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer insertUser(String openid,String nickname) {
        Integer integer = userDao.insertUser(openid,nickname);
        return integer;
    }

    @Override
    public Integer updateImage(String openid, String avatarUrl, String nickname) {
        if (openid.isEmpty() || avatarUrl.isEmpty() || nickname.isEmpty()){
            return 0;
        }
        return userDao.updateImage(openid, avatarUrl, nickname);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateInfo(String openid,String stunum, String name,String partment) {
        Integer integer = userDao.updateInfo(openid,stunum, name,partment);
        if (integer != 1){
            return false;
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateSteps(String openid, Integer todayStepsnum, Integer totalStepsnum, String timeStamp) {
        Integer integer = userDao.updateSteps(openid, todayStepsnum, totalStepsnum, timeStamp);
        if (integer != 1){
            return false;
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean InsertsuccessInfo(String stunum,String timestamp) {
        Integer integer = userDao.InsertsuccessInfo(stunum,timestamp);
        if (integer != 1){
            return false;
        }
        return true;
    }


    @Override
    public List<User> getTwentyRank(String timeStamp) {
        List<User> selfInfo = userDao.getTwentyRank(timeStamp);
        return selfInfo;
    }

    @Override
    public Integer selectSelfTotalRank(String openid) {
        Integer rank = userDao.selectSelfTotalRank(openid);
        return rank;
    }

    @Override
    public Integer selectSelfRank(String openid,String timeStamp) {
        Integer integer = userDao.selectSelfRank(openid,timeStamp);
        return integer;
    }

    @Override
    public List<User> getTotalRank() {
        List<User> totalRank = userDao.getTotalRank();
        return totalRank;
    }

    @Override
    public List<User> getSuccessTwentyRank() {
        List<User> successTwentyRank = userDao.getSuccessTwentyRank();
        return successTwentyRank;
    }

    @Override
    public Integer getSelfSuccessRank(String openid) {
        return userDao.getSelfSuccessRank(openid);
    }
}
