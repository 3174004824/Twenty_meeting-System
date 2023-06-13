package czl.cz.service.impl;

import czl.cz.dao.LockUserDao;
import czl.cz.domain.User;
import czl.cz.service.LockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LockUserServiceImpl implements LockUser{

    @Autowired
    LockUserDao lockUserDao;

    @Override
    public Boolean selectAll(String openid) {
        Set<User> users;
        try {
            users = lockUserDao.selectByOpenid(openid);
            if (users.size() == 0) {
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
