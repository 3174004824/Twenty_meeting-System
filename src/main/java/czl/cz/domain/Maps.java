package czl.cz.domain;

import lombok.Data;

@Data
public class Maps {
    private String station;
    private Integer distance;
    private String longitude;
    private String latitude;
    private Integer id;
    private String iconPath;
    private String title;
    private String content;
}
