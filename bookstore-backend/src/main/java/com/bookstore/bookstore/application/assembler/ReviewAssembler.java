package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.port.out.IProfileRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.ReviewResult;
import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.domain.model.Review;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReviewAssembler {

    private final IUserRepository userRepository;
    private final IProfileRepository profileRepository;

    public ReviewAssembler(IUserRepository userRepository, IProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    public ReviewResult toResult(Review review) {
        return new ReviewResult(
                review.getId(),
                review.getUserId(),
                review.getBookId(),
                review.getOrderItemId(),
                resolveDisplayName(review.getUserId()),
                resolveAvatarUrl(review.getUserId()),
                true,
                List.of(),
                0L,
                review.getRating(),
                review.getComment(),
                review.getStatus(),
                review.getModerationReason(),
                review.getModeratedBy(),
                resolveDisplayName(review.getModeratedBy()),
                review.getModeratedAt(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    private String resolveDisplayName(UUID userId) {
        if (userId == null) {
            return null;
        }

        User user = userRepository.findByIdIncludingDeleted(userId).orElse(null);
        Profile profile = profileRepository.findByUserIdIncludingDeleted(userId).orElse(null);
        return resolveDisplayName(user, profile);
    }

    private String resolveAvatarUrl(UUID userId) {
        if (userId == null) {
            return null;
        }

        Profile profile = profileRepository.findByUserIdIncludingDeleted(userId).orElse(null);
        return profile != null ? profile.getAvatarUrl() : null;
    }

    private String resolveDisplayName(User user, Profile profile) {
        if (profile != null) {
            String fullName = ((profile.getLastName() == null ? "" : profile.getLastName()) + " "
                    + (profile.getFirstName() == null ? "" : profile.getFirstName())).trim();
            if (!StringUtils.isBlank(fullName)) {
                return fullName;
            }
        }
        if (user != null && !StringUtils.isBlank(user.getUsername())) {
            return user.getUsername();
        }
        return null;
    }
}
