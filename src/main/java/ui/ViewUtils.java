package ui;

import javafx.scene.Node;

// Shared by the Cube/Session/Timer screens' "click away clears the current selection" behavior.
final class ViewUtils {

    private ViewUtils() {
    }

    static boolean isDescendant(Node ancestor, Node node) {
        while (node != null) {
            if (node == ancestor) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }
}
