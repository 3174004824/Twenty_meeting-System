package czl.cz.controller;

import com.google.gson.JsonElement;
import czl.cz.domain.User;
import czl.cz.result.Code;
import czl.cz.result.Result;
import czl.cz.service.impl.UserServiceImpl;
import czl.cz.util.StartTimeUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.jws.soap.SOAPBinding;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2022年07月09日 15:32
 */

@RestController
public class RankController {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserServiceImpl userService;

    //获取今日排行和用户自己的排行信息的
    @RequestMapping(value = "gettodayrank",method = RequestMethod.GET)
    public Result getTodayRank(@RequestParam("_3rdsession") String _3rdsession){

        Logger logger = Logger.getLogger(this.getClass());
        Result result;

        if (_3rdsession == null){
            logger.info("_3rdsession为空 " + _3rdsession);
            result = new Result(new Code("10002","参数为空"));
            return result;
        }

        String openid = (String) redisTemplate.opsForHash().get(_3rdsession, "openid");

        if (openid == null){
            logger.info("openid: " + openid);
            result = new Result(new Code("10002","3rdsession失效"));
            return result;
        }






        redisTemplate.expire(_3rdsession,30, TimeUnit.MINUTES);



        //Account account = accountService.selectOpenByAcocunt(openid);
        //后面用
        User user = null;
        List<User> todayRank;


        Integer rank = 0; //当前用户的排名
        try {
            user = userService.selectUserByAccount(openid);














            //优化，可以使用内存存储前20名，每10分钟更新一次


            //Redis中拿今日前20名
            todayRank = redisTemplate.opsForList().range("todayRank", 0, -1);

            if (todayRank == null || todayRank.size() == 0){
                //StartTimeUtil.getTodayStartTime()/1000+""    让它变成一个字符串  时间戳
                todayRank = userService.getTwentyRank(StartTimeUtil.getTodayStartTime()/1000+"");
                if(todayRank != null){
                    redisTemplate.opsForList().rightPushAll("todayRank",todayRank);
                    redisTemplate.expire("todayRank",1,TimeUnit.MINUTES);  //expire 有效期
                }
            }
            //selfInfo = userService.getTwentyRank(StartTimeUtil.getTodayStartTime()/1000+"");



            if (todayRank == null){
                logger.info("selfInfo: "+ todayRank);
                result = new Result(new Code("10002","数据库查询失败"));
                return result;
            }



            //遍历前20名信息，如果当前用户在前20名当中，就可直接获得当前用户的排名
            for (int i = 0; i < todayRank.size(); i++) {
                if (todayRank.get(i).getOpenid().equals(openid)){
                    rank = i+1;
                }
            }



            if (rank == 0){
                //StartTimeUtil.getTodayStartTime()/1000+"" 当天我的步数能排多少名
                rank = userService.selectSelfRank(user.getOpenid(),StartTimeUtil.getTodayStartTime()/1000+"");
            }




        }catch (Exception e){
            logger.error("异常详细信息",e);
            e.printStackTrace();
            return new Result(new Code("10002","系统错误"));
        }
        logger.info("各个参数信息    selfInfo:" + todayRank + "rank:" + rank);
        result = new Result<User>(new Code("10001","查询成功"),todayRank,rank);
        return result;
    }



    @RequestMapping(value = "gettotalrank",method = RequestMethod.GET)
    public Result getTotalRank(@RequestParam("_3rdsession") String _3rdsession){
        Logger logger = Logger.getLogger(this.getClass());
        Result result;
        if (_3rdsession == null){
            logger.info("_3rdsession为空 " + _3rdsession);
            result = new Result(new Code("10002","参数为空"));
            return result;
        }

        String openid = (String) redisTemplate.opsForHash().get(_3rdsession, "openid");
        if (openid == null){
            result = new Result(new Code("10002","3rdsession失效"));
            return result;
        }
        redisTemplate.expire(_3rdsession,30, TimeUnit.MINUTES);
        //Account account = accountService.selectOpenByAcocunt(openid);

        //User user = userService.selectUserByAccount(openid);


        Integer rank = 0;











        //优化方案：使用内存存储前20数据,每隔10分钟更新一次排名
        //测试工作：测试添加到Redis中的排名顺序和取出来的顺序是否一致
        List<User> totalRank = redisTemplate.opsForList().range("totalRank", 0, -1);

        if (totalRank == null || totalRank.size() == 0){
            totalRank = userService.getTotalRank();
            redisTemplate.opsForList().rightPushAll("totalRank",totalRank);
            redisTemplate.expire("totalRank",1,TimeUnit.MINUTES);
        }


        if (totalRank == null){
            result = new Result(new Code("10002","数据库查询失败"));
            return result;
        }







        for (int i = 0; i < totalRank.size(); i++) {
            if (totalRank.get(i).getOpenid().equals(openid)){
                rank = i+1;
            }
        }




        if (rank == 0){
            rank = userService.selectSelfTotalRank(openid);
        }

        logger.info("各个参数信息    selfInfo:" + totalRank + "rank:" + rank);

        result = new Result(new Code("10001","查询成功"),totalRank,rank);
        return result;
    }

    //获取到达前20名信息
    @RequestMapping("/getcomrank")
    public Result getRank(@RequestParam("_3rdsession") String _3rdsession){
        Logger logger = Logger.getLogger(this.getClass());
        Result result;

        if (_3rdsession == null){
            result = new Result(new Code("10002","传递的参数为空"));
            return result;
        }

        String openid = (String) redisTemplate.opsForHash().get(_3rdsession, "openid");
        if (openid == null){
            result = new Result(new Code("10002","Redis问题，拿不到openid"));
            return result;
        }
        //User user = userService.selectUserByAccount(openid);














        //优化方案：用内存去存储前20名，每10分钟刷新一次
        List<User> successTwentyRank = redisTemplate.opsForList().range("successRank", 0, -1);
        if (successTwentyRank == null || successTwentyRank.size() == 0){
            successTwentyRank =  userService.getSuccessTwentyRank();
            redisTemplate.expire("successRank",1,TimeUnit.MINUTES);
        }
        //List<User> successRank = userService.getSuccessTwentyRank();





















        Integer rank = 0;
        if (successTwentyRank.size() > 0){
            for (int i = 0; i < successTwentyRank.size(); i++) {
                if (successTwentyRank.get(i).getOpenid() == openid){
                    rank = i+1;
                }
            }
        }
        if (rank == 0){
            rank = userService.getSelfSuccessRank(openid);
        }



        if (rank == null){
            result = new Result(new Code("10001","您还未到达终点"),successTwentyRank,0);
            return result;
        }
        result = new Result(new Code("10001","查询成功!"),successTwentyRank,rank);
        return result;
    }
}
