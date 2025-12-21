package ci553.happyshop.client.customer;

import ci553.happyshop.utility.UIStyle;
import ci553.happyshop.utility.WinPosManager;
import ci553.happyshop.utility.WindowBounds;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Updated to work with Flexible Search (ID or Name).
 */
public class CustomerView  {
    public CustomerController cusController;

    public javafx.scene.control.Spinner<Integer> spQty;
    public javafx.scene.control.Button btnAddMultiple;

    private final int WIDTH = UIStyle.customerWinWidth;
    private final int HEIGHT = UIStyle.customerWinHeight;
    private final int COLUMN_WIDTH = WIDTH / 2 - 10;

    private HBox hbRoot;
    private VBox vbTrolleyPage;
    private VBox vbReceiptPage;

    TextField tfId;
    TextField tfName;

    private ImageView ivProduct;
    private Label lbProductInfo;
    private TextArea taTrolley;
    private TextArea taReceipt;

    private Stage viewWindow;

    public void start(Stage window) {
        VBox vbSearchPage = createSearchPage();
        vbTrolleyPage = CreateTrolleyPage();
        vbReceiptPage = createReceiptPage();

        Line line = new Line(0, 0, 0, HEIGHT);
        line.setStrokeWidth(4);
        line.setStroke(Color.PINK);

        VBox lineContainer = new VBox(line);
        lineContainer.setPrefWidth(4);
        lineContainer.setAlignment(Pos.CENTER);

        hbRoot = new HBox(10, vbSearchPage, lineContainer, vbTrolleyPage);
        hbRoot.setAlignment(Pos.CENTER);
        hbRoot.setStyle(UIStyle.rootStyle);

        Scene scene = new Scene(hbRoot, WIDTH, HEIGHT);
        window.setScene(scene);
        window.setTitle("🛒 HappyShop Customer Client");
        WinPosManager.registerWindow(window, WIDTH, HEIGHT);
        window.show();
        viewWindow = window;
    }

    private VBox createSearchPage() {
        Label laPageTitle = new Label("Search by Product ID/Name");
        laPageTitle.setStyle(UIStyle.labelTitleStyle);

        // dual input fields implemented

        Label laId = new Label("ID:");
        laId.setStyle(UIStyle.labelStyle);
        tfId = new TextField();
        tfId.setPromptText("e.g. 0001");
        tfId.setStyle(UIStyle.textFiledStyle);
        HBox hbId = new HBox(10, laId, tfId);

        Label laName = new Label("Name:");
        laName.setStyle(UIStyle.labelStyle);
        tfName = new TextField();
        tfName.setPromptText("e.g. watch");
        tfName.setStyle(UIStyle.textFiledStyle);
        HBox hbName = new HBox(10, laName, tfName);

        Label laPlaceHolder = new Label(" ".repeat(15));

        Button btnSearch = new Button("Search");
        btnSearch.setStyle(UIStyle.buttonStyle);
        btnSearch.setOnAction(this::buttonClicked);

        Button btnAddToTrolley = new Button("Add to Trolley");
        btnAddToTrolley.setStyle(UIStyle.buttonStyle);
        btnAddToTrolley.setOnAction(this::buttonClicked);

        // quantity spinner + add multiple button

        spQty = new Spinner<>(1, 50, 1);
        spQty.setEditable(true);
        spQty.setPrefWidth(85);

        btnAddMultiple = new Button("Add Multiple");
        btnAddMultiple.setStyle(UIStyle.buttonStyle);
        btnAddMultiple.setOnAction(this::buttonClicked);

        HBox hbBtns = new HBox(10, laPlaceHolder, btnSearch, btnAddToTrolley, spQty, btnAddMultiple);
        hbBtns.setAlignment(Pos.CENTER_LEFT);

        ivProduct = new ImageView("imageHolder.jpg");
        ivProduct.setFitHeight(60);
        ivProduct.setFitWidth(60);
        ivProduct.setPreserveRatio(true);
        ivProduct.setSmooth(true);

        lbProductInfo = new Label("Use ID or Name to search.");
        lbProductInfo.setWrapText(true);
        lbProductInfo.setStyle(UIStyle.labelMulLineStyle);

        HBox hbSearchResult = new HBox(5, ivProduct, lbProductInfo);
        hbSearchResult.setAlignment(Pos.CENTER_LEFT);

        return new VBox(15, laPageTitle, hbId, hbName, hbBtns, hbSearchResult);
    }

    private VBox CreateTrolleyPage() {
        Label laPageTitle = new Label("🛒🛒  Trolley 🛒🛒");
        laPageTitle.setStyle(UIStyle.labelTitleStyle);

        taTrolley = new TextArea();
        taTrolley.setEditable(false);
        taTrolley.setPrefSize(WIDTH/2, HEIGHT-50);

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle(UIStyle.buttonStyle);
        btnCancel.setOnAction(this::buttonClicked);

        Button btnCheckout = new Button("Check Out");
        btnCheckout.setStyle(UIStyle.buttonStyle);
        btnCheckout.setOnAction(this::buttonClicked);

        HBox hbBtns = new HBox(10, btnCancel, btnCheckout);
        hbBtns.setAlignment(Pos.CENTER);

        return new VBox(15, laPageTitle, taTrolley, hbBtns);
    }

    private VBox createReceiptPage() {
        Label laPageTitle = new Label("Receipt");
        laPageTitle.setStyle(UIStyle.labelTitleStyle);

        taReceipt = new TextArea();
        taReceipt.setEditable(false);
        taReceipt.setPrefSize(WIDTH/2, HEIGHT-50);

        Button btnCloseReceipt = new Button("OK & Close");
        btnCloseReceipt.setStyle(UIStyle.buttonStyle);
        btnCloseReceipt.setOnAction(this::buttonClicked);

        return new VBox(15, laPageTitle, taReceipt, btnCloseReceipt);
    }

    private void buttonClicked(ActionEvent event) {
        try {
            Button btn = (Button) event.getSource();
            String action = btn.getText();

            if (action.equals("Add to Trolley")) {
                showTrolleyOrReceiptPage(vbTrolleyPage);
            }

            // also show trolley after adding multiple items
            if (action.equals("Add Multiple")) {
                showTrolleyOrReceiptPage(vbTrolleyPage);
            }

            if (action.equals("OK & Close")) {
                showTrolleyOrReceiptPage(vbTrolleyPage);
            }

            cusController.doAction(action);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }


    // now supports multiple search results & name searches

    public void update(String imageName, String searchResult, String trolley, String receipt) {

        //  if multi-search is detected, hide image & only show text
        if (searchResult.startsWith("Search Results:")) {
            ivProduct.setImage(new Image("imageHolder.jpg"));   // Clear image
        } else {
            ivProduct.setImage(new Image(imageName));
        }

        lbProductInfo.setText(searchResult);
        taTrolley.setText(trolley);

        if (!receipt.equals("")) {
            showTrolleyOrReceiptPage(vbReceiptPage);
            taReceipt.setText(receipt);
        }
    }

    private void showTrolleyOrReceiptPage(Node pageToShow) {
        int lastIndex = hbRoot.getChildren().size() - 1;
        hbRoot.getChildren().set(lastIndex, pageToShow);
    }

    WindowBounds getWindowBounds() {
        return new WindowBounds(
                viewWindow.getX(),
                viewWindow.getY(),
                viewWindow.getWidth(),
                viewWindow.getHeight()
        );
    }
}
