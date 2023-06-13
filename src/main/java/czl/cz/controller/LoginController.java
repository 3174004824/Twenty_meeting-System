package czl.cz.controller;

import com.alibaba.fastjson.JSONObject;
import czl.cz.config.SysParamConfig;
import czl.cz.domain.User;
import czl.cz.result.Code;
import czl.cz.result.Result;
import czl.cz.service.impl.LockUserServiceImpl;
import czl.cz.service.impl.UserServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;


@RestController
public class LoginController {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    LockUserServiceImpl lockUserService;

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public Result login(@RequestParam String code){
        Logger logger = Logger.getLogger(this.getClass());
        Result results;
        if (code == null){
            results = new Result(new Code("10002","参数为空"));
            logger.info("参数为空异常  code = " + code);
            return results;
        }
        try {


            String url = "http://api.weixin.qq.com/sns/jscode2session?appid=" + SysParamConfig.appid +
                    "&secret=" + SysParamConfig.secret + "&js_code="+code;
            String session = restTemplate.getForObject(url, String.class);
            //logger.info("请求url: " + url + "返回结果: " + session);
            JSONObject result = JSONObject.parseObject(session);//JSON
            if (result.getString("errcode") != null){
                results = new Result(new Code("10002","code失效(errcode)，请重新尝试"));
                logger.info("返回结果为：" + results);
                return results;
            }
            String openid = result.getString("openid");        //账号
            String session_key = result.getString("session_key");
            try {

                Boolean res = lockUserService.selectAll(openid);  //查看用户是否被封禁

                if (!res){
                    return new Result(new Code("10004","用户被封"));
                }
            }catch (Exception e){
                logger.error("异常详细信息",e);
                e.printStackTrace();
            }
            User user = userService.selectUserByAccount(openid);
            String _3rdsession = DigestUtils.md5DigestAsHex(session.getBytes());   //_3rdsession
            if (user == null){   //是新用户
                Integer integer = userService.insertUser(openid,"微信用户");   //注册用户
                if (integer < 0){
                    return new Result(new Code("10002","注册失败"));
                }
                HashMap<String,String> map = new HashMap();         //HashMap    K-V   201910xxx    人
                map.put("openid",openid);
                map.put("session_key",session_key);
                redisTemplate.opsForHash().putAll(_3rdsession,map);
                redisTemplate.expire(_3rdsession,30,TimeUnit.MINUTES);
                results = new Result(new Code("10001","注册成功(已存入redis)"),_3rdsession,null);
                logger.info("返回结果为：" + results);
                return results;
            }
            //老用户  30分钟计时重新开始
            HashMap<String,String> map = new HashMap();
            map.put("openid",openid);
            map.put("session_key",session_key);
            redisTemplate.opsForHash().putAll(_3rdsession,map);
            redisTemplate.expire(_3rdsession,30,TimeUnit.MINUTES);
            results = new Result(new Code("10001","登陆成功(已存入redis)"),_3rdsession,null);//返回给前端
            logger.info("返回结果为：" + results);
            return results;
        }catch (Exception e){
            logger.error("异常详细信息",e);
            e.printStackTrace();
            return new Result(new Code("10002","系统错误，请稍后重试"));
        }
    }

//    @RequestMapping(value = "/bindstunum",method = RequestMethod.POST)
//    public Result bindAccount(@RequestParam("_3rdsession") String _3rdsession,@RequestParam("stuNum")
//            String stuNum,@RequestParam("stuPassword") String stuPassword){
//        Result resut;
//        Logger logger = LoggerFactory.getLogger(this.getClass());
//
//        if (_3rdsession == null || stuNum == null || stuPassword == null){
//            resut = new Result(new Code("10002","参数为空"));
//            logger.info("参数为空异常   result = " + resut);
//            return resut;
//        }
//
//        try {
//            String openid = (String) redisTemplate.opsForHash().get(_3rdsession, "openid");
//            if (openid == null){
//                logger.info("redis中查不到_3rdsession对应的值，可能是键过期了  openid:" + openid);
//                resut = new Result(new Code("10003","登陆过期(在redis中查询不到openid)，请重新登陆！"));
//                logger.info("返回结果为：" + resut);
//                return resut;
//            }
//
//            Account account = accountService.selectOpenByAcocunt(openid);
//            if (account != null){
//                logger.info("数据库中没有这条记录  openid:" + openid);
//                resut = new Result(new Code("10002","微信已绑定其他账号！"),_3rdsession);
//                logger.info("返回结果为：" + resut);
//                return resut;
//            }
//
//            Boolean aBoolean = accountService.updateOpenid(stuNum, stuPassword, openid);
//
//            if (!aBoolean){
//                logger.info("更新失败，openid的值没有跟账号密码表绑定到一起   aBoolean:" + aBoolean);
//                resut = new Result(new Code("10002","绑定失败，账号或密码错误！"),_3rdsession);
//                logger.info("返回结果为：" + resut);
//                return resut;
//            }
//
//            redisTemplate.expire(_3rdsession,30,TimeUnit.MINUTES);
//            logger.info("键重新设置了过期时间");
//        }catch (Exception e){
//            logger.info("Redis获取值的操作失败     openid: ");
//            e.printStackTrace();
//            resut = new Result(new Code("10002","系统错误，请稍后重试"));
//            logger.info("返回结果为：" + resut);
//            return resut;
//        }
//        resut = new Result(new Code("10001","绑定成功！"),_3rdsession);
//        logger.info("返回结果为：" + resut);
//        return resut;
//    }
}




