package controller;

import model.User;
import view.BaseView;
import interfaces.IAction;
import exception.OperationError;
import exception.IntegrityError;
import action.;

import java.util.*;

public abstract class BaseRoleController {
    protected User currentUser;
    protected Map<String, Object> services;
    protected Map<String, Object> views;
    protected Map<String, Object> controllerData;
    protected List<AbstractMap.SimpleEntry<String, Class<? extends IAction>>> menuDefinition;
    protected Map<Class<? extends IAction>, IAction> actionInstances;

    public BaseRoleController(User currentUser, Map<String, Object> services, Map<String, Object> views) {
        this.currentUser = currentUser;
        this.services = services;
        this.views = views;
        this.controllerData = new HashMap<>();
        this.controllerData.put("filters", new HashMap<>());
        this.menuDefinition = new ArrayList<>();
        this.actionInstances = new HashMap<>();

        buildMenu(); // Abstract method
    }

    protected abstract void buildMenu();

    protected IAction getActionInstance(Class<? extends IAction> actionClass) {
        if (!actionInstances.containsKey(actionClass)) {
            try {
                actionInstances.put(actionClass, actionClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate action: " + actionClass.getSimpleName(), e);
            }
        }
        return actionInstances.get(actionClass);
    }

    public String runMenu() {
        BaseView baseView = (BaseView) views.get("base");

        List<String> menuOptions = new ArrayList<>();
        Map<Integer, IAction> actionMap = new HashMap<>();

        int index = 1;
        for (AbstractMap.SimpleEntry<String, Class<? extends IAction>> entry : menuDefinition) {
            menuOptions.add(entry.getKey());
            if (entry.getValue() != null) {
                actionMap.put(index, getActionInstance(entry.getValue()));
                index++;
            }
        }

        Integer choiceIndex = baseView.displayMenu(currentUser.getRole().toString() + " Menu", menuOptions);
        if (choiceIndex == null) return null;

        IAction selectedAction = actionMap.get(choiceIndex);
        if (selectedAction != null) {
            try {
                return selectedAction.execute(services, views, currentUser, controllerData);
            } catch (OperationError | IntegrityError e) {
                baseView.displayMessage(e.getMessage(), true, false, false);
            } catch (Exception e) {
                baseView.displayMessage("An unexpected error occurred: " + e.getMessage(), true, false, false);
            }
        } else if (menuOptions.get(choiceIndex - 1).startsWith("---")) {
            // Do nothing for separators
        } else {
            baseView.displayMessage("Invalid menu option selected.", true, false, false);
        }

        baseView.pauseForUser();
        return null;
    }

    protected List<AbstractMap.SimpleEntry<String, Class<? extends IAction>>> getCommonMenuItems() {
        return List.of(
            new AbstractMap.SimpleEntry<>("--- General Actions ---", null),
            new AbstractMap.SimpleEntry<>("Change Password", ChangePasswordAction.class),
            new AbstractMap.SimpleEntry<>("Logout", LogoutAction.class),
            new AbstractMap.SimpleEntry<>("Exit System", ExitAction.class)
        );
    }
}

