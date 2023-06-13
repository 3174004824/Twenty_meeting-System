package czl.cz.service.impl;

import czl.cz.dao.MapsDao;
import czl.cz.domain.Maps;
import czl.cz.service.MapsService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;

@Service
public class MapsServiceImpl implements MapsService {

    @Autowired
    MapsDao mapsDao;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public ArrayList<Maps> selectAll() {
        Logger logger = Logger.getLogger(this.getClass());
        //ArrayList<Maps> maps = mapsDao.selectAll();
        ArrayList<Maps> maps = (ArrayList<Maps>) redisTemplate.opsForValue().get("map");

        if (maps == null){
            logger.info("map没查询到");
            return null;
        }

//        maps.sort(new Comparator<Maps>() {
//            @Override
//            public int compare(Maps o1, Maps o2) {
//                return o1.getDistance() - o2.getDistance();
//            }
//        });

        for (int i = 0; i < maps.size(); i++) {
            maps.get(i).setId(i+1);
        }

        return maps;
    }
}
