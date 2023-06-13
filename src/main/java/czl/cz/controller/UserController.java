package czl.cz.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import czl.cz.config.SysParamConfig;
import czl.cz.dao.MapsDao;
import czl.cz.domain.Maps;
import czl.cz.domain.User;
import czl.cz.result.Code;
import czl.cz.result.Info;
import czl.cz.result.Result;
import czl.cz.service.impl.MapsServiceImpl;
import czl.cz.service.impl.UserServiceImpl;
import czl.cz.util.MyBase64;

import czl.cz.util.StartTimeUtil;
import czl.cz.util.WxStep;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
public class UserController {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserServiceImpl userService;
    @Autowired
    MapsServiceImpl mapsService;
    @Autowired
    MapsDao mapsDao;

    //获取用户信息  User user
    @RequestMapping(value = "/getuserinfo",method = RequestMethod.GET)
    public Result getInformation(@RequestParam("_3rdsession") String _3rdsession){
        Result result;
        Logger logger = Logger.getLogger(this.getClass());
        //判断3rdsession是否为null
        if (_3rdsession == null){
            result = new Result(new Code("10002","参数为空"));
            logger.info("参数为空错误！  _3rdsession = " + _3rdsession);
            return result;
        }
        try {
            //根据3rdsession拿用户的唯一标识符openid
            Map entries = redisTemplate.opsForHash().entries(_3rdsession);
            //entries为空，则用户登陆超过半个小时，登陆过期，重新登陆
            if (entries.size() == 0){
                logger.info("Redis中查询不到相关记录   entries:" + entries);
                result = new Result(new Code("10003","登陆过期，请重新登陆"),_3rdsession,null);
                logger.info("返回结果为：" + result);
                return result;
            }
            String openid = (String) entries.get("openid");  //账号
            if (openid == null){
                logger.info("可能是存_3rdsession时出现异常，导致openid没有存进Redis");
                result = new Result(new Code("10002","_3rdsession存在，但openid不存在"));
                logger.info("返回结果为：" + result);
                return result;
            }
            User user = userService.selectUserByAccount(openid);
            if (user == null){  //没有查到
                result = new Result(new Code("10002","获取失败"),_3rdsession,null);
                logger.info("返回结果为：" + result);
                return result;
            }
            result = new Result(new Code("10001","获取成功"),_3rdsession,user);
            logger.info("返回结果为：" + result);
            return result;
        }catch (Exception e){
            logger.error("异常详细信息",e);
            e.printStackTrace();
            result = new Result(new Code("10003","系统故障，请稍后重试"));
            logger.info("返回结果为：" + result);
            return result;
        }
    }

    /**
     * 解密接口，用于解密开放数据
     * @param encryptedData   加密的数据
     * @param iv              关键字
     * @param _3rdsession     用户唯一标识
     * @return
     */
    @RequestMapping(value = "/getstepnum",method = RequestMethod.POST)
    public Result decryptWxData(@RequestParam("encryptedData") String encryptedData,@RequestParam("iv")
            String iv,@RequestParam("_3rdsession") String _3rdsession){
        Result result;
        Logger logger = Logger.getLogger(this.getClass());
        if (encryptedData == null || _3rdsession == null || iv == null){  //encryptedData   iv   _3rdsession
            logger.info("encrypetData: " + encryptedData + "  _3rdsession: " + _3rdsession + "   iv: " + iv);
            result = new Result(new Code("10002","参数为空"));
            return result;
        }
        try {
            String session_key = (String) redisTemplate.opsForHash().get(_3rdsession, "session_key");  //从Redis中获取当前用户的解密密钥
            if (session_key == null){
                result = new Result(new Code("10003","登陆过期，请重新登陆"));
                logger.info("返回结果为：" + result);
                return result;
            }
            encryptedData = encryptedData.replaceAll(" ", "+");   //    将空格替换为+
            iv = iv.replaceAll(" ","+");    //    将空格替换为+
            String s = MyBase64.decryptData(encryptedData, session_key,iv);   //对加密的数据进行解密
            if (s != null){
                redisTemplate.expire(_3rdsession,30,TimeUnit.MINUTES);
                result = new Result(new Code("10001","解密完成"),s);
                return result;
            }
        }catch (Exception e){
            logger.error("异常详细信息",e);
            e.printStackTrace();
            logger.info("各个数据的情况   encryptedData = " + encryptedData + "参数iv = "+ iv + "参数_3rdsession = "+ _3rdsession);
            result = new Result(new Code("10002","系统错误，请稍后重试"));
            logger.info("返回结果为：" + result);
           return result;
        }
        result = new Result(new Code("10002","解密错误"));
        logger.info("返回结果为：" + result);
        return result;
    }

