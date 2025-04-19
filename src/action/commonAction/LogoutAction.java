package action.commonAction;
import java.util.Map;

import interfaces.IAction;
import model.User;

public class LogoutAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) {
        return "LOGOUT";
    }
}
