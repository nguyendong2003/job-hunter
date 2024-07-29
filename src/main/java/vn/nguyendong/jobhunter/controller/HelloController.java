package vn.nguyendong.jobhunter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.nguyendong.jobhunter.util.error.IdInvalidException;

@RestController
public class HelloController {

    @GetMapping("/")
    public String getHelloWorld() throws IdInvalidException {
        // if (true)
        // throw new IdInvalidException("error rồi");
        return "Hello world";
    }

}
