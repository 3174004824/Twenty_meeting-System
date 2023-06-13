package czl.cz.result;

import czl.cz.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Result<T> implements Serializable {
    private Code code;
    private String _3rdsession;
    private User user;
    private String Data;
    private Info info;
    private List<T> userList;
    private Integer rank;

    public Result(Code code) {
        this.code = code;
    }

    public Result(Code code, String data){
        this.code = code;
        this.Data = data;
    }

    public Result(Code code, String _3rdsession, User user) {
        this.code = code;
        this._3rdsession = _3rdsession;
        this.user = user;
    }

    public Result(Code code, User user, Info info) {
        this.code = code;
        this.user = user;
        this.info = info;
    }

    public Result(Code code, List<T> userList, Integer rank) {
        this.code = code;
        this.userList = userList;
        this.rank = rank;
    }

    public Result(Code code, List<T> userList) {
        this.code = code;
        this.userList = userList;
    }
}
