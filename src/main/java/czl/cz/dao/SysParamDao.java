package czl.cz.dao;

import czl.cz.domain.SysParam;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysParamDao {

    @Select("select * from sysparam")
    List<SysParam> selectAllParam();

}
