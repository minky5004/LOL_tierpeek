package com.loltracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddFriendRequest {
    @NotBlank(message = "gameName은 필수입니다")
    private String gameName;

    @NotBlank(message = "tagLine은 필수입니다")
    private String tagLine;

    private String platform;
}