    //设置用户信息
    @RequestMapping(value = "/setuserinfo",method = RequestMethod.POST)
    public Result updateUrlAndNickname(@RequestParam("_3rdsession") String _3rdsession,
                                       @RequestParam("name") String name,
                                       @RequestParam("partment")String partment,
                                       @RequestParam("stunum")String stunum){

        Result result;
        Logger logger = Logger.getLogger(this.getClass());
        if (_3rdsession == null){
            logger.info("_3rdsession: " + _3rdsession);
            result = new Result(new Code("10002","参数为空"));
            return result;
        }
        try {
            Map entries = redisTemplate.opsForHash().entries(_3rdsession);
            redisTemplate.expire(_3rdsession,30, TimeUnit.MINUTES);
            if (entries.size() == 0){
                logger.info("3rdsession没有获取到Redis中的数据");
                result = new Result(new Code("10003","登录过期，请重新登录"));
                return result;
            }
            String openid = (String) entries.get("openid");
            if (openid == null){
                logger.info("openid没有获取到");
                result = new Result(new Code("10003","登录过期，请重新登录"));
                logger.info("返回结果为：" + result);
                return result;
            }
            //Account account = accountService.selectOpenByAcocunt(openid);
            User user = userService.selectUserByAccount(openid);
            Boolean aBoolean = userService.updateInfo(user.getOpenid(),stunum, name,partment);   //更新用户的信息
            if (!aBoolean){
                logger.info("setuserinfo接口更新操作失败");
                result = new Result(new Code("10002","更新失败，请重试"));
                logger.info("返回结果为：" + result);
                return result;
            }
            User updateUser = userService.selectUserByAccount(openid);  //再次把最新的数据查出来，返回给前端
            result = new Result(new Code("10001","更新成功！"),null,updateUser);
            logger.info("返回结果为：" + result);
            return result;
        }catch (Exception e){
            logger.error("异常详细信息",e);
            e.printStackTrace();
            result = new Result(new Code("10002","系统错误，请稍后重试"));
            logger.info("返回结果为：" + result);
            return result;
        }
    }



