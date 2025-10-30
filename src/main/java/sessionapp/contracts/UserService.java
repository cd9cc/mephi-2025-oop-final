package sessionapp.contracts;

import sessionapp.exceptions.AuthenticationFailedException;
import sessionapp.exceptions.BadConfigurationException;
import sessionapp.exceptions.UserAlreadyExistsException;
import sessionapp.models.User;

public interface UserService {
  User registerUser(String username, String password)
      throws UserAlreadyExistsException, BadConfigurationException;

  User authenticateAsUser(String username, String password)
      throws AuthenticationFailedException, BadConfigurationException;
}
