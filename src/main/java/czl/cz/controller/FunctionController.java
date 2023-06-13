package czl.cz.controller;

import czl.cz.config.SysParamConfig;
import czl.cz.result.Code;
import czl.cz.result.Result;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2022年07月08日 21:52
 */
@RestController
public class FunctionController {

    @Autowired
    RedisTemplate redisTemplate;


    @RequestMapping(value = "verify3rd",method = RequestMethod.GET)     //验证3rdsession是否有效
    public Result verify3rd(@RequestParam("_3rdsession") String _3rdsession){
        Logger logger = Logger.getLogger(this.getClass());
        Result result;

        if (_3rdsession == null){
            logger.info("_3rdsession参数为空");
            result = new Result(new Code("10002","参数为空!"));
            logger.info("result = " + result);
            return result;
        }
        try {
            Map entries = redisTemplate.opsForHash().entries(_3rdsession);  //map中存有  openid  用户账号
            if (entries.size() == 0){
                result = new Result(new Code("10001","_3rdsession失效!"),"false");
                logger.info("_3rdsession失效,或者redis出问题了" + result);
                return result;
            }
            redisTemplate.expire(_3rdsession,30, TimeUnit.MINUTES);// 30分钟计时
            result = new Result(new Code("10001","_3rdsession有效!"),"true");
            logger.info("_3rdsesion有效 :" + result);
            return result;
        }catch (Exception e){
            logger.error("异常详细信息",e);
            result = new Result(new Code("10002","系统故障!"));
            logger.info("result = " + result);
            return result;
        }
    }

    @PostMapping("/getRatio")        //  获取换算比例
    public Result getRatio(){
        try {
            String ratio = SysParamConfig.ratio;
            if (ratio == null){
                return new Result(new Code("10002","获取失败"));
            }
            return new Result(new Code("10001","获取成功"),ratio);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Result(new Code("10003","系统故障"));
    }
}
