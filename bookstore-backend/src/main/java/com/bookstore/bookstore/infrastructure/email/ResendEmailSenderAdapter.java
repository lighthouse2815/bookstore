package com.bookstore.bookstore.infrastructure.email;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IEmailSender;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class ResendEmailSenderAdapter implements IEmailSender {

    private final RestClient resendRestClient;
    private final ResendProperties resendProperties;

    @Override
    public void sendOtpEmail(String recipientEmail, String otpCode, long expirationMinutes) {
        validateConfiguration();

        SendEmailRequest request = new SendEmailRequest(
                formatFrom(),
                List.of(recipientEmail),
                "Ma OTP xac thuc tai khoan Bookstore",
                buildTextBody(otpCode, expirationMinutes),
                buildHtmlBody(otpCode, expirationMinutes)
        );

        try {
            resendRestClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendProperties.apiKey())
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new ApplicationException(ApplicationErrorCode.OTP_EMAIL_SEND_FAILED);
        }
    }

    private void validateConfiguration() {
        if (StringUtils.isBlank(resendProperties.baseUrl())
                || StringUtils.isBlank(resendProperties.apiKey())
                || StringUtils.isBlank(resendProperties.fromEmail())) {
            throw new ApplicationException(ApplicationErrorCode.OTP_EMAIL_NOT_CONFIGURED);
        }
    }

    private String formatFrom() {
        String fromName = StringUtils.trimToNull(resendProperties.fromName());
        if (fromName == null) {
            return resendProperties.fromEmail();
        }
        return fromName + " <" + resendProperties.fromEmail() + ">";
    }

    private String buildTextBody(String otpCode, long expirationMinutes) {
        return "Ma OTP cua ban la " + otpCode
                + ". Ma co hieu luc trong "
                + expirationMinutes
                + " phut.";
    }

    private String buildHtmlBody(String otpCode, long expirationMinutes) {
        return """
                <div style="font-family:Arial,sans-serif;line-height:1.6;color:#111827">
                  <h2 style="margin-bottom:12px">Xac thuc tai khoan Bookstore</h2>
                  <p>Su dung ma OTP ben duoi de kich hoat tai khoan cua ban:</p>
                  <div style="display:inline-block;padding:12px 20px;margin:12px 0;background:#111827;color:#ffffff;font-size:24px;font-weight:700;letter-spacing:4px">
                    %s
                  </div>
                  <p>Ma co hieu luc trong %d phut.</p>
                  <p>Neu ban khong thuc hien yeu cau nay, vui long bo qua email.</p>
                </div>
                """.formatted(otpCode, expirationMinutes);
    }

    private record SendEmailRequest(
            String from,
            List<String> to,
            String subject,
            String text,
            String html
    ) {
    }
}
