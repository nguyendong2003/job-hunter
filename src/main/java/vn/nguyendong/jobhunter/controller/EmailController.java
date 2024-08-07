package vn.nguyendong.jobhunter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.nguyendong.jobhunter.service.EmailService;
import vn.nguyendong.jobhunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    /*
     * 
     * 
     * tạo file job.html trong src/main/resources/templates
     * => Hàm sendEmailFromTemplateSync cần truyền vào file job.html
     * => Truyền test ở biến templateName
     * 
     */
    @GetMapping("/email")
    @ApiMessage("Send simple email")
    public String sendSimpleEmail() {
        // this.emailService.sendSimpleEmail();
        // this.emailService.sendEmailSync("dongcoi14122003@gmail.com", "test send
        // email", "<h1><b>hello</b></h1>", false,
        // true);
        this.emailService.sendEmailFromTemplateSync("dongcoi14122003@gmail.com", "test send email", "job");
        return "ok";
    }
}
