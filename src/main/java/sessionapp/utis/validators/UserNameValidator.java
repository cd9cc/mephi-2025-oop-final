package sessionapp.utis.validators;

import java.util.regex.Pattern;
import sessionapp.models.validated.Invalid;
import sessionapp.models.validated.Valid;
import sessionapp.models.validated.Validated;

public final class UserNameValidator {

  private static final String allowedChars = "[а-яА-Яa-zA-Z1234567890,.\\-]";
  private static final Pattern forbiddenCharsRegex =
      Pattern.compile("[^а-яА-Яa-zA-Z1234567890,.\\-]");

  public static Validated validate(String userName) {
    var sb = new StringBuilder();
    if (userName == null) {
      sb.append("имя пользователя не должно быть пустым");
      return new Invalid(sb.toString());
    }
    if (userName.isBlank()) {
      sb.append("имя пользователя не должно быть пустым");
    }

    if (forbiddenCharsRegex.matcher(userName).matches()) {
      sb.append("имя пользователя содержит запрещённые символы, используйте символы из множества: " + allowedChars);
    }

    if (sb.isEmpty()) {
      return new Valid();
    } else {
      return new Invalid(sb.toString());
    }
  }
}
