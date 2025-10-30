package sessionapp.utis.validators;

import sessionapp.models.validated.Invalid;
import sessionapp.models.validated.Valid;
import sessionapp.models.validated.Validated;

public final class CategoryNameValidator {
  public static Validated validate(String categoryName) {
    var sb = new StringBuilder();
    if (categoryName == null) {
      sb.append("category name is null");
      return new Invalid(sb.toString());
    }
    if (categoryName.isBlank()) {
      sb.append("category name must not be empty/blank");
    }

    if (sb.isEmpty()) {
      return new Valid();
    } else {
      return new Invalid(sb.toString());
    }
  }
}
