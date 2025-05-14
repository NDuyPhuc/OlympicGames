package com.duyphuc.olympics.animation;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class OlympicRingsAnimation {
    private final Pane container;
    private final Group ringsGroup = new Group();
    private Timeline entranceAnimation; // Đổi tên để rõ ràng hơn
    private Timeline pulseAnimation;    // Thêm biến cho pulse animation để có thể dừng

    // Không cần mảng rings và ringColors là thành viên của lớp nữa nếu chỉ dùng trong setupRings
    // private Circle[] rings = new Circle[5];
    // private Color[] ringColors = { ... };

    public OlympicRingsAnimation(Pane container) {
        this.container = container;
        // Gọi setupRings khi container có kích thước hợp lệ
        // Thêm listener để xử lý trường hợp kích thước chưa có ngay
        if (container.getWidth() > 0 && container.getHeight() > 0) {
            setupRings();
        } else {
            container.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0 && container.getHeight() > 0 && ringsGroup.getChildren().isEmpty()) {
                    setupRings();
                }
            });
            container.heightProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0 && container.getWidth() > 0 && ringsGroup.getChildren().isEmpty()) {
                    setupRings();
                }
            });
        }
    }

    private void setupRings() {
        // Xóa các vòng tròn cũ nếu có (trường hợp gọi lại setupRings do resize)
        ringsGroup.getChildren().clear();
        container.getChildren().remove(ringsGroup); // Xóa group cũ khỏi container
        container.getChildren().add(ringsGroup);    // Thêm group mới (hoặc group đã clear)

        // Sử dụng kích thước thực tế của container nếu có, nếu không thì dùng prefSize
        // Điều này quan trọng nếu prefSize không được đặt hoặc container đã được layout
        double actualContainerWidth = (container.getWidth() > 0) ? container.getWidth() : container.getPrefWidth();
        double actualContainerHeight = (container.getHeight() > 0) ? container.getHeight() : container.getPrefHeight();

        if (actualContainerWidth <= 0 || actualContainerHeight <= 0) {
            System.err.println("OlympicRingsAnimation: Container dimensions are invalid for setupRings. Width: " + actualContainerWidth + ", Height: " + actualContainerHeight);
            return; // Không thể vẽ nếu không có kích thước
        }

        // Tính toán kích thước dựa trên chiều cao của container để logo cân đối
        // Giả sử Pane có chiều cao khoảng 120-140 (từ FXML)
        double ringOuterRadius = actualContainerHeight / 4.0; // Bán kính bao ngoài của mỗi vòng
        double strokeProportion = 0.15; // Tỷ lệ độ dày nét vẽ so với bán kính ngoài
        double ringStrokeWidth = ringOuterRadius * strokeProportion;
        double ringInnerRadius = ringOuterRadius - ringStrokeWidth; // Bán kính bên trong (phần màu)

        // Khoảng cách giữa các tâm vòng tròn (điều chỉnh để các vòng xen kẽ đẹp)
        // Khoảng cách ngang giữa các tâm của vòng trên cùng = đường kính + một chút đè lên nhau
        double horizontalSpacing = ringOuterRadius * 2 * 0.85; // 2*R nhưng có đè
        double verticalSpacing = ringOuterRadius * 0.85;     // Khoảng cách dọc giữa tâm hàng trên và hàng dưới

        // Tâm của toàn bộ cụm logo, sẽ căn giữa Pane
        double groupCenterX = actualContainerWidth / 2.0;
        double groupCenterY = actualContainerHeight / 2.0;

        // Tọa độ các vòng tròn (tính toán lại cho đúng chuẩn Olympic)
        // Vòng màu theo thứ tự: Xanh, Vàng, Đen, Lục, Đỏ
        Circle[] rings = new Circle[5];
        Color[] ringColors = {
                Color.valueOf("#0085C7"), // 0: Blue
                Color.valueOf("#F4C300"), // 1: Yellow
                Color.valueOf("#000000"), // 2: Black
                Color.valueOf("#009F3D"), // 3: Green
                Color.valueOf("#DF0024")  // 4: Red
        };

        // Hàng trên: Xanh, Đen, Đỏ
        // Hàng dưới: Vàng, Lục (xen kẽ)

        // Tâm X của vòng Đen (ở giữa hàng trên)
        double blackRingCenterX = groupCenterX;
        // Tâm Y của hàng trên
        double topRowCenterY = groupCenterY - (verticalSpacing / 2.0);

        rings[2] = createRing(blackRingCenterX, topRowCenterY, ringInnerRadius, ringColors[2], ringStrokeWidth); // Đen (Black)

        rings[0] = createRing(blackRingCenterX - horizontalSpacing, topRowCenterY, ringInnerRadius, ringColors[0], ringStrokeWidth); // Xanh (Blue)
        rings[4] = createRing(blackRingCenterX + horizontalSpacing, topRowCenterY, ringInnerRadius, ringColors[4], ringStrokeWidth); // Đỏ (Red)

        // Tâm Y của hàng dưới
        double bottomRowCenterY = groupCenterY + (verticalSpacing / 2.0);
        // Tâm X của vòng Vàng (Yellow)
        double yellowRingCenterX = groupCenterX - (horizontalSpacing / 2.0);
        // Tâm X của vòng Lục (Green)
        double greenRingCenterX = groupCenterX + (horizontalSpacing / 2.0);

        rings[1] = createRing(yellowRingCenterX, bottomRowCenterY, ringInnerRadius, ringColors[1], ringStrokeWidth); // Vàng (Yellow)
        rings[3] = createRing(greenRingCenterX, bottomRowCenterY, ringInnerRadius, ringColors[3], ringStrokeWidth); // Lục (Green)


        // Z-ordering (thứ tự vẽ để lồng vào nhau đúng)
        // Thứ tự vẽ chuẩn: Vàng, Lục, Xanh, Đỏ, Đen (hoặc điều chỉnh viewOrder)
        // Đơn giản hơn: các vòng được vẽ sau sẽ đè lên vòng vẽ trước trong cùng một Group.
        // Hoặc dùng viewOrder: giá trị nhỏ hơn sẽ ở trên.
        ringsGroup.getChildren().addAll(rings[1], rings[3], rings[0], rings[4], rings[2]);
        // Vàng (1) và Lục (3) ở dưới cùng.
        // Xanh (0) và Đỏ (4) ở giữa.
        // Đen (2) ở trên cùng.

        // Nếu muốn dùng setViewOrder (giá trị âm hơn sẽ ở trên):
        // rings[1].setViewOrder(2); // Yellow
        // rings[3].setViewOrder(2); // Green
        // rings[0].setViewOrder(1); // Blue
        // rings[4].setViewOrder(1); // Red
        // rings[2].setViewOrder(0); // Black


        // Initial state - rings are invisible and small
        ringsGroup.setScaleX(0.1);
        ringsGroup.setScaleY(0.1);
        ringsGroup.setOpacity(0);
    }

    // Sửa lại createRing để nhận strokeWidth riêng
    private Circle createRing(double centerX, double centerY, double innerRadius, Color color, double strokeWidth) {
        Circle ring = new Circle(centerX, centerY, innerRadius + strokeWidth / 2.0); // Bán kính của đường tâm nét vẽ
        ring.setFill(Color.TRANSPARENT);
        ring.setStroke(color);
        ring.setStrokeWidth(strokeWidth);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(Math.max(5, strokeWidth * 0.5)); // Điều chỉnh shadow theo độ dày
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setOffsetX(strokeWidth * 0.1);
        shadow.setOffsetY(strokeWidth * 0.1);
        ring.setEffect(shadow);

        return ring;
    }

    public void startAnimation() {
        // Create the entrance animation
        entranceAnimation = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(ringsGroup.scaleXProperty(), 0.1),
                new KeyValue(ringsGroup.scaleYProperty(), 0.1),
                new KeyValue(ringsGroup.opacityProperty(), 0)
            ),
            new KeyFrame(Duration.seconds(1.5),
                new KeyValue(ringsGroup.scaleXProperty(), 1.0),
                new KeyValue(ringsGroup.scaleYProperty(), 1.0),
                new KeyValue(ringsGroup.opacityProperty(), 1.0)
            )
        );

        // Add continuous subtle pulsing animation
        pulseAnimation = new Timeline( // Gán cho biến thành viên
            new KeyFrame(Duration.seconds(0),
                new KeyValue(ringsGroup.scaleXProperty(), 1.0),
                new KeyValue(ringsGroup.scaleYProperty(), 1.0)
            ),
            new KeyFrame(Duration.seconds(2),
                new KeyValue(ringsGroup.scaleXProperty(), 1.05),
                new KeyValue(ringsGroup.scaleYProperty(), 1.05)
            ),
            new KeyFrame(Duration.seconds(4),
                new KeyValue(ringsGroup.scaleXProperty(), 1.0),
                new KeyValue(ringsGroup.scaleYProperty(), 1.0)
            )
        );
        pulseAnimation.setCycleCount(Animation.INDEFINITE);

        entranceAnimation.setOnFinished(e -> {
            if (pulseAnimation != null) { // Kiểm tra null phòng trường hợp stopAnimation được gọi trước
                pulseAnimation.play();
            }
        });
        entranceAnimation.play();
    }

    public void stopAnimation() {
        if (entranceAnimation != null) {
            entranceAnimation.stop();
        }
        if (pulseAnimation != null) { // Dừng cả pulse animation
            pulseAnimation.stop();
        }
        // Dọn dẹp rings khỏi group để tránh memory leak nếu cần
        ringsGroup.getChildren().clear();
    }
}