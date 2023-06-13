package czl.cz.controller;

import czl.cz.domain.User;
import czl.cz.result.Code;
import czl.cz.result.Result;
import czl.cz.service.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class ImageAndNicknameController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Value("${file.url}")
    private String url;

    @Value("${upload.filePath}")
    private String filePath;

    @RequestMapping(value = "/sendImage",method = RequestMethod.POST)
    public Result receivedImageAndNickname(@RequestParam("image") MultipartFile image, @RequestParam("nickname") String nickname,
                                           String _3rdsession){
        Result result;
        Logger logger = Logger.getLogger(this.getClass());
        if (_3rdsession == null || image == null || nickname == null){
            logger.info("_3rdsession: " + _3rdsession + "   avatarUrl: " + image + "   nickname: " + nickname);
            result = new Result(new Code("10002","参数为空"));
            return result;
        }
        try {

            String filename = image.getOriginalFilename();
            //String name = filename.substring(0,filename.indexOf("."));
            String suffix = filename.substring(filename.lastIndexOf("."));
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
            File file = new File(filePath);
            File absoluteFile = file.getAbsoluteFile();
            System.out.println(absoluteFile);

            if (!file.exists()){
                file.mkdir();
            }

            File targetFile = new File(file,openid+suffix);

            if (targetFile.exists()){
                targetFile.delete();
            }

//            if (!file.getParentFile().exists()){
//                file.getParentFile().mkdir();
//            }
            image.transferTo(targetFile);

            String fileUrl = url + openid + suffix;

            Integer integer = userService.updateImage(openid, fileUrl, nickname);
            if (integer == 0){
                return new Result(new Code("10002","上传失败"));
            }
            User user = userService.selectUserByAccount(openid);
            return new Result(new Code("10001","上传成功"),null,user);
        }catch (Exception e){
            e.printStackTrace();
            return new Result(new Code("10002","系统错误"));
        }
    }
}
