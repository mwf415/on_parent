package cn.onlov.on_cms_admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {

    @RequestMapping("/start")
    public String index() {
        log.info("log 启动成功");
        System.out.println("启动成功");
        return "启动成功";
    }

}
