package com.bookstore.bookstore.presentation.support;

import com.bookstore.bookstore.application.command.AuthRequestMetadata;
import com.bookstore.bookstore.infrastructure.security.AuthSecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientRequestMetadataResolver {

    private final AuthSecurityProperties properties;

    public AuthRequestMetadata resolve(HttpServletRequest request) {
        return new AuthRequestMetadata(
                resolveIp(request),
                truncate(request.getHeader("User-Agent"), 500),
                truncate(request.getHeader("X-Device-Id"), 128),
                truncate(request.getHeader("X-Device-Name"), 160)
        );
    }

    public String resolveIp(HttpServletRequest request) {
        String remote = canonicalIp(request == null ? null : request.getRemoteAddr());
        if (remote == null || request == null || properties.trustedProxy() == null
                || !properties.trustedProxy().enabled() || !isTrustedProxy(remote)) {
            return remote;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return remote;
        }

        List<String> chain = new ArrayList<>();
        for (String value : forwardedFor.split(",")) {
            String candidate = canonicalIp(value);
            if (candidate != null) {
                chain.add(candidate);
            }
        }

        for (int index = chain.size() - 1; index >= 0; index--) {
            String candidate = chain.get(index);
            if (!isTrustedProxy(candidate)) {
                return candidate;
            }
        }
        return chain.isEmpty() ? remote : chain.get(0);
    }

    private boolean isTrustedProxy(String ip) {
        List<String> cidrs = properties.trustedProxy().cidrs();
        if (cidrs == null) {
            return false;
        }
        return cidrs.stream().anyMatch(cidr -> matchesCidr(ip, cidr));
    }

    private boolean matchesCidr(String ip, String cidr) {
        try {
            if (cidr == null || cidr.isBlank()) {
                return false;
            }
            String[] parts = cidr.trim().split("/", 2);
            InetAddress address = InetAddress.getByName(ip);
            InetAddress network = InetAddress.getByName(parts[0]);
            if (address.getAddress().length != network.getAddress().length) {
                return false;
            }
            int bits = parts.length == 2 ? Integer.parseInt(parts[1]) : address.getAddress().length * 8;
            int totalBits = address.getAddress().length * 8;
            if (bits < 0 || bits > totalBits) {
                return false;
            }
            BigInteger mask = BigInteger.ONE.shiftLeft(totalBits).subtract(BigInteger.ONE)
                    .shiftRight(totalBits - bits).shiftLeft(totalBits - bits);
            BigInteger value = new BigInteger(1, address.getAddress());
            BigInteger networkValue = new BigInteger(1, network.getAddress());
            return value.and(mask).equals(networkValue.and(mask));
        } catch (Exception ignored) {
            return false;
        }
    }

    private String canonicalIp(String value) {
        try {
            if (value == null || value.isBlank() || value.length() > 64) {
                return null;
            }
            return InetAddress.getByName(value.trim()).getHostAddress();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }
}
