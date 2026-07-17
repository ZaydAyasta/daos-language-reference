package com.toyota.platform.eb1122u202410837.shared.domain.exceptions;

public class BusinessRuleException extends RuntimeException {
  private final String code;

  public BusinessRuleException(String code) {
    super(code);
    this.code = code;
  }

  public String code() {
    return code;
  }
}
