package interfaces;

import java.util.Map;
import model.User;

public interface IAction {
    /**
     * Executes the action triggered by a menu choice.
     *
     * @param services         A map of service instances.
     * @param views            A map of view instances.
     * @param currentUser      The currently logged-in user.
     * @param controllerData   A shared map for passing data (e.g., filters).
     * @return A string signal (e.g., "LOGOUT", "EXIT") or null to continue.
     * @throws Exception       For any business logic failures (e.g., OperationError).
     */
    String execute(Map<String, Object> services,
                   Map<String, Object> views,
                   User currentUser,
                   Map<String, Object> controllerData) throws Exception;
}
