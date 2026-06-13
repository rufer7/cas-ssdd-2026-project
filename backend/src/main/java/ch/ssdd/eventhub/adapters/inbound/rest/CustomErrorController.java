package ch.ssdd.eventhub.adapters.inbound.rest;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        int statusCode = (status != null) ? Integer.parseInt(status.toString()) : 500;

        if (statusCode == HttpStatus.BAD_REQUEST.value()) {
            body.put("status", 400);
            body.put("error", "Bad Request");
            body.put("message", "Invalid character encoding in URL query parameter.");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        // Generic fallback for any other low-level container errors
        body.put("status", statusCode);
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred processing the request.");
        return new ResponseEntity<>(body, HttpStatus.valueOf(statusCode));
    }
}
