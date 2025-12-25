package ci553.happyshop.utility;

/**
 * UIStyle is a centralized Java record that holds all JavaFX UI-related style and size constants
 * used across all client views in the system.
 *
 * These values are grouped here rather than being hardcoded throughout the codebase:
 * - improves maintainability, ensures style consistency,
 * - avoids hardcoded values scattered across the codebase.
 *
 * Example usages:
 * - UIStyle.HistoryWinHeight for setting the height of the order history window
 * - UIStyle.labelStyle for applying consistent styling to labels
 *
 * UIStyle is deliberately defined as a `record` instead of a normal class for several reasons:
 *  - Lightweight and memory-efficient: Records are designed to be compact data carriers
 *    with minimal memory overhead compared to traditional classes.
 *  - No instance needed: Since this holds only static constants, using a record clearly
 *    communicates that no state or behavior is expected.
 *  - Final and immutable by default: Records cannot be extended and implicitly prevent misuse.
 *  - Cleaner syntax: Avoids unnecessary boilerplate (constructors, getters, etc.).
 */

public final class UIStyle {

    // Private constructor prevents instantiation
    private UIStyle() {
        throw new UnsupportedOperationException("UIStyle is a utility class");
    }

    public static final int customerWinWidth = 600;
    public static final int customerWinHeight = 300;
    public static final int removeProNotifierWinWidth = customerWinWidth / 2 + 160;
    public static final int removeProNotifierWinHeight = 230;

    public static final int pickerWinWidth = 310;
    public static final int pickerWinHeight = 300;

    public static final int trackerWinWidth = 210;
    public static final int trackerWinHeight = 300;

    public static final int warehouseWinWidth = 630;
    public static final int warehouseWinHeight = 300;
    public static final int AlertSimWinWidth = 300;
    public static final int AlertSimWinHeight = 170;
    public static final int HistoryWinWidth = 300;
    public static final int HistoryWinHeight = 140;

    public static final int EmergencyExitWinWidth = 200;
    public static final int EmergencyExitWinHeight = 300;

    public static final String labelTitleStyle =
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #212529;";

    public static final String labelStyle =
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #343a40;";

    public static final String comboBoxStyle =
            "-fx-font-size: 14px; -fx-font-weight: bold;";

    public static final String buttonStyle =
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #339af0; " +
                    "-fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 10px; -fx-border-radius: 10px;";

    public static final String rootStyle =
            "-fx-padding: 12px; -fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef);";

    public static final String rootStyleBlue =
            "-fx-padding: 12px; -fx-background-color: linear-gradient(to bottom, #e7f5ff, #d0ebff);";

    public static final String rootStyleGray =
            "-fx-padding: 12px; -fx-background-color: linear-gradient(to bottom, #f1f3f5, #e9ecef);";

    public static final String rootStyleWarehouse =
            "-fx-padding: 12px; -fx-background-color: linear-gradient(to bottom, #fff0f6, #ffe3ec);";

    public static final String rootStyleYellow =
            "-fx-padding: 12px; -fx-background-color: linear-gradient(to bottom, #fff9db, #ffec99);";

    public static final String textFiledStyle =
            "-fx-font-size: 14px; -fx-padding: 6px; -fx-background-color: white; -fx-border-color: #ced4da; " +
                    "-fx-border-radius: 8px; -fx-background-radius: 8px;";

    public static final String labelMulLineStyle =
            "-fx-font-size: 14px; -fx-text-fill: #212529; -fx-background-color: white; -fx-border-color: #dee2e6; " +
                    "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px;";

    public static final String listViewStyle =
            "-fx-font-size: 14px; -fx-control-inner-background: white; -fx-border-color: #dee2e6; " +
                    "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px;";

    public static final String manageStockChildStyle =
            "-fx-background-color: #f1f3f5; -fx-border-color: #dee2e6; -fx-border-width: 1px; -fx-padding: 5px;";

    public static final String manageStockChildStyle1 =
            "-fx-background-color: #fff9db; -fx-border-color: #ffe066; -fx-border-width: 1px; -fx-padding: 5px;";

    public static final String greenFillBtnStyle =
            "-fx-background-color: #37b24d; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-background-radius: 10px; -fx-border-radius: 10px;";

    public static final String redFillBtnStyle =
            "-fx-background-color: #f03e3e; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-background-radius: 10px; -fx-border-radius: 10px;";

    public static final String grayFillBtnStyle =
            "-fx-background-color: #868e96; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-background-radius: 10px; -fx-border-radius: 10px;";

    public static final String blueFillBtnStyle =
            "-fx-background-color: #228be6; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-background-radius: 10px; -fx-border-radius: 10px;";

    public static final String alertBtnStyle =
            "-fx-background-color: #339af0; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-background-radius: 10px; -fx-border-radius: 10px;";

    public static final String alertTitleLabelStyle =
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f03e3e; -fx-background-color: #e7f5ff;";

    public static final String alertContentTextAreaStyle =
            "-fx-font-size: 14px; -fx-control-inner-background: #fff9db; -fx-text-fill: #1c7ed6;";

    public static final String alertContentUserActionStyle =
            "-fx-font-size: 14px; -fx-text-fill: #37b24d;";
}
