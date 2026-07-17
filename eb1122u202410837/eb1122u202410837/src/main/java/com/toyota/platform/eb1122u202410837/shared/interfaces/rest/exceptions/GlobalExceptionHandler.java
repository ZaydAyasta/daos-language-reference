package com.toyota.platform.eb1122u202410837.shared.interfaces.rest.exceptions;

import com.toyota.platform.eb1122u202410837.shared.domain.exceptions.BusinessRuleException;
import com.toyota.platform.eb1122u202410837.shared.interfaces.rest.resources.ErrorResource;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private final MessageSource messageSource;

  public GlobalExceptionHandler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ExceptionHandler(BusinessRuleException.class)
  public ResponseEntity<ErrorResource> handleBusinessRuleException(
      BusinessRuleException exception, Locale locale) {
    return badRequest(exception.code(), locale);
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResource> handleIllegalArgumentException(
      RuntimeException exception, Locale locale) {
    return new ResponseEntity<>(
        new ErrorResource("request.invalid", resolve("request.invalid", locale) + ": " + exception.getMessage()),
        HttpStatus.BAD_REQUEST
    );
  }

  @ExceptionHandler(DateTimeParseException.class)
  public ResponseEntity<ErrorResource> handleDateTimeParseException(Locale locale) {
    return badRequest("request.date-time.invalid", locale);
  }

  private ResponseEntity<ErrorResource> badRequest(String code, Locale locale) {
    return new ResponseEntity<>(new ErrorResource(code, resolve(code, locale)), HttpStatus.BAD_REQUEST);
  }

  private String resolve(String code, Locale locale) {
    return messageSource.getMessage(code, null, code, locale);
  }
}
