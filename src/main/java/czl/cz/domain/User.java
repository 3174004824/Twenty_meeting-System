package czl.cz.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class User implements Serializable {
    private String openid;
    private String stunum;
    private String name;
    private String nickname;
    private String avatarUrl;
    private Integer totalstepsnum;
    private Integer todaystepsnum;
    private String partment;
    private String timeStamp;
}
