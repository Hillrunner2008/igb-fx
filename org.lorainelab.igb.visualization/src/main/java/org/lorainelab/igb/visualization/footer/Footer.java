package org.lorainelab.igb.visualization.footer;

import aQute.bnd.annotation.component.Component;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = Footer.class)
public class Footer extends HBox {

    private static final double FIXED_HEIGHT = 25.0;
    private MemoryTracker memoryTracker;
    private Pane spacer;

    public Footer() {
        setMaxHeight(FIXED_HEIGHT);
        setPrefHeight(FIXED_HEIGHT);
        memoryTracker = new MemoryTracker();
        spacer = new Pane();
        spacer.setMaxHeight(FIXED_HEIGHT);
        spacer.setPrefHeight(FIXED_HEIGHT);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(memoryTracker, spacer);
    }

}
// <HBox maxHeight="25.0" prefHeight="25.0" prefWidth="200.0">
//            <children>
//                <StackPane maxHeight="20.0" maxWidth="250.0" prefHeight="20.0" prefWidth="250.0">
//                    <children>
//                        <ProgressBar fx:id="memoryProgressBar" maxHeight="20.0" prefHeight="20.0" prefWidth="250.0" progress="0.27" />
//                        <Label fx:id="memoryLabel" text="0M of 64M" />
//                        <FontAwesomeIconView fx:id="gcTrashIcon" glyphName="TRASH" StackPane.alignment="CENTER_RIGHT">
//                            <StackPane.margin>
//                                <Insets right="7.0" />
//                            </StackPane.margin>
//                        </FontAwesomeIconView>
//                    </children>
//                    <padding>
//                        <Insets left="5.0" top="5.0" />
//                    </padding>
//                </StackPane>
//                <Pane HBox.hgrow="ALWAYS" />
//                <StatusBar fx:id="statusBar" maxWidth="500.0" prefWidth="500.0">
//                    <HBox.margin>
//                        <Insets bottom="2.0" left="2.0" right="2.0" top="2.0" />
//                    </HBox.margin>
//                </StatusBar>
//            </children>
//        </HBox>
