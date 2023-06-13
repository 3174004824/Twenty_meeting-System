package czl.cz.service;

import czl.cz.domain.User;

import java.util.Set;

public interface LockUser {

    Boolean selectAll(String openid);

}
