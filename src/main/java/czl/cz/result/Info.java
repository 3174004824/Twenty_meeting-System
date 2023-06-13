package czl.cz.result;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2022年07月09日 12:22
 */
@Data
@AllArgsConstructor
public class Info {
    private String location;
    private String nextLocation;
    private Double nextLocatDistance;
}