    //计算用户上传的步数
    //encryptedData  iv 加密的数据
    //_3rdsession   用户唯一标识
    @RequestMapping(value = "/getsandp",method = RequestMethod.POST)
    public Result getStepsAndPlace(@RequestParam("encryptedData") String encryptedData,
                                   @RequestParam("iv") String iv,
                                   @RequestParam("_3rdsession") String _3rdsession){

        Logger logger = Logger.getLogger(this.getClass());
        Result result;
        if (_3rdsession == null || encryptedData == null || iv == null){
            logger.info("_3rdsession: " + _3rdsession + "  encryptedData: " + encryptedData + "  iv: " + iv);
            result = new Result(new Code("10002","参数为空"),new User(),null);
            logger.info("返回结果为：" + result);
            return result;
        }
        try {
            String session_key = (String) redisTemplate.opsForHash().get(_3rdsession, "session_key");  //拿密钥
            if (session_key == null){
                logger.info("session_key为空值");
                result = new Result(new Code("10002","session_key为空"));
                logger.info("返回结果为：" + result);
                return result;
            }
            redisTemplate.expire(_3rdsession,30,TimeUnit.MINUTES);  //更新30min时间
            String openid = (String) redisTemplate.opsForHash().get(_3rdsession, "openid");
            if (openid == null){
                logger.info("openid值为空: " +openid);
                result = new Result(new Code("10002","openid值为空"));
                logger.info("返回结果为：" + result);
                return result;
            }
            //map数据是从项目一启动就加载到Redis里面去的 --------------  MySQL查到map数据，然后存到Redis
            ArrayList<Maps> maps = (ArrayList<Maps>) redisTemplate.opsForValue().get("map");  //从Redis里面拿到地图数据
            if (maps == null || maps.size() == 0){
                maps = mapsDao.selectAll();      //从数据库中获取地图数据  ArrayList 是一个列表
                maps.sort(new Comparator<Maps>() {   //对数据进行排序    0  1  2  3  map.get(index)
                    @Override
                    public int compare(Maps o1, Maps o2) {
                        return o1.getDistance() - o2.getDistance();
                    }
                });

                redisTemplate.opsForValue().set("map",maps);
            }
            Integer selfSuccessRank1 = userService.getSelfSuccessRank(openid); // 判断用户是否到达终点
            if (selfSuccessRank1 != null){
                return new Result(new Code("10004","您已到达终点!"),null,new Info(maps.get(maps.size()-1).getStation(),"无",0.00));
            }
            encryptedData = encryptedData.replaceAll(" ", "+");
            iv = iv.replaceAll(" ","+");
            String openData = MyBase64.decryptData(encryptedData, session_key, iv); //解密
            //对解密的数据做了一个处理
            JSONObject stepInfoListJson = JSON.parseObject(openData);
            //解析json，获取stepInfoList下面的步数
            String stepInfoList = stepInfoListJson.getString("stepInfoList");
            Gson gson = new Gson();
            List<WxStep> list = gson.fromJson(stepInfoList, new TypeToken<List<WxStep>>() {}.getType());
            if (list.size() < 28){
                logger.info("数据不是28天的数据，很有可能出问题了! list.size() = " + list.size());
            }
            User user = userService.selectUserByAccount(openid);  //根据账号查用户信息
            int size = list.size();
            //拿到两个时间进行对比
            String timeStamp = user.getTimeStamp();  //拿到用户上一次登陆的时间戳，可以知道用户上一次什么时候登陆的
            String nowTime = StartTimeUtil.getTodayStartTime()/1000 + "";  //当前时间00：00：00
            //获取用户自上一次登陆后的总步数
            Integer totalStepSum = user.getTotalstepsnum();
            Integer todaySteps = user.getTodaystepsnum();//获取用户今日步数
            //将用户上传的步数累加到用户总步数当中去
            for (int i = 0; i < size; i++) {
                if (list.get(i).getTimestamp().compareTo(timeStamp) > 0  //遍历到的日期大于用户最后一次上传步数的日期
                        && list.get(i).getTimestamp().compareTo(SysParamConfig.startTime) >= 0  //遍历到的日期大于活动开始的日期
                        && list.get(i).getTimestamp().compareTo(SysParamConfig.endTime) <= 0 //遍历到的日期小于活动截至日期
                ){
                    if (list.get(i).getStep() > 30000){
                        totalStepSum += 30000;
                    }else {
                        totalStepSum = totalStepSum + list.get(i).getStep();
                    }
                }else
                    //一天之内上传多次步数
                    if (list.get(i).getTimestamp().compareTo(timeStamp) == 0){

                    if (list.get(i).getStep() > 30000){
                        totalStepSum += 30000 - todaySteps;  //500   1500   15000  15500    最终步数  15500 - 500 + 1500
                    }else {
                        totalStepSum += list.get(i).getStep() - todaySteps;
                    }
                }
            }
            if (list.get(size-1).getStep() > 30000){
                todaySteps = 30000;
            }else {
                todaySteps = list.get(size-1).getStep();
            }
            //寻找用户在地图当中的位置  totalStepSum 总步数
            //maps.get(maps.size()-1).getDistance()  会宁的距离
            //totalStepSum * Double.valueOf(SysParamConfig.ratio)   用户总步数经过转化后的距离
            if (totalStepSum * Double.valueOf(SysParamConfig.ratio) >= maps.get(maps.size()-1).getDistance()){
                Boolean aBoolean = userService.InsertsuccessInfo(user.getOpenid(),String.valueOf(new Date().getTime()/1000));  //将用户信息插入到到达表
                if (!aBoolean){
                    return new Result(new Code("10004","用户已到达终点,但添加记录失败!"));
                }
                userService.updateSteps(user.getOpenid(), todaySteps, 400000, nowTime);
                return new Result(new Code("10004","您已到达终点!"),null,new Info(maps.get(maps.size()-1).getStation(),"无",0.00));
            }
            Boolean aBoolean = userService.updateSteps(user.getOpenid(), todaySteps, totalStepSum, nowTime);
            if (!aBoolean){
                result = new Result(new Code("10002","更新失败!"));
                logger.info("返回信息 : " + result);
                return result;
            }
            User curUser = userService.selectUserByAccount(user.getOpenid());
            if (curUser == null){
                logger.info("curUser是空值");
                result = new Result(new Code("10002","获取数据失败!"));
                return result;
            }
            Double doubleStps = totalStepSum * Double.valueOf(SysParamConfig.ratio);   //totalStepSum * Double.valueOf(SysParamConfig.ratio)   用户总步数经过转化后的距离
            String location = maps.get(0).getStation();//起点 瑞金
            String nextlocation = maps.get(1).getStation(); //下一个地点
            Double nextLocationDistance = 0.00; //防止精度丢失
            //遍历地图表，找用户所在位置
            for (int i = 0; i < maps.size()-1; i++) {
                //如果判断正确，证明用户在下标为i的地点和下标为i+1的地点的中间
                if (doubleStps >= maps.get(i).getDistance() && doubleStps < maps.get(i+1).getDistance()){
                    location = maps.get(i).getStation();
                    nextlocation = maps.get(i+1).getStation();
                    nextLocationDistance = maps.get(i+1).getDistance() - curUser.getTotalstepsnum() *Double.valueOf(SysParamConfig.ratio);
                    break;  //跳出循环
                }
            }
            return new Result(new Code("10001","更新成功!"),curUser,new Info(location,nextlocation,nextLocationDistance));
        }catch (Exception e){
            logger.error("异常详细信息",e);
            logger.info("开放数据 :" + encryptedData + "iv : " + iv + "_3rdsession :" + _3rdsession);
            e.printStackTrace();
            return new Result(new Code("10002","系统异常"));
        }
    }





    //获取地图
    @GetMapping("/getmap")
    public Result getMap(){
        Logger logger = Logger.getLogger(this.getClass());
        Result result;
        ArrayList<Maps> maps;
        try {
            maps = (ArrayList<Maps>) redisTemplate.opsForValue().get("map"); //在Redis中获取地图数据
            if (maps == null || maps.size() == 0){
                maps = mapsDao.selectAll();
                maps.sort(new Comparator<Maps>() {  //对地图进行排序
                    @Override
                    public int compare(Maps o1, Maps o2) {
                        return o1.getDistance() - o2.getDistance();
                    }
                });
                redisTemplate.opsForValue().set("map",maps);  //再将数据放入Redis，方便下次查询
            }
        }catch (Exception e){
            result = new Result(new Code("10002","系统异常"));
            e.printStackTrace();
            logger.error("异常详细信息",e);
            return result;
        }
        result = new Result(new Code("10001","获取成功"),maps);
        return result;
    }
}
