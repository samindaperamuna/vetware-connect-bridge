package com.fifthgen.prahranvet.vetwarebridge.ui;

import com.fifthgen.prahranvet.vetwarebridge.Application;
import com.fifthgen.prahranvet.vetwarebridge.data.ConnectAPI;
import com.fifthgen.prahranvet.vetwarebridge.data.callback.GetOrderCallback;
import com.fifthgen.prahranvet.vetwarebridge.data.model.*;
import com.fifthgen.prahranvet.vetwarebridge.ui.factory.TableFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class OrderController implements Initializable {

    public volatile int orderId;
    public Stage stage;

    @FXML
    private Label title;

    @FXML
    private Label poNumber;

    @FXML
    private Label status;

    @FXML
    private Label name;

    @FXML
    private Label email;

    @FXML
    private Label phone;

    @FXML
    private Label authorizedBy;

    @FXML
    private Label accountCode;

    @FXML
    private Label addressLine1;

    @FXML
    private Label state;

    @FXML
    private Label postcode;

    @FXML
    private Label country;

    @FXML
    private Label addressLine2;

    @FXML
    private Label suburb;

    @FXML
    private Label notes;

    @FXML
    private Label reqDeliveryDate;

    @FXML
    private Label shipWithNextOrder;

    @FXML
    private Label pickUp;

    @FXML
    private Label deliveryInstructions;

    @FXML
    private Label invoiceWithGoods;

    @FXML
    private TableView<OrderLine> orderLinesTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Thread(() -> {
            // Wait till the orderId is updated.
            while (orderId == 0) {
                Logger.getGlobal().info("Waiting till order is populated.");
            }

            new Thread(new OrderTableUpdater(orderId)).start();
        }).start();
    }

    private class OrderTableUpdater implements Runnable {

        private int orderId;

        OrderTableUpdater(int orderId) {
            this.orderId = orderId;
        }

        @Override
        public void run() {
            ConnectAPI api = new ConnectAPI(Application.propertyManager);
            api.getOrder(this.orderId, new GetOrderCallback() {

                private void updateLabel(Object value, String template, Label label) {
                    if (value != null) {
                        label.setText(label.getText().replace(template, value.toString()));
                    } else {
                        label.setText(label.getText().replace(template, ""));
                    }
                }

                @Override
                public void onCompleted(Order order) {
                    Platform.runLater(() -> {
                        String strOrderId = order.getId();
                        updateLabel(strOrderId, "{orderId}", title);

                        String strPONumber = order.getPurchaseOrderNumber();
                        updateLabel(strPONumber, "{purchaseOrderNumber}", poNumber);

                        String strStatus = order.getStatus();
                        updateLabel(strStatus, "{status}", status);

                        // Sender details.
                        Sender sender = order.getSender();
                        if (sender != null) {
                            String strName = sender.getName();
                            updateLabel(strName, "{name}", name);

                            String strEmail = sender.getEmail();
                            updateLabel(strEmail, "{email}", email);

                            String strPhone = sender.getPhone();
                            updateLabel(strPhone, "{phone}", phone);

                            String strAuthBy = sender.getAuthorisedBy();
                            updateLabel(strAuthBy, "{authorizedBy}", authorizedBy);

                            String strAccCode = sender.getAccountCode();
                            updateLabel(strAccCode, "{accountCode}", accountCode);
                        }

                        // Ship to details.
                        Address shipTo = order.getShipTo();
                        if (shipTo != null) {
                            String strAddrL1 = shipTo.getAddressLine1();
                            updateLabel(strAddrL1, "{addressLine1}", addressLine1);

                            String strAddrL2 = shipTo.getAddressLine2();
                            updateLabel(strAddrL2, "{addressLine2}", addressLine2);

                            String strSuburb = shipTo.getSuburb();
                            updateLabel(strSuburb, "{suburb}", suburb);

                            String strState = shipTo.getState();
                            updateLabel(strState, "{state}", state);

                            Integer strPostcode = shipTo.getPostcode();
                            updateLabel(strPostcode, "{postcode}", postcode);

                            String strCountry = shipTo.getCountry();
                            updateLabel(strCountry, "{country}", country);
                        }

                        // Additional order instructions.
                        AdditionalOrderInstructions aoi = order.getAdditionalOrderInstructions();

                        if (aoi != null) {
                            String strNotes = aoi.getNotes();
                            updateLabel(strNotes, "{notes}", notes);

                            String strDelIns = aoi.getDeliveryInstructions();
                            updateLabel(strDelIns, "{deliveryInstructions}", deliveryInstructions);

                            Boolean bInvWithGoods = aoi.getIncludeInvoiceWithGoods();
                            updateLabel(bInvWithGoods, "{includeInvoiceWithGoods}", invoiceWithGoods);

                            Date dReqDelDate = aoi.getRequestedDeliveryDate();
                            if (dReqDelDate != null)
                                updateLabel(TableFactory.DATE_FORMAT.format(dReqDelDate),
                                        "{requestedDeliveryDate}", reqDeliveryDate);
                            else updateLabel("-", "{requestedDeliveryDate}", reqDeliveryDate);

                            Boolean bSWNOrder = aoi.getShipWithNextOrder();
                            updateLabel(bSWNOrder, "{shipWithNextOrder}", shipWithNextOrder);

                            Boolean bPickUp = order.getAdditionalOrderInstructions().getPickUp();
                            updateLabel(bPickUp.toString(), "{pickUp}", pickUp);
                        }

                        ObservableList<OrderLine> data = FXCollections.observableArrayList(order.getOrderLines());

                        TableColumn<OrderLine, String> lineItemNum = new TableColumn<>("Item No.");
                        lineItemNum.setCellValueFactory(new PropertyValueFactory<>("lineItemNumber"));

                        TableColumn<OrderLine, String> productCode = new TableColumn<>("Product Code");
                        productCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));

                        TableColumn<OrderLine, String> description = new TableColumn<>("Description");
                        description.setCellValueFactory(new PropertyValueFactory<>("description"));

                        TableColumn<OrderLine, Integer> qty = new TableColumn<>("Qty.");
                        qty.setCellValueFactory(new PropertyValueFactory<>("quantity"));

                        TableColumn<OrderLine, Double> price = new TableColumn<>("Price");
                        price.setCellValueFactory(new PropertyValueFactory<>("price"));

                        TableColumn<OrderLine, Boolean> crit = new TableColumn<>("Crit.");
                        crit.setCellValueFactory(new PropertyValueFactory<>("critical"));

                        TableColumn<OrderLine, String> notes = new TableColumn<>("Notes");
                        notes.setCellValueFactory(new PropertyValueFactory<>("notes"));

                        orderLinesTable.getColumns().clear();
                        orderLinesTable.getColumns().add(lineItemNum);
                        orderLinesTable.getColumns().add(productCode);
                        orderLinesTable.getColumns().add(description);
                        orderLinesTable.getColumns().add(qty);
                        orderLinesTable.getColumns().add(price);
                        orderLinesTable.getColumns().add(crit);
                        orderLinesTable.getColumns().add(notes);

                        orderLinesTable.setItems(data);
                        orderLinesTable.setColumnResizePolicy(param -> true);
                    });
                }

                @Override
                public void onFailed(Exception e) {
                    Platform.runLater(() -> {
                        // Show error.
                    });
                }
            });
        }
    }
}