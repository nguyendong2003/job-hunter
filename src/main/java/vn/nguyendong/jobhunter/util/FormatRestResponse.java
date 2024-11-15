package vn.nguyendong.jobhunter.util;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import jakarta.servlet.http.HttpServletResponse;
import vn.nguyendong.jobhunter.domain.response.RestResponse;
import vn.nguyendong.jobhunter.util.annotation.ApiMessage;

@ControllerAdvice
public class FormatRestResponse implements ResponseBodyAdvice<Object> {

    /*
     * Hàm supports này return true; thì đều ghi đè lại response của các controller
     * 
     * Nếu controller nào không muốn ghi đề lại response thì có thể check điều kiện
     * trong hàm này và return false
     * 
     * => Khi hàm supports được return true thì mới chạy xuống hàm beforeBodyWrite()
     * 
     */
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    @Nullable
    public Object beforeBodyWrite(
            @Nullable Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int status = servletResponse.getStatus();

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(status);

        if (body instanceof String || body instanceof Resource) {
            return body;
        }

        // case swagger
        String path = request.getURI().getPath();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return body;
        }

        // case error
        if (status >= 400) {
            return body;
        }
        // case success
        else {
            res.setData(body);
            ApiMessage message = returnType.getMethodAnnotation(ApiMessage.class);
            res.setMessage(message != null ? message.value() : "CALL API SUCCESS");
        }

        return res;
    }

}
