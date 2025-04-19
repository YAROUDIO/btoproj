package action.commonAction;

import interfaces.IAction;
import service.AuthService;
import view.AuthView;
import view.BaseView;
import model.User;
import exception.OperationError;

import java.util.Map;

public class ChangePasswordAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) {
        if (currentUser == null) {
            ((BaseView) views.get("base")).displayMessage("Cannot change password. No user logged in.", true, false, false);
            return null;
        }

        AuthService authService = (AuthService) services.get("auth");
        AuthView authView = (AuthView) views.get("auth");
        BaseView baseView = (BaseView) views.get("base");

        String currentPasswordAttempt = authView.getPassword("Enter your CURRENT password for verification");
        if (!currentUser.checkPassword(currentPasswordAttempt)) {
            baseView.displayMessage("Verification failed: Incorrect current password.", true, false, false);
            return null;
        }

        String newPassword = authView.promptChangePassword();
        if (newPassword != null) {
            try {
                authService.changePassword(currentUser, newPassword);
                baseView.displayMessage("Password changed successfully.", false, true, false);
            } catch (OperationError e) {
                baseView.displayMessage(e.getMessage(), true, false, false);
            }
        }
        return null;
    }
}
