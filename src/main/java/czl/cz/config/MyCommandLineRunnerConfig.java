package czl.cz.config;

import czl.cz.dao.MapsDao;
import czl.cz.dao.SysParamDao;
import czl.cz.domain.Maps;
import czl.cz.domain.SysParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class MyCommandLineRunnerConfig implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    SysParamDao sysParamDao;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    MapsDao mapsDao;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        List<SysParam> sysParams = sysParamDao.selectAllParam();
        for (SysParam sysParam : sysParams) {
            switch (sysParam.getId()){
                case "1":
                    SysParamConfig.appid = sysParam.getData();
                    break;
                case "2":
                    SysParamConfig.secret = sysParam.getData();
                    break;
                case "3":
                    SysParamConfig.ratio = sysParam.getData();
                    break;
                case "4":
                    SysParamConfig.startTime = sysParam.getData();
                case "5":
                    SysParamConfig.endTime = sysParam.getData();
            }
        }

        ArrayList<Maps> maps = mapsDao.selectAll();
        maps.sort(new Comparator<Maps>() {
            @Override
            public int compare(Maps o1, Maps o2) {
                return o1.getDistance() - o2.getDistance();
            }
        });

        redisTemplate.opsForValue().set("map",maps);

    }
}
