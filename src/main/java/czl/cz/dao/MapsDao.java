package czl.cz.dao;

import czl.cz.domain.Maps;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Repository
public interface MapsDao {

    @Select("select * from map")
    ArrayList<Maps> selectAll();

    public abstract void select();

}
