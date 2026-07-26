package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.result.PersonalizedRecommendationResult;
import java.util.UUID;

public interface IPersonalizedRecommendationService {

    PersonalizedRecommendationResult getForUser(UUID userId, int limit);
}
