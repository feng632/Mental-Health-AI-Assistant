package com.example.aimentalhealth.controller;

import com.example.aimentalhealth.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")

public class Test {
    @GetMapping("/test")
    public Result<String> test() {
//        Result result = new Result();
//        result.setCode("200");
//        System.out.println(result.getCode());
        return Result.ok("hello world");
    }
}
