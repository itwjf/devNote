package com.example.devnote.dto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 UserRegistrationDto 上的 Bean Validation 约束
 */
public class UserRegistrationDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldHaveViolationWhenFieldsAreBlank() {
        UserRegistrationDto dto = new UserRegistrationDto();
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    void shouldValidateSuccessForProperDto() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("user1");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        dto.setEmail("user@example.com");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldDetectInvalidEmail() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("user1");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        dto.setEmail("not-an-email");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertThat(violations).anyMatch(v -> "邮箱格式不正确".equals(v.getMessage()));
    }
}
