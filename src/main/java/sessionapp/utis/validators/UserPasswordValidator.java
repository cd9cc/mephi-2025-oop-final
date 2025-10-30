package sessionapp.utis.validators;

import sessionapp.models.validated.Invalid;
import sessionapp.models.validated.Valid;
import sessionapp.models.validated.Validated;

public final class UserPasswordValidator {

  private static final int minLength = 3;

  public static Validated validate(String userName) {
    var sb = new StringBuilder();
    if (userName == null) {
      sb.append("пароль не должен быть пустым");
      return new Invalid(sb.toString());
    }
    if (userName.isBlank()) {
      sb.append("пароль не должен быть пустым");
    }

    if (userName.length() < minLength) {
      sb.append("пароль должен быть длиннее ");
      sb.append(minLength);
      sb.append(" символов");
    }

    if (sb.isEmpty()) {
      return new Valid();
    } else {
      return new Invalid(sb.toString());
    }
  }
}
