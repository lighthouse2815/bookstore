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

        SendEmailRequest request = buildEmailRequest(
                recipientEmail,
                otpCode,
                expirationMinutes,
                "Mã OTP xác thực tài khoản Bookstore",
                "Xác thực tài khoản Bookstore",
                "Sử dụng mã OTP bên dưới để kích hoạt tài khoản của bạn:"
        );

        send(request);
    }

    @Override
    public void sendPasswordResetOtpEmail(String recipientEmail, String otpCode, long expirationMinutes) {
        validateConfiguration();

        SendEmailRequest request = buildEmailRequest(
                recipientEmail,
                otpCode,
                expirationMinutes,
                "Mã OTP đặt lại mật khẩu Bookstore",
                "Đặt lại mật khẩu Bookstore",
                "Sử dụng mã OTP bên dưới để đặt lại mật khẩu của bạn:"
        );

        send(request);
    }

    private void send(SendEmailRequest request) {
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

    private SendEmailRequest buildEmailRequest(
            String recipientEmail,
            String otpCode,
            long expirationMinutes,
            String subject,
            String heading,
            String description
    ) {
        return new SendEmailRequest(
                formatFrom(),
                List.of(recipientEmail),
                subject,
                buildTextBody(otpCode, expirationMinutes, description),
                buildHtmlBody(otpCode, expirationMinutes, heading, description)
        );
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

    private String buildTextBody(String otpCode, long expirationMinutes, String description) {
        return description
                + System.lineSeparator()
                + "Mã OTP của bạn là "
                + otpCode
                + ". Mã có hiệu lực trong "
                + expirationMinutes
                + " phút.";
    }

    private String buildHtmlBody(String otpCode, long expirationMinutes, String heading, String description) {
        return """
                <div style="font-family:Arial,sans-serif;line-height:1.6;color:#111827">
                  <h2 style="margin-bottom:12px">%s</h2>
                  <p>%s</p>
                  <div style="display:inline-block;padding:12px 20px;margin:12px 0;background:#111827;color:#ffffff;font-size:24px;font-weight:700;letter-spacing:4px">
                    %s
                  </div>
                  <p>Mã có hiệu lực trong %d phút.</p>
                  <p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.</p>
                </div>
                """.formatted(heading, description, otpCode, expirationMinutes);
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

